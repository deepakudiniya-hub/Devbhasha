package com.example.data

import android.content.Context
import android.util.Log
import com.example.ui.models.DreamCommentItem
import com.example.ui.models.PublicDreamItem
import com.example.ui.models.SadhakCommentSnippet
import com.example.utils.FirestoreProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class CommunityDreamRepository(context: Context) {

    private val db: FirebaseFirestore = FirestoreProvider.get(context)
    private val dreamsCollection = db.collection("public_dreams")

    companion object {
        private const val TAG = "CommunityDreamRepo"

        @Volatile
        private var instance: CommunityDreamRepository? = null

        fun getInstance(context: Context): CommunityDreamRepository {
            return instance ?: synchronized(this) {
                instance ?: CommunityDreamRepository(context.applicationContext).also { instance = it }
            }
        }
    }

    /**
     * Observes real-time stream of community dreams from public_dreams collection.
     */
    fun observePublicDreams(currentUserId: String): Flow<List<PublicDreamItem>> = callbackFlow {
        val listenerRegistration = dreamsCollection
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(60)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error observing public_dreams: ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val items = snapshot.documents.mapNotNull { doc ->
                        try {
                            val dreamId = doc.getString("dreamId") ?: doc.id
                            val userId = doc.getString("userId") ?: ""
                            val authorName = doc.getString("authorName") ?: "Anonymous"
                            var dreamText = doc.getString("dreamText") ?: ""
                            @Suppress("UNCHECKED_CAST")
                            var tags = (doc.get("tags") as? List<String>) ?: emptyList()
                            val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                            val likeCount = (doc.getLong("likeCount") ?: 0L).toInt()
                            val commentCount = (doc.getLong("commentCount") ?: 0L).toInt()
                            val isAnonymous = doc.getBoolean("isAnonymous") ?: (authorName.equals("Anonymous", ignoreCase = true))

                            val topSadhakData = doc.get("topSadhakComment") as? Map<*, *>
                            var topSadhakComment = topSadhakData?.let {
                                SadhakCommentSnippet(
                                    commentId = it["commentId"]?.toString() ?: "",
                                    userId = it["userId"]?.toString() ?: "",
                                    authorName = it["authorName"]?.toString() ?: "Sadhak",
                                    text = it["text"]?.toString() ?: "",
                                    timestamp = (it["timestamp"] as? Number)?.toLong() ?: 0L,
                                    authorRole = it["authorRole"]?.toString() ?: "sadhak"
                                )
                            }

                            // Ensure starter dreams display in pure English even if previously seeded in Hindi
                            if (dreamId == "starter_dream_01") {
                                if (dreamText.contains("आज प्रातः") || tags.any { it.contains("भगवान") }) {
                                    seedStarterDreamsIfEmpty()
                                }
                                dreamText = "Early this morning, I saw an ancient Shiva Lingam in my dream with a stream of pure crystal water flowing perpetually over it. The sacred resonance of 'Om Namah Shivaya' echoed all around. Does this indicate a spiritual awakening or sadhana guidance?"
                                tags = listOf("Lord Shiva", "Temple Vision", "Sacred Water", "Sadhana")
                                topSadhakComment = SadhakCommentSnippet(
                                    commentId = "sadhak_cmt_01",
                                    userId = "sadhak_acharya_ramesh",
                                    authorName = "Acharya Ramesh Shastri",
                                    text = "Pranam seeker! Seeing water continuously flowing over the Shiva Lingam is deeply auspicious. It signifies mental tranquility, cleansing of past karmas, and progress in meditation. Consider offering fresh water with gratitude each morning.",
                                    timestamp = topSadhakComment?.timestamp ?: (timestamp + 1000 * 60 * 25),
                                    authorRole = "sadhak"
                                )
                            } else if (dreamId == "starter_dream_03") {
                                if (dreamText.contains("श्वेत गाय") || tags.any { it.contains("गौमाता") }) {
                                    seedStarterDreamsIfEmpty()
                                }
                                dreamText = "I saw a radiant white cow peacefully entering our courtyard and gently touching the sacred Tulsi plant with affection and warmth."
                                tags = listOf("Gau Mata", "Holy Tulsi", "Auspicious Vision")
                                topSadhakComment = SadhakCommentSnippet(
                                    commentId = "sadhak_cmt_03",
                                    userId = "sadhak_devi_ananya",
                                    authorName = "Sadhvi Maa Gayatri",
                                    text = "The conjunction of the sacred cow and holy Tulsi signifies divine blessings of prosperity, grace, and harmony within your household. Peace and abundance are arriving.",
                                    timestamp = topSadhakComment?.timestamp ?: (timestamp + 1000 * 60 * 120),
                                    authorRole = "sadhak"
                                )
                            }

                            PublicDreamItem(
                                dreamId = dreamId,
                                userId = userId,
                                authorName = authorName,
                                dreamText = dreamText,
                                tags = tags,
                                timestamp = timestamp,
                                likeCount = likeCount,
                                commentCount = commentCount,
                                isAnonymous = isAnonymous,
                                topSadhakComment = topSadhakComment
                            )
                        } catch (e: Exception) {
                            Log.w(TAG, "Failed to parse dream document: ${e.message}")
                            null
                        }
                    }

                    if (items.isEmpty()) {
                        seedStarterDreamsIfEmpty()
                    }

                    trySend(items)
                }
            }

        awaitClose {
            listenerRegistration.remove()
        }
    }

    /**
     * Observes comments for a specific dream in real-time.
     */
    fun observeComments(dreamId: String): Flow<List<DreamCommentItem>> = callbackFlow {
        val commentsRef = dreamsCollection.document(dreamId).collection("comments")
            .orderBy("timestamp", Query.Direction.ASCENDING)

        val listener = commentsRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Error observing comments for dream $dreamId: ${error.message}")
                trySend(emptyList())
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val comments = snapshot.documents.mapNotNull { doc ->
                    try {
                        DreamCommentItem(
                            commentId = doc.getString("commentId") ?: doc.id,
                            userId = doc.getString("userId") ?: "",
                            authorName = doc.getString("authorName") ?: "Seeker",
                            text = doc.getString("text") ?: "",
                            timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                            authorRole = doc.getString("authorRole") ?: "user"
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                trySend(comments)
            }
        }

        awaitClose {
            listener.remove()
        }
    }

    /**
     * Posts a new dream to public_dreams.
     */
    suspend fun postPublicDream(
        userId: String,
        authorName: String,
        dreamText: String,
        isAnonymous: Boolean,
        customTags: List<String> = emptyList()
    ): Result<PublicDreamItem> {
        return try {
            val dreamId = UUID.randomUUID().toString()
            val finalAuthor = if (isAnonymous || authorName.isBlank()) "Anonymous" else authorName.trim()
            val extractedTags = if (customTags.isNotEmpty()) customTags else autoDetectVedicTags(dreamText)
            val now = System.currentTimeMillis()

            val dreamItem = PublicDreamItem(
                dreamId = dreamId,
                userId = userId,
                authorName = finalAuthor,
                dreamText = dreamText.trim(),
                tags = extractedTags,
                timestamp = now,
                likeCount = 0,
                commentCount = 0,
                isAnonymous = isAnonymous
            )

            val payload = hashMapOf<String, Any>(
                "dreamId" to dreamId,
                "userId" to userId,
                "authorName" to finalAuthor,
                "dreamText" to dreamText.trim(),
                "tags" to extractedTags,
                "timestamp" to now,
                "likeCount" to 0,
                "commentCount" to 0,
                "isAnonymous" to isAnonymous
            )

            dreamsCollection.document(dreamId).set(payload).await()
            Result.success(dreamItem)
        } catch (e: Exception) {
            Log.e(TAG, "Error posting public dream: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Toggles Pranam (Like) for a dream.
     */
    suspend fun togglePranamLike(dreamId: String, userId: String, isCurrentlyLiked: Boolean): Result<Boolean> {
        return try {
            val dreamDocRef = dreamsCollection.document(dreamId)
            val likeDocRef = dreamDocRef.collection("likes").document(userId)

            db.runTransaction { tx ->
                val dreamSnap = tx.get(dreamDocRef)
                val currentLikes = (dreamSnap.getLong("likeCount") ?: 0L).toInt()

                if (isCurrentlyLiked) {
                    tx.delete(likeDocRef)
                    tx.update(dreamDocRef, "likeCount", maxOf(0, currentLikes - 1))
                } else {
                    tx.set(likeDocRef, mapOf("userId" to userId, "timestamp" to System.currentTimeMillis()))
                    tx.update(dreamDocRef, "likeCount", currentLikes + 1)
                }
            }.await()

            Result.success(!isCurrentlyLiked)
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling like for dream $dreamId: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Checks if current user has liked a dream.
     */
    suspend fun isDreamLikedByUser(dreamId: String, userId: String): Boolean {
        return try {
            if (userId.isBlank()) return false
            val snap = dreamsCollection.document(dreamId).collection("likes").document(userId).get().await()
            snap.exists()
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Posts a comment to public_dreams/{dreamId}/comments sub-collection.
     */
    suspend fun postComment(
        dreamId: String,
        userId: String,
        authorName: String,
        text: String,
        authorRole: String = "user"
    ): Result<DreamCommentItem> {
        return try {
            val commentId = UUID.randomUUID().toString()
            val now = System.currentTimeMillis()
            val commentDocRef = dreamsCollection.document(dreamId).collection("comments").document(commentId)
            val dreamDocRef = dreamsCollection.document(dreamId)

            val commentItem = DreamCommentItem(
                commentId = commentId,
                userId = userId,
                authorName = authorName.ifBlank { if (authorRole == "sadhak") "Sadhak" else "Seeker" },
                text = text.trim(),
                timestamp = now,
                authorRole = authorRole
            )

            val commentPayload = hashMapOf<String, Any>(
                "commentId" to commentId,
                "userId" to userId,
                "authorName" to commentItem.authorName,
                "text" to text.trim(),
                "timestamp" to now,
                "authorRole" to authorRole
            )

            db.runTransaction { tx ->
                tx.set(commentDocRef, commentPayload)
                tx.update(dreamDocRef, "commentCount", FieldValue.increment(1))

                // If sadhak comments, set topSadhakComment for prominent banner highlight
                if (authorRole == "sadhak") {
                    val snippet = mapOf(
                        "commentId" to commentId,
                        "userId" to userId,
                        "authorName" to commentItem.authorName,
                        "text" to text.trim(),
                        "timestamp" to now,
                        "authorRole" to "sadhak"
                    )
                    tx.update(dreamDocRef, "topSadhakComment", snippet)
                }
            }.await()

            Result.success(commentItem)
        } catch (e: Exception) {
            Log.e(TAG, "Error posting comment for dream $dreamId: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Auto-detects spiritual & Vedic tags from text for structured community discovery.
     */
    private fun autoDetectVedicTags(text: String): List<String> {
        val detected = mutableListOf<String>()
        val lower = text.lowercase()

        val keywordToTag = mapOf(
            "शिव" to "Lord Shiva",
            "shiva" to "Lord Shiva",
            "मंदिर" to "Temple Vision",
            "temple" to "Temple Vision",
            "सर्प" to "Serpent",
            "snake" to "Serpent",
            "जल" to "Sacred Water",
            "water" to "Sacred Water",
            "गंगा" to "Holy Ganga",
            "ganga" to "Holy Ganga",
            "सूर्य" to "Surya Deva",
            "sun" to "Surya Deva",
            "चन्द्र" to "Chandra",
            "moon" to "Chandra",
            "दीपक" to "Sacred Flame",
            "diya" to "Sacred Flame",
            "गाय" to "Gau Mata",
            "cow" to "Gau Mata",
            "उड़ान" to "Flight / Sky",
            "flying" to "Flight / Sky",
            "पितृ" to "Ancestors",
            "ancestor" to "Ancestors",
            "कमल" to "Sacred Lotus",
            "lotus" to "Sacred Lotus"
        )

        for ((key, tag) in keywordToTag) {
            if (lower.contains(key) && !detected.contains(tag)) {
                detected.add(tag)
                if (detected.size >= 4) break
            }
        }

        if (detected.isEmpty()) {
            detected.add("Dream Vision")
            detected.add("Inner Reflection")
        }

        return detected
    }

    /**
     * Seeds initial community dreams if collection is empty so seekers have a rich discussion feed.
     */
    private fun seedStarterDreamsIfEmpty() {
        try {
            val starterDreams = listOf(
                PublicDreamItem(
                    dreamId = "starter_dream_01",
                    userId = "seeker_divine_01",
                    authorName = "Anonymous",
                    dreamText = "Early this morning, I saw an ancient Shiva Lingam in my dream with a stream of pure crystal water flowing perpetually over it. The sacred resonance of 'Om Namah Shivaya' echoed all around. Does this indicate a spiritual awakening or sadhana guidance?",
                    tags = listOf("Lord Shiva", "Temple Vision", "Sacred Water", "Sadhana"),
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 45,
                    likeCount = 14,
                    commentCount = 2,
                    isAnonymous = true,
                    topSadhakComment = SadhakCommentSnippet(
                        commentId = "sadhak_cmt_01",
                        userId = "sadhak_acharya_ramesh",
                        authorName = "Acharya Ramesh Shastri",
                        text = "Pranam seeker! Seeing water continuously flowing over the Shiva Lingam is deeply auspicious. It signifies mental tranquility, cleansing of past karmas, and progress in meditation. Consider offering fresh water with gratitude each morning.",
                        timestamp = System.currentTimeMillis() - 1000 * 60 * 20,
                        authorRole = "sadhak"
                    )
                ),
                PublicDreamItem(
                    dreamId = "starter_dream_02",
                    userId = "seeker_arjun_02",
                    authorName = "Arjun K.",
                    dreamText = "I saw myself climbing a steep golden mountain amidst heavy clouds, and at the summit, a radiant sunrise bathed everything in warm amber light.",
                    tags = listOf("Surya", "Mount Kailash", "Spiritual Ascent"),
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 180,
                    likeCount = 9,
                    commentCount = 1,
                    isAnonymous = false,
                    topSadhakComment = SadhakCommentSnippet(
                        commentId = "sadhak_cmt_02",
                        userId = "sadhak_swami_brahmanand",
                        authorName = "Swami Brahmanand",
                        text = "The mountain represents the sushumna nadi and inner spiritual ascent. Emerging into the golden sunrise indicates spiritual awakening and overcoming recent career obstacles.",
                        timestamp = System.currentTimeMillis() - 1000 * 60 * 95,
                        authorRole = "sadhak"
                    )
                ),
                PublicDreamItem(
                    dreamId = "starter_dream_03",
                    userId = "seeker_ananya_03",
                    authorName = "Anonymous",
                    dreamText = "I saw a radiant white cow peacefully entering our courtyard and gently touching the sacred Tulsi plant with affection and warmth.",
                    tags = listOf("Gau Mata", "Holy Tulsi", "Auspicious Vision"),
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 360,
                    likeCount = 22,
                    commentCount = 3,
                    isAnonymous = true,
                    topSadhakComment = SadhakCommentSnippet(
                        commentId = "sadhak_cmt_03",
                        userId = "sadhak_devi_ananya",
                        authorName = "Sadhvi Maa Gayatri",
                        text = "The conjunction of the sacred cow and holy Tulsi signifies divine blessings of prosperity, grace, and harmony within your household. Peace and abundance are arriving.",
                        timestamp = System.currentTimeMillis() - 1000 * 60 * 240,
                        authorRole = "sadhak"
                    )
                )
            )

            starterDreams.forEach { dream ->
                val dreamDoc = dreamsCollection.document(dream.dreamId)
                val payload = hashMapOf<String, Any>(
                    "dreamId" to dream.dreamId,
                    "userId" to dream.userId,
                    "authorName" to dream.authorName,
                    "dreamText" to dream.dreamText,
                    "tags" to dream.tags,
                    "timestamp" to dream.timestamp,
                    "likeCount" to dream.likeCount,
                    "commentCount" to dream.commentCount,
                    "isAnonymous" to dream.isAnonymous
                )
                if (dream.topSadhakComment != null) {
                    payload["topSadhakComment"] = mapOf(
                        "commentId" to dream.topSadhakComment.commentId,
                        "userId" to dream.topSadhakComment.userId,
                        "authorName" to dream.topSadhakComment.authorName,
                        "text" to dream.topSadhakComment.text,
                        "timestamp" to dream.topSadhakComment.timestamp,
                        "authorRole" to "sadhak"
                    )
                }
                dreamDoc.set(payload)

                // Add starter comments in sub-collection
                if (dream.topSadhakComment != null) {
                    dreamDoc.collection("comments").document(dream.topSadhakComment.commentId).set(
                        mapOf(
                            "commentId" to dream.topSadhakComment.commentId,
                            "userId" to dream.topSadhakComment.userId,
                            "authorName" to dream.topSadhakComment.authorName,
                            "text" to dream.topSadhakComment.text,
                            "timestamp" to dream.topSadhakComment.timestamp,
                            "authorRole" to "sadhak"
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Starter dreams seed skipped: ${e.message}")
        }
    }
}
