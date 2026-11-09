package com.example.utils

import android.content.Context
import android.content.Intent
import android.net.Uri

object SafetyAndCrisisManager {

    // Small curated list of self-harm / crisis terms in English & Hindi / Hinglish
    val SELF_HARM_KEYWORDS = listOf(
        "suicide",
        "end my life",
        "kill myself",
        "want to die",
        "marna chahta",
        "marna chahti",
        "jeena nahi",
        "jaan dena",
        "atmahatya",
        "khatam karna chahta",
        "khatam karna chahti",
        "give up on life"
    )

    val CONTACT_KEYWORDS = listOf("whatsapp", "telegram", "wa.me", "t.me")
    val PHONE_REGEX = Regex("(?<!\\d)\\d{10}(?!\\d)")
    val UPI_REGEX = Regex("[a-zA-Z0-9.\\-_]{2,256}@[a-zA-Z]{2,64}")

    fun containsSelfHarmRisk(text: String): Boolean {
        val lower = text.lowercase()
        return SELF_HARM_KEYWORDS.any { lower.contains(it) }
    }

    data class ContactSanitizationResult(
        val hasContactInfo: Boolean,
        val sanitizedText: String,
        val warningMessage: String?
    )

    fun sanitizeContactSharing(text: String): ContactSanitizationResult {
        val lower = text.lowercase()
        val hasPhone = PHONE_REGEX.containsMatchIn(text)
        val hasUpi = UPI_REGEX.containsMatchIn(text)
        val hasSocial = CONTACT_KEYWORDS.any { lower.contains(it) }

        if (!hasPhone && !hasUpi && !hasSocial) {
            return ContactSanitizationResult(false, text, null)
        }

        // Mask 10-digit numbers with "••••"
        val masked = PHONE_REGEX.replace(text, "••••••••••")

        return ContactSanitizationResult(
            hasContactInfo = true,
            sanitizedText = masked,
            warningMessage = "For your safety, keep chat and payments inside the app."
        )
    }

    fun callTeleManas(context: Context) {
        try {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:14416"))
            context.startActivity(intent)
        } catch (_: Exception) {
            try {
                val fallbackIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:18008914416"))
                context.startActivity(fallbackIntent)
            } catch (_: Exception) {}
        }
    }
}
