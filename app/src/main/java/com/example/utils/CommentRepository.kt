package com.example.utils

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class CardComment(
    val id: String = "",
    val cardId: String = "",
    val userName: String = "",
    val commentText: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

object CommentRepository {
    suspend fun fetchComments(cardId: String): List<CardComment> {
        return try {
            val db = FirestoreProvider.get()
            val snapshot = db.collection("card_comments")
                .whereEqualTo("cardId", cardId)
                .get()
                .await()
            snapshot.documents.map { doc ->
                CardComment(
                    id = doc.id,
                    cardId = doc.getString("cardId") ?: cardId,
                    userName = doc.getString("userName") ?: "साधक",
                    commentText = doc.getString("commentText") ?: "",
                    timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                )
            }.sortedByDescending { it.timestamp }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun postComment(cardId: String, userName: String, commentText: String): Boolean {
        return try {
            val db = FirestoreProvider.get()
            val data = mapOf(
                "cardId" to cardId,
                "userName" to userName,
                "commentText" to commentText,
                "timestamp" to System.currentTimeMillis()
            )
            db.collection("card_comments").add(data).await()
            true
        } catch (e: Exception) {
            false
        }
    }
}
