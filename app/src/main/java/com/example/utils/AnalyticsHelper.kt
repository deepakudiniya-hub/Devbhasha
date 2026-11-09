package com.example.utils

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

/**
 * Centralized, privacy-safe analytics helper for Devbhasha.
 *
 * Logged events strictly adhere to user-specified whitelist:
 * - screen_view (handled via MainActivity Compose observer)
 * - wallet_recharge_success (zero amount / zero PII)
 * - session_start (type only: "chat", "call", "session", "dream_chat", "dream_call", "text")
 * - session_end
 * - free_dream_chat_used
 *
 * Absolutely NO PII (name, phone, email), exact rupee amounts, or message text is logged.
 */
object AnalyticsHelper {
    @Volatile
    private var analytics: FirebaseAnalytics? = null

    fun init(context: Context) {
        if (analytics == null) {
            synchronized(this) {
                if (analytics == null) {
                    analytics = FirebaseAnalytics.getInstance(context.applicationContext)
                }
            }
        }
    }

    fun logWalletRechargeSuccess() {
        analytics?.logEvent("wallet_recharge_success", Bundle())
    }

    fun logSessionStart(type: String) {
        val bundle = Bundle().apply {
            putString("type", type.lowercase())
        }
        analytics?.logEvent("session_start", bundle)
    }

    fun logSessionEnd() {
        analytics?.logEvent("session_end", Bundle())
    }

    fun logFreeDreamChatUsed() {
        analytics?.logEvent("free_dream_chat_used", Bundle())
    }
}
