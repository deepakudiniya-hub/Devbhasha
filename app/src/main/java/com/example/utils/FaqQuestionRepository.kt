package com.example.utils

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await

/** An approved, anonymised question shown publicly under a topic's FAQ. */
data class PublicFaqQuestion(
    val id: String = "",
    val topic: String = "",
    val question: String = "",
    val answer: String = "",
    val approvedAtMillis: Long = 0L
)

sealed class FaqSubmitResult {
    object Success : FaqSubmitResult()
    object NotSignedIn : FaqSubmitResult()
    data class Error(val message: String) : FaqSubmitResult()
}

/**
 * "आपका सवाल यहाँ नहीं है?" — user-submitted topic questions.
 *
 * Private:  faq_questions/{id}  {topic, text, status:"pending", createdAt, userId}
 *           userId is kept only for abuse control; readable by admins only (rules).
 *           No name / phone / avatar is ever stored.
 * Public:   faq_public/{id}     {topic, question, answer, approvedAt}
 *           Written only by the admin panel on approval; contains no user identifier.
 *           The app reads ONLY this collection.
 */
object FaqQuestionRepository {

    const val MAX_LENGTH = 500
    private const val PRIVATE_COLLECTION = "faq_questions"
    private const val PUBLIC_COLLECTION = "faq_public"

    /** True when a real Firebase user is signed in (guest_explorer has no auth). */
    fun isSignedIn(appUserId: String): Boolean {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        return !uid.isNullOrBlank() && appUserId != "guest_explorer"
    }

    suspend fun submitQuestion(context: Context?, topic: String, rawText: String): FaqSubmitResult {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid.isNullOrBlank()) return FaqSubmitResult.NotSignedIn
        val text = rawText.trim().take(MAX_LENGTH)
        if (text.isEmpty()) return FaqSubmitResult.Error("empty")
        return try {
            val db = FirestoreProvider.get(context)
            db.collection(PRIVATE_COLLECTION).document().set(
                hashMapOf<String, Any>(
                    "topic" to topic,
                    "text" to text,
                    "status" to "pending",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "userId" to uid
                )
            ).await()
            FaqSubmitResult.Success
        } catch (e: Exception) {
            FaqSubmitResult.Error(e.localizedMessage ?: "error")
        }
    }

    /** Live list of approved anonymous questions for one topic (oldest first). */
    fun observePublic(
        context: Context?,
        topic: String,
        onUpdate: (List<PublicFaqQuestion>) -> Unit
    ): ListenerRegistration {
        val db = FirestoreProvider.get(context)
        // Single-field filter only (no composite index needed); sorted on device.
        return db.collection(PUBLIC_COLLECTION)
            .whereEqualTo("topic", topic)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.w("FaqQuestionRepository", "faq_public listen error: ${error.message}")
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    val q = doc.getString("question")?.trim().orEmpty()
                    val a = doc.getString("answer")?.trim().orEmpty()
                    if (q.isEmpty() || a.isEmpty()) return@mapNotNull null
                    PublicFaqQuestion(
                        id = doc.id,
                        topic = doc.getString("topic") ?: topic,
                        question = q,
                        answer = a,
                        approvedAtMillis = doc.getTimestamp("approvedAt")?.toDate()?.time ?: 0L
                    )
                }?.sortedBy { it.approvedAtMillis } ?: emptyList()
                onUpdate(list)
            }
    }
}
