package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ui.models.SadhakItem
import com.example.utils.FirestoreProvider
import com.example.utils.SadhakRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ProviderViewModel manages the sadhaks / spiritual experts directory,
 * real-time online status observation, search and category filtering,
 * user favorites, waitlist notifications, and consultation session initiation.
 */
class ProviderViewModel(application: Application) : AndroidViewModel(application) {

    private val db get() = FirestoreProvider.get(getApplication())

    private val _sadhaks = MutableStateFlow<List<SadhakItem>>(emptyList())
    val sadhaks: StateFlow<List<SadhakItem>> = _sadhaks.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow("all")
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

    private val _selectedCategory = MutableStateFlow("all")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _favorites = MutableStateFlow<Set<String>>(emptySet())
    val favorites: StateFlow<Set<String>> = _favorites.asStateFlow()

    private val _waitlisted = MutableStateFlow<Set<String>>(emptySet())
    val waitlisted: StateFlow<Set<String>> = _waitlisted.asStateFlow()

    // Active Consultation States
    private val _activeChatSadhak = MutableStateFlow<SadhakItem?>(null)
    val activeChatSadhak: StateFlow<SadhakItem?> = _activeChatSadhak.asStateFlow()

    private val _activeChatSessionId = MutableStateFlow<String?>(null)
    val activeChatSessionId: StateFlow<String?> = _activeChatSessionId.asStateFlow()

    private val _activeCallingSadhak = MutableStateFlow<SadhakItem?>(null)
    val activeCallingSadhak: StateFlow<SadhakItem?> = _activeCallingSadhak.asStateFlow()

    private val _activeCallingCallId = MutableStateFlow<String?>(null)
    val activeCallingCallId: StateFlow<String?> = _activeCallingCallId.asStateFlow()

