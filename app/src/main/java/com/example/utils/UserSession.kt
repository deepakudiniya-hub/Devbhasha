package com.example.utils

import android.content.Context
import android.content.SharedPreferences

class UserSession(context: Context) {
    private val prefs: SharedPreferences = 
        context.getSharedPreferences("devbhasha_user_prefs", Context.MODE_PRIVATE)

    // यूज़र का नाम, लॉगिन स्टेट और आईडी सेव करने के लिए
    fun saveUserSession(userName: String, isLoggedIn: Boolean, userId: String, phoneNumber: String = "") {
        prefs.edit()
            .putString("user_name", userName)
            .putBoolean("is_logged_in", isLoggedIn)
            .putString("user_id", userId)
            .putString("phone_number", phoneNumber)
            .apply()
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

    fun getWalletBalance(): Double {
        // FIX: default to 0 (no free local credit). Recharge must go through
        // Razorpay, consistent with the wallet-integrity fix on main.
        return prefs.getFloat("wallet_balance", 0.0f).toDouble()
    }

    fun setWalletBalance(balance: Double) {
        prefs.edit().putFloat("wallet_balance", balance.toFloat()).apply()
    }

    // लॉग आउट करने के लिए
    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
