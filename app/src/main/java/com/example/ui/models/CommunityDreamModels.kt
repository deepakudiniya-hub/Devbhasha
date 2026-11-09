package com.example.ui.models

import androidx.annotation.Keep

@Keep
data class SadhakCommentSnippet(
    val commentId: String = "",
    val userId: String = "",
    val authorName: String = "",
    val text: String = "",
    val timestamp: Long = 0L,
    val authorRole: String = "sadhak"
)

@Keep
data class PublicDreamItem(
    val dreamId: String = "",
    val userId: String = "",
    val authorName: String = "Anonymous",
    val dreamText: String = "",
    val tags: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis(),
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val isAnonymous: Boolean = false,
    val topSadhakComment: SadhakCommentSnippet? = null,
    val isLikedByMe: Boolean = false
)

@Keep
data class DreamCommentItem(
    val commentId: String = "",
    val userId: String = "",
    val authorName: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val authorRole: String = "user" // "user" or "sadhak"
)
