package com.example.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
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
 * Billing is server-authoritative: the app calls `startSession` with only the
 * session type and provider. The server decides price, duration, whether this is
 * the one-time free dream chat (once per phone number), and debits the wallet.
 * The client never sends an amount.
 */
object DreamSubmitter {

    private suspend fun pickVerifiedSadhak(): VerifiedSadhakInfo? {
        val snap = FirestoreProvider.get()
            .collection("sadhaks")
            .whereEqualTo("verified", true)
            .limit(25)
            .get()
            .await()
        val available = snap.documents.filter { it.getBoolean("availability") != false }
        val doc = (available.ifEmpty { snap.documents }).randomOrNull() ?: return null
        return VerifiedSadhakInfo(
            id = doc.getString("sadhakId")?.takeIf { it.isNotBlank() } ?: doc.id,
            name = doc.getString("name") ?: "साधक"
        )
    }

    suspend fun submitDream(
        userName: String,
        dreamText: String,
        type: SessionType = SessionType.DREAM_CHAT
    ): DreamSubmitResult {
        val cleanText = dreamText.trim()
        if (cleanText.isBlank()) return DreamSubmitResult.Error("सपना खाली नहीं हो सकता।")
        val uid = FirebaseAuth.getInstance().currentUser?.uid
            ?: return DreamSubmitResult.Error("कृपया पहले लॉगिन करें।")

        return try {
            val sadhak = pickVerifiedSadhak() ?: return DreamSubmitResult.NoVerifiedSadhak()

            val started = SessionBilling.startSession(type, sadhak.id).getOrElse { e ->
                val be = e as? SessionBillingException
                return if (be?.isInsufficientBalance == true) DreamSubmitResult.InsufficientBalance()
                else DreamSubmitResult.Error(e.localizedMessage ?: "सत्र शुरू नहीं हो सका")
            }

            // Record the question (no money fields — earningAmount is server-only).
            val questionRef = FirestoreProvider.get().collection("questions").document()
            questionRef.set(
                mapOf(
                    "questionText" to cleanText,
                    "userId" to uid,
                    "userName" to userName,
                    "category" to "Dream",
                    "providerId" to sadhak.id,
                    "sadhakName" to sadhak.name,
                    "status" to "assigned",
                    "sessionId" to started.sessionId,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()

            DreamSubmitResult.Success(
                sadhakName = sadhak.name,
                questionId = questionRef.id,
                isFreeTrial = started.isFreeTrial,
                amountPaise = started.amountPaise
            )
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            DreamSubmitResult.Error(e.localizedMessage ?: "सपना भेजने में त्रुटि आई")
        }
    }
}
