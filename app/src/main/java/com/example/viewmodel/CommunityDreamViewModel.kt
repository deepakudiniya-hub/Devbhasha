package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.CommunityDreamRepository
import com.example.ui.models.DreamCommentItem
import com.example.ui.models.PublicDreamItem
import com.example.utils.UserSession
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CommunityDreamUiState(
    val dreams: List<PublicDreamItem> = emptyList(),
    val isLoading: Boolean = true,
    val isPostingDream: Boolean = false,
    val postSuccessMessage: String? = null,
    val errorMessage: String? = null,
    val activeCommentsDream: PublicDreamItem? = null,
    val activeComments: List<DreamCommentItem> = emptyList(),
    val isLoadingComments: Boolean = false,
    val isPostingComment: Boolean = false,
    val userLikes: Set<String> = emptySet()
)

class CommunityDreamViewModel(
    application: Application,
    private val repository: CommunityDreamRepository
) : AndroidViewModel(application) {

    private val userSession = UserSession(application)
    private val _uiState = MutableStateFlow(CommunityDreamUiState())
    val uiState: StateFlow<CommunityDreamUiState> = _uiState.asStateFlow()

    private var commentsJob: Job? = null

    init {
        val currentUserId = userSession.getUserId()
        observeDreams(currentUserId)
    }

    private fun observeDreams(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.observePublicDreams(userId).collect { dreamList ->
                // Check liked status for visible dreams
                val likedSet = mutableSetOf<String>()
                dreamList.forEach { dream ->
                    if (repository.isDreamLikedByUser(dream.dreamId, userId)) {
                        likedSet.add(dream.dreamId)
                    }
                }

                val enrichedDreams = dreamList.map { dream ->
                    dream.copy(isLikedByMe = likedSet.contains(dream.dreamId))
                }

                _uiState.update { current ->
                    current.copy(
                        dreams = enrichedDreams,
                        userLikes = likedSet,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun postDream(
        dreamText: String,
        isAnonymous: Boolean,
        onSuccess: () -> Unit
    ) {
        val trimmed = dreamText.trim()
        if (trimmed.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please describe your dream") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isPostingDream = true, errorMessage = null) }
            val userId = userSession.getUserId()
            val authorName = if (isAnonymous) "Anonymous" else userSession.getUserName().ifBlank { "Seeker" }

            val result = repository.postPublicDream(
                userId = userId,
                authorName = authorName,
                dreamText = trimmed,
                isAnonymous = isAnonymous
            )

            result.onSuccess {
                _uiState.update {
                    it.copy(
                        isPostingDream = false,
                        postSuccessMessage = "Dream shared with the community"
                    )
                }
                onSuccess()
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isPostingDream = false,
                        errorMessage = err.localizedMessage ?: "Failed to share dream"
                    )
                }
            }
        }
    }

    fun togglePranamLike(dream: PublicDreamItem) {
        val userId = userSession.getUserId()
        val isCurrentlyLiked = _uiState.value.userLikes.contains(dream.dreamId)

        // Optimistic UI update
        val updatedLikes = _uiState.value.userLikes.toMutableSet().apply {
            if (isCurrentlyLiked) remove(dream.dreamId) else add(dream.dreamId)
        }
        val updatedDreams = _uiState.value.dreams.map {
            if (it.dreamId == dream.dreamId) {
                it.copy(
                    isLikedByMe = !isCurrentlyLiked,
                    likeCount = if (isCurrentlyLiked) maxOf(0, it.likeCount - 1) else it.likeCount + 1
                )
            } else it
        }

        _uiState.update { it.copy(dreams = updatedDreams, userLikes = updatedLikes) }

        viewModelScope.launch {
            val result = repository.togglePranamLike(dream.dreamId, userId, isCurrentlyLiked)
            result.onFailure {
                // Revert on failure
                val revertedLikes = _uiState.value.userLikes.toMutableSet().apply {
                    if (isCurrentlyLiked) add(dream.dreamId) else remove(dream.dreamId)
                }
                val revertedDreams = _uiState.value.dreams.map {
                    if (it.dreamId == dream.dreamId) {
                        it.copy(isLikedByMe = isCurrentlyLiked, likeCount = dream.likeCount)
                    } else it
                }
                _uiState.update { it.copy(dreams = revertedDreams, userLikes = revertedLikes) }
            }
        }
    }

    fun openCommentsSheet(dream: PublicDreamItem) {
        commentsJob?.cancel()
        _uiState.update {
            it.copy(
                activeCommentsDream = dream,
                isLoadingComments = true,
                activeComments = emptyList()
            )
        }

        commentsJob = viewModelScope.launch {
            repository.observeComments(dream.dreamId).collect { comments ->
                _uiState.update {
                    it.copy(
                        activeComments = comments,
                        isLoadingComments = false
                    )
                }
            }
        }
    }

    fun closeCommentsSheet() {
        commentsJob?.cancel()
        _uiState.update { it.copy(activeCommentsDream = null, activeComments = emptyList()) }
    }

    fun postComment(
        text: String,
        authorRole: String = "user",
        onSuccess: () -> Unit
    ) {
        val dream = _uiState.value.activeCommentsDream ?: return
        val trimmed = text.trim()
        if (trimmed.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isPostingComment = true) }
            val userId = userSession.getUserId()
            val authorName = if (authorRole == "sadhak") {
                "Verified Sadhak"
            } else {
                userSession.getUserName().ifBlank { "Seeker" }
            }

            val result = repository.postComment(
                dreamId = dream.dreamId,
                userId = userId,
                authorName = authorName,
                text = trimmed,
                authorRole = authorRole
            )

            result.onSuccess {
                _uiState.update { it.copy(isPostingComment = false) }
                onSuccess()
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isPostingComment = false,
                        errorMessage = err.localizedMessage ?: "Failed to post comment"
                    )
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(postSuccessMessage = null, errorMessage = null) }
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val repo = CommunityDreamRepository.getInstance(application)
            return CommunityDreamViewModel(application, repo) as T
        }
    }
}
