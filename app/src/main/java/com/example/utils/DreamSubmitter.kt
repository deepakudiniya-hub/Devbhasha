package com.example.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

sealed class DreamSubmitResult {
    data class Success(
        val sadhakName: String,
        val questionId: String,
        /** True only when the SERVER granted the one-time free dream chat. */
        val isFreeTrial: Boolean = false,
        /** Amount the server charged (display only). */
        val amountPaise: Long = 0L
    ) : DreamSubmitResult()
    data class NoVerifiedSadhak(
        val message: String = "अभी कोई सत्यापित साधक उपलब्ध नहीं है, कृपया थोड़ी देर बाद प्रयास करें।"
    ) : DreamSubmitResult()
    /** Server refused because the wallet can't cover the price; prompt a recharge. */
    data class InsufficientBalance(
        val message: String = "वॉलेट में पर्याप्त राशि नहीं है। कृपया रिचार्ज करें।"
    ) : DreamSubmitResult()
    data class Error(val message: String) : DreamSubmitResult()
}

data class VerifiedSadhakInfo(
    val id: String,
    val name: String
)

/**
 * Submits a dream for interpretation.
 *
 * A dream question is a separate session kind with status = "pending" and also includes providerId.
 * Creates session doc fields: userId, providerId, type = "dream", status = "pending".
 */
object DreamSubmitter {

    private suspend fun pickVerifiedSadhak(targetProviderId: String? = null): VerifiedSadhakInfo? {
        val db = FirestoreProvider.get()
        if (!targetProviderId.isNullOrBlank()) {
            val direct = runCatching { db.collection("sadhaks").document(targetProviderId).get().await() }.getOrNull()
            if (direct != null && direct.exists()) {
                val name = direct.getString("name")
                    ?: direct.getString("displayName")
                    ?: direct.getString("nameHi")
                    ?: "साधक"
                return VerifiedSadhakInfo(id = direct.id, name = name)
            }
        }
        var snap = db.collection("sadhaks")
            .whereEqualTo("status", "APPROVED")
            .whereEqualTo("isVerified", true)
            .limit(20)
            .get()
            .await()
        if (snap.isEmpty) {
            snap = db.collection("providers")
                .whereEqualTo("status", "APPROVED")
                .whereEqualTo("isVerified", true)
                .limit(20)
                .get()
                .await()
        }
        val available = snap.documents.filter { it.getBoolean("availability") != false }
        val doc = (available.ifEmpty { snap.documents }).randomOrNull() ?: return null
        return VerifiedSadhakInfo(
            id = doc.getString("sadhakId")?.takeIf { it.isNotBlank() }
                ?: doc.getString("providerId")?.takeIf { it.isNotBlank() }
                ?: doc.id,
            name = doc.getString("name") ?: doc.getString("displayName") ?: doc.getString("nameHi") ?: "साधक"
        )
    }

    suspend fun submitDream(
        userName: String,
        dreamText: String,
        type: SessionType = SessionType.DREAM_CHAT,
        targetProviderId: String? = null
    ): DreamSubmitResult {
        val cleanText = dreamText.trim()
        if (cleanText.isBlank()) return DreamSubmitResult.Error("सपना खाली नहीं हो सकता।")
        val uid = FirebaseAuth.getInstance().currentUser?.uid
            ?: return DreamSubmitResult.Error("कृपया पहले लॉगिन करें।")

        return try {
            val sadhak = pickVerifiedSadhak(targetProviderId) ?: return DreamSubmitResult.NoVerifiedSadhak()
            val db = FirestoreProvider.get()

            var sessionId = ""
            var isFreeTrial = false
            var amountPaise = 0L

            // Try server-authoritative billing if available
            val startedResult = SessionBilling.startSession(type, sadhak.id)
            startedResult.onSuccess { started ->
                sessionId = started.sessionId
                isFreeTrial = started.isFreeTrial
                amountPaise = started.amountPaise
            }.onFailure { e ->
                val be = e as? SessionBillingException
                if (be?.isInsufficientBalance == true) {
                    return DreamSubmitResult.InsufficientBalance()
                }
            }

            // A dream question is a separate session kind with status = "pending" and also includes providerId
            val sessionRef = if (sessionId.isNotBlank()) {
                db.collection("sessions").document(sessionId)
            } else {
                db.collection("sessions").document()
            }
            sessionId = sessionRef.id

            val sessionDocData = hashMapOf<String, Any>(
                "sessionId" to sessionId,
                "userId" to uid,
                "providerId" to sadhak.id,
                "sadhakName" to sadhak.name,
                "userName" to userName,
                "type" to "dream",
                "kind" to "dream",
                "status" to "pending",
                "questionText" to cleanText,
                "category" to "Dream",
                "createdAt" to FieldValue.serverTimestamp()
            )
            runCatching { sessionRef.set(sessionDocData, SetOptions.merge()).await() }

            // Record the question in questions collection as well
            val questionRef = db.collection("questions").document(sessionId)
            val questionData = hashMapOf<String, Any>(
                "questionText" to cleanText,
                "userId" to uid,
                "userName" to userName,
                "category" to "Dream",
                "providerId" to sadhak.id,
                "sadhakName" to sadhak.name,
                "status" to "pending",
                "sessionId" to sessionId,
                "type" to "dream",
                "createdAt" to FieldValue.serverTimestamp()
            )
            runCatching { questionRef.set(questionData, SetOptions.merge()).await() }

            DreamSubmitResult.Success(
                sadhakName = sadhak.name,
                questionId = sessionId,
                isFreeTrial = isFreeTrial,
                amountPaise = amountPaise
            )
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            DreamSubmitResult.Error(e.localizedMessage ?: "सपना भेजने में त्रुटि आई")
        }
    }
}
