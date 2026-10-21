package com.example.utils

import android.content.Context
import com.example.ui.models.SadhakItem
import com.google.firebase.firestore.ListenerRegistration
import java.util.Locale

object SadhakRepository {

    /**
     * Real-time listener for provider heartbeat & presence status in Firestore.
     * Validates online status via isOnline and recent heartbeat (lastSeen/lastHeartbeat timestamp),
     * ensuring only online providers are displayed in the experts section.
     */
    fun observeOnlineSadhaks(
        context: Context?,
        onUpdate: (List<SadhakItem>) -> Unit,
        onError: (Exception) -> Unit = {}
    ): ListenerRegistration {
        val db = FirestoreProvider.get(context)

        return db.collection("sadhaks")
            .whereEqualTo("status", "APPROVED")
            .whereEqualTo("isVerified", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val currentTime = System.currentTimeMillis()
                    val items = snapshot.documents.mapNotNull { doc ->
                        val name = doc.getString("name")
                            ?: doc.getString("displayName")
                            ?: doc.getString("nameHi")
                            ?: doc.getString("nameEn")
                            ?: "वैदिक Sadhak"
                        val nameHi = doc.getString("nameHi")?.takeIf { it.isNotBlank() } ?: name
                        val nameEn = doc.getString("nameEn")?.takeIf { it.isNotBlank() }
                            ?: doc.getString("displayName")?.takeIf { it.isNotBlank() }
                            ?: name
                        val title = doc.getString("title")
                            ?: doc.getString("specialization")
                            ?: doc.getString("expertise")
                            ?: doc.getString("titleHi")
                            ?: "वैदिक विशेषज्ञ"
                        val titleHi = doc.getString("titleHi")?.takeIf { it.isNotBlank() } ?: title
                        val titleEn = doc.getString("titleEn")?.takeIf { it.isNotBlank() } ?: title

                        val isVerified = doc.getBoolean("isVerified") ?: false
                        val isOnlineRaw = doc.getBoolean("isOnline") ?: doc.getBoolean("availability") ?: false

                        // Provider heartbeat status check (lastSeen / lastHeartbeat timestamp)
                        val lastSeenTimestamp = doc.getTimestamp("lastSeen")?.toDate()?.time
                            ?: doc.getLong("lastSeen")
                            ?: doc.getTimestamp("lastHeartbeat")?.toDate()?.time
                            ?: doc.getLong("lastHeartbeat") ?: 0L

                        // Only consider online if isOnlineRaw is true and heartbeat is within last 15 minutes (if timestamp is provided)
                        val isOnline = if (lastSeenTimestamp > 0L) {
                            isOnlineRaw && (currentTime - lastSeenTimestamp < 15 * 60 * 1000L)
                        } else {
                            isOnlineRaw
                        }

                        // Ensure only online providers are displayed in the experts section
                        if (!isOnline) {
                            return@mapNotNull null
                        }

                        val ratingVal = doc.get("rating")
                        val ratingStr = when (ratingVal) {
                            is Number -> String.format(Locale.US, "%.1f", ratingVal.toDouble())
                            is String -> ratingVal
                            else -> "4.9"
                        }

                        val exp = doc.getString("experience")
                            ?: doc.getString("experienceHi")
                            ?: "10+ वर्ष अनुभव"
                        val expEn = doc.getString("experienceEn") ?: exp

                        val bio = doc.getString("bio")
                            ?: doc.getString("description")
                            ?: doc.getString("about")
                            ?: "वैदिक परंपरा के अनुसार सटीक मार्गदर्शन और अचूक उपाय प्रदान करते हैं।"

                        val phone = doc.getString("phone")
                            ?: doc.getString("phoneNumber")
                            ?: ""

                        val initialHi = nameHi.firstOrNull()?.toString() ?: "सा"
                        val initialEn = nameEn.firstOrNull()?.uppercase() ?: "S"

                        SadhakItem(
                            id = doc.getString("sadhakId")?.takeIf { it.isNotBlank() } ?: doc.id,
                            nameHi = nameHi,
                            nameEn = nameEn,
                            titleHi = titleHi,
                            titleEn = titleEn,
                            experienceHi = exp,
                            experienceEn = expEn,
                            rating = ratingStr,
                            initialHi = initialHi,
                            initialEn = initialEn,
                            isVerified = isVerified,
                            isOnline = isOnline,
                            consultationFee = doc.getString("consultationFee") ?: "₹499 / 20 min",
                            bio = bio,
                            phone = phone
                        )
                    }
                    onUpdate(items)
                }
            }
    }
}
