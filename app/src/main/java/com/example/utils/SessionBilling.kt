package com.example.utils

import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import kotlinx.coroutines.tasks.await

/**
 * Server-authoritative session billing.
 *
 * The app NEVER computes or sends rates, durations or amounts. It only asks the
 * server to start / extend / end a session and displays whatever the server
 * returns (`endsAt`, `amountPaise`, `isFreeTrial`). Wallet debits, free-trial
 * eligibility (once per phone number) and pricing are decided in Cloud Functions.
 *
 * Callables (default region):
 *  - startSession({type, providerId}) -> {sessionId, endsAt, amountPaise, isFreeTrial}
 *  - extendSession({sessionId})       -> {endsAt, amountPaise}
 *  - getAgoraToken({sessionId})       -> {token, channel, uid, appId}
 *  - endSession({sessionId})
 */
enum class SessionType(val wire: String) {
    SESSION("SESSION"),
    DREAM_CHAT("DREAM_CHAT"),
    DREAM_CALL("DREAM_CALL")
}

data class StartedSession(
    val sessionId: String,
    /** Epoch millis when the server will end the session. */
    val endsAtMillis: Long,
    val amountPaise: Long,
    val isFreeTrial: Boolean
)

data class ExtendedSession(
    val endsAtMillis: Long,
    val amountPaise: Long
)

data class AgoraJoinInfo(
    val token: String,
    val channel: String,
    val uid: Int,
    val appId: String
)

/**
 * Display-only price labels. These strings are NEVER used to compute or send
 * money; the authoritative amount always comes back from the server.
 */
object PriceLabels {
    const val SESSION = "₹499 / 20 min"
    const val EXTEND = "+5 min ₹99"
    const val DREAM_CHAT = "₹99 / 7 min"
    const val DREAM_CALL = "₹199 / 10 min"
    const val FREE_TRIAL_BADGE = "पहली स्वप्न चैट 5 मिनट मुफ़्त"
    const val FREE_TRIAL_BADGE_EN = "First dream chat free · 5 min"

    /** Seconds left at which the "extend" warning is shown. */
    const val EXTEND_WARNING_SECONDS = 120L

    fun forType(type: SessionType): String = when (type) {
        SessionType.SESSION -> SESSION
        SessionType.DREAM_CHAT -> DREAM_CHAT
        SessionType.DREAM_CALL -> DREAM_CALL
    }

    /** Formats a server-provided amount for display. */
    fun formatPaise(amountPaise: Long): String {
        val rupees = amountPaise / 100
        val paise = amountPaise % 100
        return if (paise == 0L) "₹$rupees" else "₹$rupees.${paise.toString().padStart(2, '0')}"
    }
}

class SessionBillingException(
    message: String,
    val code: FirebaseFunctionsException.Code? = null,
    cause: Throwable? = null
) : Exception(message, cause) {
    /** Server rejected because the wallet doesn't cover the price. */
    val isInsufficientBalance: Boolean
        get() = code == FirebaseFunctionsException.Code.RESOURCE_EXHAUSTED ||
            (code == FirebaseFunctionsException.Code.FAILED_PRECONDITION &&
                (message ?: "").contains("insufficient", ignoreCase = true))
    val isUnauthenticated: Boolean
        get() = code == FirebaseFunctionsException.Code.UNAUTHENTICATED
}

object SessionBilling {
    private val functions: FirebaseFunctions by lazy { FirebaseFunctions.getInstance() }

    suspend fun startSession(type: SessionType, providerId: String): Result<StartedSession> = runCall {
        val data = call("startSession", mapOf("type" to type.wire, "providerId" to providerId))
        StartedSession(
            sessionId = data.str("sessionId") ?: throw malformed("startSession"),
            endsAtMillis = parseEpochMillis(data["endsAt"]) ?: throw malformed("startSession"),
            amountPaise = (data["amountPaise"] as? Number)?.toLong() ?: 0L,
            isFreeTrial = data["isFreeTrial"] as? Boolean ?: false
        )
    }

    suspend fun extendSession(sessionId: String): Result<ExtendedSession> = runCall {
        val data = call("extendSession", mapOf("sessionId" to sessionId))
        ExtendedSession(
            endsAtMillis = parseEpochMillis(data["endsAt"]) ?: throw malformed("extendSession"),
            amountPaise = (data["amountPaise"] as? Number)?.toLong() ?: 0L
        )
    }

    suspend fun getAgoraToken(sessionId: String): Result<AgoraJoinInfo> = runCall {
        val data = call("getAgoraToken", mapOf("sessionId" to sessionId))
        AgoraJoinInfo(
            token = data.str("token") ?: throw malformed("getAgoraToken"),
            channel = data.str("channel") ?: throw malformed("getAgoraToken"),
            uid = (data["uid"] as? Number)?.toInt() ?: 0,
            appId = data.str("appId") ?: ""
        )
    }

    suspend fun endSession(sessionId: String): Result<Unit> = runCall {
        call("endSession", mapOf("sessionId" to sessionId))
        Unit
    }

    /** Fire-and-forget end, for use from dispose / non-suspending contexts. */
    fun endSessionAsync(sessionId: String) {
        if (sessionId.isBlank()) return
        functions.getHttpsCallable("endSession").call(mapOf("sessionId" to sessionId))
    }

    // ---- internals ----

    private suspend fun call(name: String, payload: Map<String, Any>): Map<String, Any?> {
        val res = functions.getHttpsCallable(name).call(payload).await()
        @Suppress("UNCHECKED_CAST")
        return (res.data as? Map<String, Any?>) ?: emptyMap()
    }

    private inline fun <T> runCall(block: () -> T): Result<T> = try {
        Result.success(block())
    } catch (e: kotlinx.coroutines.CancellationException) {
        throw e
    } catch (e: FirebaseFunctionsException) {
        Result.failure(SessionBillingException(e.message ?: "Server error", e.code, e))
    } catch (e: SessionBillingException) {
        Result.failure(e)
    } catch (e: Exception) {
        Result.failure(SessionBillingException(e.message ?: "Network error", null, e))
    }

    private fun malformed(fn: String) = SessionBillingException("Malformed $fn response")

    private fun Map<String, Any?>.str(key: String): String? =
        (this[key] as? String)?.takeIf { it.isNotBlank() }

    private fun parseIso(v: String): Long? {
        // SimpleDateFormat pattern 'X' is available from API 24 (minSdk).
        for (pattern in listOf("yyyy-MM-dd'T'HH:mm:ss.SSSX", "yyyy-MM-dd'T'HH:mm:ssX")) {
            val parsed = runCatching {
                java.text.SimpleDateFormat(pattern, java.util.Locale.US).parse(v)?.time
            }.getOrNull()
            if (parsed != null) return parsed
        }
        return null
    }

    /** Accepts epoch millis/seconds, ISO-8601 strings, or serialized Timestamps. */
    internal fun parseEpochMillis(v: Any?): Long? = when (v) {
        is Number -> v.toLong().let { if (it < 100_000_000_000L) it * 1000 else it }
        is String -> v.toLongOrNull()?.let { if (it < 100_000_000_000L) it * 1000 else it }
            ?: parseIso(v)
        is Map<*, *> -> {
            val secs = (v["_seconds"] ?: v["seconds"]) as? Number
            val nanos = (v["_nanoseconds"] ?: v["nanoseconds"]) as? Number
            secs?.let { it.toLong() * 1000 + (nanos?.toLong() ?: 0L) / 1_000_000 }
        }
        else -> null
    }
}