    // Filtered Sadhaks StateFlow
    val filteredSadhaks: StateFlow<List<SadhakItem>> = combine(
        _sadhaks,
        _searchQuery,
        _selectedFilter,
        _selectedCategory
    ) { list, query, filter, cat ->
        list.filter { sadhak ->
            val matchesQuery = query.isBlank() ||
                sadhak.nameHi.contains(query, ignoreCase = true) ||
                sadhak.nameEn.contains(query, ignoreCase = true) ||
                sadhak.titleHi.contains(query, ignoreCase = true) ||
                sadhak.titleEn.contains(query, ignoreCase = true)

            val matchesFilter = when (filter) {
                "online" -> sadhak.isOnline
                "4+" -> (sadhak.rating.toDoubleOrNull() ?: 4.0) >= 4.0
                "4.9+" -> (sadhak.rating.toDoubleOrNull() ?: 4.0) >= 4.9
                "top" -> (sadhak.rating.toDoubleOrNull() ?: 0.0) >= 4.8
                "vedic" -> sadhak.titleHi.contains("वैदिक", true) || sadhak.titleEn.contains("Vedic", true)
                "vastu" -> sadhak.titleHi.contains("वास्तु", true) || sadhak.titleEn.contains("Vastu", true)
                "tarot" -> sadhak.titleHi.contains("टैरो", true) || sadhak.titleEn.contains("Tarot", true)
                else -> true
            }

            val matchesCategory = when (cat) {
                "all" -> true
                "online" -> sadhak.isOnline
                "kundli" -> sadhak.titleHi.contains("कुंडली", true) || sadhak.titleHi.contains("ज्योतिष", true)
                "marriage" -> sadhak.titleHi.contains("विवाह", true) || sadhak.bio.contains("विवाह", true)
                "career" -> sadhak.titleHi.contains("कैरियर", true) || sadhak.bio.contains("व्यापार", true)
                "vastu" -> sadhak.titleHi.contains("वास्तु", true)
                else -> true
            }

            matchesQuery && matchesFilter && matchesCategory
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private var listenerRegistration: ListenerRegistration? = null

    init {
        startObservingSadhaks()
    }

    fun startObservingSadhaks() {
        listenerRegistration?.remove()
        _isLoading.value = true
        listenerRegistration = SadhakRepository.observeOnlineSadhaks(
            context = getApplication(),
            onUpdate = { items ->
                _sadhaks.value = items
                _isLoading.value = false
            },
            onError = {
                _isLoading.value = false
            }
        )
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedFilter(filter: String) {
        _selectedFilter.value = filter
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun toggleFavorite(sadhakId: String, userId: String) {
        val current = _favorites.value.toMutableSet()
        val isNowFav = if (current.contains(sadhakId)) {
            current.remove(sadhakId)
            false
        } else {
            current.add(sadhakId)
            true
        }
        _favorites.value = current

        // Persist to user's favorites in Firestore if signed in
        if (userId.isNotBlank()) {
            val userFavRef = db.collection("users").document(userId).collection("favorites").document(sadhakId)
            if (isNowFav) {
                userFavRef.set(
                    mapOf(
                        "sadhakId" to sadhakId,
                        "createdAt" to FieldValue.serverTimestamp()
                    ),
                    SetOptions.merge()
                )
            } else {
                userFavRef.delete()
            }
        }
    }

    fun isFavorite(sadhakId: String): Boolean {
        return _favorites.value.contains(sadhakId)
    }

    fun toggleWaitlist(
        sadhak: SadhakItem,
        userId: String,
        isHindi: Boolean,
        onResult: (Boolean, String) -> Unit
    ) {
        val current = _waitlisted.value.toMutableSet()
        if (current.contains(sadhak.id)) {
            current.remove(sadhak.id)
            _waitlisted.value = current
            val msg = if (isHindi) "${sadhak.nameHi} की वेटलिस्ट से हटा दिया गया" else "Removed from ${sadhak.nameEn}'s waitlist"
            onResult(false, msg)
        } else {
            current.add(sadhak.id)
            _waitlisted.value = current
            val waitlistDoc = db.collection("waitlist").document("${sadhak.id}_$userId")
            waitlistDoc.set(
                mapOf(
                    "sadhakId" to sadhak.id,
                    "sadhakName" to sadhak.nameEn,
                    "userId" to userId,
                    "requestedAt" to FieldValue.serverTimestamp(),
                    "status" to "WAITING"
                ),
                SetOptions.merge()
            )
            val msg = if (isHindi) "${sadhak.nameHi} के ऑनलाइन आते ही सूचना दी जाएगी ✓" else "You will be alerted when ${sadhak.nameEn} comes online ✓"
            onResult(true, msg)
        }
    }

    fun isWaitlisted(sadhakId: String): Boolean {
        return _waitlisted.value.contains(sadhakId)
    }

    fun startConsultation(
        sadhak: SadhakItem,
        type: String, // "chat" or "call"
        userId: String,
        onStarted: (sessionId: String) -> Unit,
        onError: (String) -> Unit = {}
    ) {
        val authUid = FirebaseAuth.getInstance().currentUser?.uid
        val uid = authUid ?: userId.ifBlank { "user_${System.currentTimeMillis()}" }

        if (type == "chat") {
            val sessionDoc = db.collection("sessions").document()
            val newSessionId = sessionDoc.id
            val sessionPayload = hashMapOf<String, Any>(
                "sessionId" to newSessionId,
                "userId" to uid,
                "providerId" to sadhak.id,
                "type" to "chat",
                "status" to "REQUESTED",
                "createdAt" to FieldValue.serverTimestamp()
            )
            sessionDoc.set(sessionPayload)
            db.collection("consultation_requests").document(newSessionId).set(
                hashMapOf(
                    "requestId" to newSessionId,
                    "userId" to uid,
                    "providerId" to sadhak.id,
                    "type" to "chat",
                    "status" to "REQUESTED",
                    "createdAt" to FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
            )
            _activeChatSessionId.value = newSessionId
            _activeChatSadhak.value = sadhak
            onStarted(newSessionId)
        } else {
            val callDoc = db.collection("active_calls").document()
            val newCallId = callDoc.id
            val callData = hashMapOf<String, Any>(
                "callId" to newCallId,
                "userId" to uid,
                "callerId" to uid,
                "providerId" to sadhak.id,
                "status" to "CALLING",
                "createdAt" to FieldValue.serverTimestamp()
            )
            callDoc.set(callData).addOnFailureListener {
                val fallbackData = hashMapOf<String, Any>(
                    "callId" to newCallId,
                    "userId" to uid,
                    "callerId" to uid,
                    "providerId" to sadhak.id,
                    "status" to "ringing",
                    "createdAt" to FieldValue.serverTimestamp()
                )
                callDoc.set(fallbackData).addOnSuccessListener {
                    callDoc.update("status", "CALLING")
                }
            }
            db.collection("consultation_requests").document(newCallId).set(
                hashMapOf(
                    "requestId" to newCallId,
                    "userId" to uid,
                    "providerId" to sadhak.id,
                    "type" to "call",
                    "status" to "REQUESTED",
                    "createdAt" to FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
            )
            _activeCallingCallId.value = newCallId
            _activeCallingSadhak.value = sadhak
            onStarted(newCallId)
        }
    }

    fun endActiveChat() {
        _activeChatSadhak.value = null
        _activeChatSessionId.value = null
    }

    fun endActiveCall() {
        val cid = _activeCallingCallId.value
        if (!cid.isNullOrBlank()) {
            db.collection("active_calls").document(cid).update("status", "ENDED")
        }
        _activeCallingSadhak.value = null
        _activeCallingCallId.value = null
    }

    fun setActiveChat(sadhak: SadhakItem?, sessionId: String?) {
        _activeChatSadhak.value = sadhak
        _activeChatSessionId.value = sessionId
    }

    fun setActiveCall(sadhak: SadhakItem?, callId: String?) {
        _activeCallingSadhak.value = sadhak
        _activeCallingCallId.value = callId
    }

    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }
}
