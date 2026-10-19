package com.example.utils

import android.content.Context
import android.content.SharedPreferences

class UserSession(context: Context) {
    private val prefs: SharedPreferences = 
        context.getSharedPreferences("devbhasha_user_prefs", Context.MODE_PRIVATE)

// English comment placeholder
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

// English comment placeholder
    fun getUserName(): String {
        val saved = prefs.getString("user_name", "")?.trim() ?: ""
        if (saved.isNotBlank() && 
            !saved.equals("null", ignoreCase = true) && 
            !saved.contains("Facebook", ignoreCase = true) && 
            saved != "Sadhak") {
            return saved
        }
        return "Devotee"
    }

// English comment placeholder
    fun getUserId(): String {
        val saved = prefs.getString("user_id", "") ?: ""
        if (saved.isNotBlank()) return saved
        val newId = "user_" + (100000..999999).random()
        prefs.edit().putString("user_id", newId).apply()
        return newId
    }

// English comment placeholder
    fun getPhoneNumber(): String {
        val saved = prefs.getString("phone_number", "") ?: ""
        return if (saved.isNotBlank() && saved != "9876543210") saved else ""
    }

// English comment placeholder
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean("is_logged_in", false)
    }

    // Supported languages: "en", "hi", "sa"
    fun getLanguage(): String {
        return prefs.getString("user_language", "en") ?: "en"
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

// English comment placeholder
    fun getAvatarId(): String {
        val saved = prefs.getString("user_avatar_id", "avatar_01") ?: "avatar_01"
        return if (saved.startsWith("avatar_")) saved else "avatar_01"
    }

    fun setAvatarId(avatarId: String) {
        val validAvatarId = if (avatarId.startsWith("avatar_")) avatarId else "avatar_01"
        prefs.edit().putString("user_avatar_id", validAvatarId).apply()
        val uid = getUserId()
        if (uid.isNotBlank()) {
            UserManager.updateAvatarPreference(uid, validAvatarId)
        }
    }

    // DPDP Consent Methods
    fun hasGivenConsent(uid: String = getUserId()): Boolean {
        return prefs.getBoolean("has_given_consent_$uid", false) || prefs.getBoolean("has_given_consent", false)
    }

    fun setGivenConsent(uid: String = getUserId(), hasGiven: Boolean = true) {
        prefs.edit()
            .putBoolean("has_given_consent_$uid", hasGiven)
            .putBoolean("has_given_consent", hasGiven)
            .apply()
    }

    fun getBirthDate(): String = prefs.getString("birth_date", "") ?: ""
    fun setBirthDate(dob: String) = prefs.edit().putString("birth_date", dob).apply()

    fun getBirthTime(): String = prefs.getString("birth_time", "") ?: ""
    fun setBirthTime(time: String) = prefs.edit().putString("birth_time", time).apply()

    fun getBirthPlace(): String = prefs.getString("birth_place", "") ?: ""
    fun setBirthPlace(place: String) = prefs.edit().putString("birth_place", place).apply()

    fun getGotra(): String = prefs.getString("gotra", "") ?: ""
    fun setGotra(gotra: String) = prefs.edit().putString("gotra", gotra).apply()

// English comment placeholder
    fun getFavorites(): Set<String> {
        return prefs.getStringSet("favorite_sadhaks", emptySet()) ?: emptySet()
    }

    fun toggleFavorite(sadhakId: String): Boolean {
        val set = getFavorites().toMutableSet()
        val isFav = if (set.contains(sadhakId)) {
            set.remove(sadhakId)
            false
        } else {
            set.add(sadhakId)
            true
        }
        prefs.edit().putStringSet("favorite_sadhaks", set).apply()
        return isFav
    }

    fun isFavorite(sadhakId: String): Boolean {
        return getFavorites().contains(sadhakId)
    }

    fun getSelectedRashi(): String {
        return prefs.getString("selected_rashi", "mesh") ?: "mesh"
    }

    fun setSelectedRashi(rashi: String) {
        prefs.edit().putString("selected_rashi", rashi).apply()
    }

    fun getWaitlist(): Set<String> {
        return prefs.getStringSet("waitlist_sadhaks", emptySet()) ?: emptySet()
    }

    fun addToWaitlist(sadhakId: String) {
        val set = getWaitlist().toMutableSet()
        set.add(sadhakId)
        prefs.edit().putStringSet("waitlist_sadhaks", set).apply()
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
