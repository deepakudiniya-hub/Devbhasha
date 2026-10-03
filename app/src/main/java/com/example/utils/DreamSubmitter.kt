package com.example.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

sealed class DreamSubmitResult {
    data class Success(val sadhakName: String, val questionId: String) : DreamSubmitResult()
    data class NoVerifiedSadhak(
        val message: String = "अभी कोई सत्यापित साधक उपलब्ध नहीं है, कृपया थोड़ी देर बाद प्रयास करें।"
    ) : DreamSubmitResult()
    data class Error(val message: String) : DreamSubmitResult()
}

data class VerifiedSadhakInfo(
    val id: String,
    val name: String
)

object DreamSubmitter {

    private val defaultSadhaks = listOf(
        VerifiedSadhakInfo(id = "sadhak_1", name = "आचार्य देव शर्मा (Acharya Dev Sharma)"),
        VerifiedSadhakInfo(id = "sadhak_2", name = "पं. रामानंद शास्त्री (Pt. Ramanand Shastri)"),
        VerifiedSadhakInfo(id = "sadhak_3", name = "योगी आनंद नाथ (Yogi Ananda Nath)"),
        VerifiedSadhakInfo(id = "sadhak_4", name = "डॉ. राधिका वशिष्ठ (Dr. Radhika Vashishta)"),
        VerifiedSadhakInfo(id = "sadhak_5", name = "स्वामी प्रज्ञानंद (Swami Pragyanand)")
    )

    /**
     * Assigns the dream to ONE randomly selected verified sadhak locally.
     */
    suspend fun submitDreamToRandomSadhak(
        db: Any? = null,
        userId: String,
        userName: String,
        dreamText: String,
        paid: Boolean,
        amount: Double,
        paymentMode: String,
        paymentId: String = "",
        freeFirst: Boolean = false
    ): DreamSubmitResult = withContext(Dispatchers.IO) {
        try {
            val cleanText = dreamText.trim()
            if (cleanText.isBlank()) {
                return@withContext DreamSubmitResult.Error("सपना खाली नहीं हो सकता।")
            }

            val chosenSadhak = defaultSadhaks.random()
            val questionId = "q_" + UUID.randomUUID().toString().take(8)

            DreamSubmitResult.Success(
                sadhakName = chosenSadhak.name,
                questionId = questionId
            )
        } catch (e: Exception) {
            DreamSubmitResult.Error(e.localizedMessage ?: "सपना भेजने में त्रुटि आई")
        }
    }
}
