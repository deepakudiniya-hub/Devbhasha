package com.example.utils

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserSessionViewModel(application: Application) : AndroidViewModel(application) {
    private val userSession = UserSession(application)

    private val _avatarId = MutableStateFlow(userSession.getAvatarId())
    val avatarId: StateFlow<String> = _avatarId.asStateFlow()

    private val _userName = MutableStateFlow(userSession.getUserName())
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(userSession.isLoggedIn())
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    fun updateAvatarId(newAvatarId: String) {
        userSession.setAvatarId(newAvatarId)
        _avatarId.value = userSession.getAvatarId()
        globalAvatarIdFlow.value = userSession.getAvatarId()
    }

    fun updateUserName(name: String) {
        val uid = userSession.getUserId()
        userSession.saveUserSession(name, userSession.isLoggedIn(), uid, userSession.getPhoneNumber())
        _userName.value = userSession.getUserName()
    }

    fun refreshSession() {
        _avatarId.value = userSession.getAvatarId()
        _userName.value = userSession.getUserName()
        _isLoggedIn.value = userSession.isLoggedIn()
        globalAvatarIdFlow.value = userSession.getAvatarId()
    }

    companion object {
        val globalAvatarIdFlow = MutableStateFlow("avatar_01")
    }
}
