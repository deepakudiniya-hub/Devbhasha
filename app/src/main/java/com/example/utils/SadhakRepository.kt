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
            .whereEqualTo("isOnline", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val currentTime = System.currentTimeMillis()
                    val items = snapshot.documents.mapNotNull { doc ->
                        // Agreement accepted check
                        val agreementAccepted = doc.getBoolean("agreementAccepted") == true ||
                            doc.getBoolean("hasAcceptedAgreement") == true ||
                            !doc.getString("acceptedAgreementVersion").isNullOrBlank()
                        if (!agreementAccepted) return@mapNotNull null

                        // Profile approved check
                        val isApproved = doc.getBoolean("approved") == true ||
                            doc.getBoolean("isApproved") == true ||
                            doc.getString("status")?.equals("APPROVED", ignoreCase = true) == true ||
                            doc.getString("verificationStatus")?.equals("verified", ignoreCase = true) == true ||
                            doc.getBoolean("isVerified") == true
                        if (!isApproved) return@mapNotNull null

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

                        val phone = ""

                        val initialHi = nameHi.firstOrNull()?.toString() ?: "सा"
                        val initialEn = nameEn.firstOrNull()?.uppercase() ?: "S"

                        val chatPrice = doc.getLong("chatPricePerMin")?.toInt() ?: 0
                        val callPrice = doc.getLong("callPricePerMin")?.toInt() ?: 0
                        val sessionPrice = doc.getLong("sessionPrice30Min")?.toInt() ?: 0

                        val feeSummary = when {
                            chatPrice > 0 -> "₹$chatPrice / min"
                            callPrice > 0 -> "₹$callPrice / min"
                            sessionPrice > 0 -> "₹$sessionPrice"
                            else -> ""
                        }

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
                            consultationFee = feeSummary,
                            bio = bio,
                            phone = phone,
                            chatPricePerMin = chatPrice,
                            callPricePerMin = callPrice,
                            sessionPrice30Min = sessionPrice
                        )
                    }
                    onUpdate(items)
                }
            }
    }

    /**
     * Approved + verified sadhaks INCLUDING offline ones (for the topic-FAQ
     * "साधक चुनें" list, where offline sadhaks are shown disabled).
     * Same query/mapping as observeOnlineSadhaks; the existing function is unchanged.
     * rating is "" when the sadhak has no rating yet (UI shows "नए").
     */
    fun observeApprovedSadhaks(
        context: Context?,
        onUpdate: (List<SadhakItem>) -> Unit,
        onError: (Exception) -> Unit = {}
    ): ListenerRegistration {
        val db = FirestoreProvider.get(context)

        return db.collection("sadhaks")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val currentTime = System.currentTimeMillis()
                    val items = snapshot.documents.mapNotNull { doc ->
                        // Agreement accepted check
                        val agreementAccepted = doc.getBoolean("agreementAccepted") == true ||
                            doc.getBoolean("hasAcceptedAgreement") == true ||
                            !doc.getString("acceptedAgreementVersion").isNullOrBlank()
                        if (!agreementAccepted) return@mapNotNull null

                        // Profile approved check
                        val isApproved = doc.getBoolean("approved") == true ||
                            doc.getBoolean("isApproved") == true ||
                            doc.getString("status")?.equals("APPROVED", ignoreCase = true) == true ||
                            doc.getString("verificationStatus")?.equals("verified", ignoreCase = true) == true ||
                            doc.getBoolean("isVerified") == true
                        if (!isApproved) return@mapNotNull null

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
                            ?: ""
                        val titleHi = doc.getString("titleHi")?.takeIf { it.isNotBlank() } ?: title
                        val titleEn = doc.getString("titleEn")?.takeIf { it.isNotBlank() } ?: title

                        val isOnlineRaw = doc.getBoolean("isOnline") ?: doc.getBoolean("availability") ?: false
                        val lastSeenTimestamp = doc.getTimestamp("lastSeen")?.toDate()?.time
                            ?: doc.getLong("lastSeen")
                            ?: doc.getTimestamp("lastHeartbeat")?.toDate()?.time
                            ?: doc.getLong("lastHeartbeat") ?: 0L
                        val isOnline = if (lastSeenTimestamp > 0L) {
                            isOnlineRaw && (currentTime - lastSeenTimestamp < 15 * 60 * 1000L)
                        } else {
                            isOnlineRaw
                        }

                        val ratingStr = when (val ratingVal = doc.get("rating")) {
                            is Number -> if (ratingVal.toDouble() > 0.0) String.format(Locale.US, "%.1f", ratingVal.toDouble()) else ""
                            is String -> ratingVal.trim()
                            else -> ""
                        }

                        val exp = doc.getString("experience")
                            ?: doc.getString("experienceHi")
                            ?: "10+ वर्ष अनुभव"
                        val expEn = doc.getString("experienceEn") ?: exp

                        val initialHi = nameHi.firstOrNull()?.toString() ?: "सा"
                        val initialEn = nameEn.firstOrNull()?.uppercase() ?: "S"

                        val chatPrice = doc.getLong("chatPricePerMin")?.toInt() ?: 0
                        val callPrice = doc.getLong("callPricePerMin")?.toInt() ?: 0
                        val sessionPrice = doc.getLong("sessionPrice30Min")?.toInt() ?: 0

                        val feeSummary = when {
                            chatPrice > 0 -> "₹$chatPrice / min"
                            callPrice > 0 -> "₹$callPrice / min"
                            sessionPrice > 0 -> "₹$sessionPrice"
                            else -> ""
                        }

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
                            isVerified = doc.getBoolean("isVerified") ?: false,
                            isOnline = isOnline,
                            consultationFee = feeSummary,
                            bio = doc.getString("bio") ?: doc.getString("description") ?: doc.getString("about") ?: "",
                            phone = "",
                            chatPricePerMin = chatPrice,
                            callPricePerMin = callPrice,
                            sessionPrice30Min = sessionPrice
                        )
                    }.sortedByDescending { it.isOnline }
                    onUpdate(items)
                }
            }
    }
}
