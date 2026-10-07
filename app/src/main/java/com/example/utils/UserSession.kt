package com.example.utils

import android.content.Context
import android.content.SharedPreferences

class UserSession(context: Context) {
    private val prefs: SharedPreferences = 
        context.getSharedPreferences("devbhasha_user_prefs", Context.MODE_PRIVATE)

    // यूज़र का नाम, लॉगिन स्टेट और आईडी सेव करने के लिए
    fun saveUserSession(userName: String, isLoggedIn: Boolean, userId: String, phoneNumber: String = "") {
        saveUserSessionWithRole(userName, isLoggedIn, userId, phoneNumber, "user")
    }

    fun saveUserSessionWithRole(userName: String, isLoggedIn: Boolean, userId: String, phoneNumber: String = "", role: String = "user") {
        prefs.edit()
            .putString("user_name", userName)
            .putBoolean("is_logged_in", isLoggedIn)
            .putString("user_id", userId)
            .putString("phone_number", phoneNumber)
            .putString("user_role", role)
            .apply()
    }

    fun getUserRole(): String {
        return prefs.getString("user_role", "user") ?: "user"
    }

    fun setUserRole(role: String) {
        prefs.edit().putString("user_role", role).apply()
    }

    fun isProvider(): Boolean {
        return getUserRole().equals("provider", ignoreCase = true)
    }

    // सेव किया हुआ नाम प्राप्त करने के लिए
    fun getUserName(): String {
        val saved = prefs.getString("user_name", "")?.trim() ?: ""
        if (saved.isNotBlank() && 
            !saved.equals("null", ignoreCase = true) && 
            !saved.contains("Facebook", ignoreCase = true) && 
            saved != "साधक") {
            return saved
        }
        return "दीपक जी"
    }

    // सेव की गई यूज़र आईडी प्राप्त करने के लिए
    fun getUserId(): String {
        val saved = prefs.getString("user_id", "") ?: ""
        if (saved.isNotBlank()) return saved
        val newId = "user_" + (100000..999999).random()
        prefs.edit().putString("user_id", newId).apply()
        return newId
    }

    // फ़ोन नंबर प्राप्त करने के लिए
    fun getPhoneNumber(): String {
        val saved = prefs.getString("phone_number", "") ?: ""
        return if (saved.isNotBlank() && saved != "9876543210") saved else ""
    }

    // क्या यूज़र लॉग इन है?
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean("is_logged_in", false)
    }

    // Supported languages: "en", "hi", "hgl"
    fun getLanguage(): String {
        return prefs.getString("user_language", "hi") ?: "hi"
    }

    fun setLanguage(lang: String) {
        prefs.edit().putString("user_language", lang).apply()
        val uid = getUserId()
        if (uid.isNotBlank()) {
            UserManager.updateLanguagePreference(uid, lang)
        }
    }

    fun syncLanguage(uid: String, onComplete: (() -> Unit)? = null) {
        onComplete?.invoke()
    }

    fun isHindi(): Boolean {
        return getLanguage() == "hi"
    }

    // DEPRECATED — the wallet is server-authoritative now (see WalletRepository
    // and functions/index.js). The balance lives in Firestore
    // (`users/{uid}.walletBalance`, in paise) and is written only by Cloud
    // Functions. The device must never own or mutate real money.
    @Deprecated("Balance is server-owned; use WalletRepository.observeBalance()")
    fun getWalletBalance(): Double = getCachedWalletBalance()

    @Deprecated("Balance is server-owned; the app must never set it.")
    @Suppress("UNUSED_PARAMETER")
    fun setWalletBalance(balance: Double) {
        // no-op by design — money is never mutated on the device
    }

    fun getCachedWalletBalance(): Double {
        return prefs.getFloat("cached_wallet_balance", 100.0f).toDouble()
    }

    fun setCachedWalletBalance(balance: Double) {
        prefs.edit().putFloat("cached_wallet_balance", balance.toFloat()).apply()
    }

    // चयनित अवतार आईडी
    fun getAvatarId(): String {
        return prefs.getString("user_avatar_id", "sadhak_dhyan") ?: "sadhak_dhyan"
    }

    fun setAvatarId(avatarId: String) {
        prefs.edit().putString("user_avatar_id", avatarId).apply()
        val uid = getUserId()
        if (uid.isNotBlank()) {
            UserManager.updateAvatarPreference(uid, avatarId)
        }
    }

    // लॉग आउट करने के लिए
    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
