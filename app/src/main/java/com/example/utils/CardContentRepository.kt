package com.example.utils

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class FirestoreCardContent(
    val cardId: String = "",
    val titleHi: String = "",
    val titleEn: String = "",
    val emoji: String = "✨",
    val descriptionHi: String = "",
    val descriptionEn: String = "",
    val remedies: List<String> = emptyList(),
    val existsInCloud: Boolean = true
)

object CardContentRepository {
    suspend fun fetchCardContent(cardId: String): FirestoreCardContent {
        return try {
            val db = FirestoreProvider.get()
            val doc = db.collection("card_contents").document(cardId).get().await()
            if (doc.exists()) {
                FirestoreCardContent(
                    cardId = doc.getString("cardId") ?: cardId,
                    titleHi = doc.getString("titleHi") ?: doc.getString("title") ?: "",
                    titleEn = doc.getString("titleEn") ?: "",
                    emoji = doc.getString("emoji") ?: "✨",
                    descriptionHi = doc.getString("descriptionHi") ?: doc.getString("description") ?: "",
                    descriptionEn = doc.getString("descriptionEn") ?: "",
                    remedies = (doc.get("remedies") as? List<*>)?.map { it.toString() } ?: emptyList(),
                    existsInCloud = true
                )
            } else {
                FirestoreCardContent(
                    cardId = cardId,
                    existsInCloud = false
                )
            }
        } catch (e: Exception) {
            FirestoreCardContent(
                cardId = cardId,
                existsInCloud = false
            )
        }
    }
}
