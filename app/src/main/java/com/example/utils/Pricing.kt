package com.example.utils

import kotlin.math.ceil

/**
 * Shared central pricing and revenue sharing constants for Devbhasha.
 *
 * Core Policies:
 * - Consultation: ₹499 for 20 minutes
 * - Extra time: ₹99 per extra 5 minutes
 * - Text question: ₹51
 * - Dream chat: ₹51 (same as text question)
 * - Dream call: ₹499 for 20 minutes + ₹99 per extra 5 minutes (same as consultation pricing)
 * - Revenue share: Sadhak (Provider) gets 65%, Platform keeps 35%
 * - Referral bonus: ₹100
 */
object Pricing {
    // ----------------------------------------------------
    // 1. Voice Consultation
    // ----------------------------------------------------
    /** Base voice consultation fee in Indian Rupees: ₹499 */
    const val CONSULTATION_PRICE_RUPEES: Int = 499
    /** Base voice consultation fee in paise: 49,900 paise */
    const val CONSULTATION_PRICE_PAISE: Long = 49900L
    /** Base consultation session duration in minutes: 20 minutes */
    const val CONSULTATION_DURATION_MINUTES: Int = 20
    /** Base consultation session duration in seconds: 1,200 seconds */
    const val CONSULTATION_DURATION_SECONDS: Int = 20 * 60

    // Extra minutes billing
    /** Rate for each extra 5 minutes block in Indian Rupees: ₹99 */
    const val EXTRA_5MIN_PRICE_RUPEES: Int = 99
    /** Rate for each extra 5 minutes block in paise: 9,900 paise */
    const val EXTRA_5MIN_PRICE_PAISE: Long = 9900L
    /** Extra block duration in minutes: 5 minutes */
    const val EXTRA_DURATION_MINUTES: Int = 5
    /** Extra block duration in seconds: 300 seconds */
    const val EXTRA_DURATION_SECONDS: Int = 5 * 60

    // ----------------------------------------------------
    // 2. Text Question
    // ----------------------------------------------------
    /** Text question fee in Indian Rupees: ₹51 */
    const val TEXT_QUESTION_PRICE_RUPEES: Int = 51
    /** Text question fee in paise: 5,100 paise */
    const val TEXT_QUESTION_PRICE_PAISE: Long = 5100L

    // ----------------------------------------------------
    // 3. Dream Guidance (Same as consultation / text question pricing)
    // ----------------------------------------------------
    /** Dream chat / text interpretation price in Rupees: ₹199 */
    const val DREAM_CHAT_PRICE_RUPEES: Int = 199
    /** Dream chat / text interpretation price in paise: 19,900 paise */
    const val DREAM_CHAT_PRICE_PAISE: Long = 19900L

    /** Dream voice call base price in Rupees: ₹499 for 20 minutes */
    const val DREAM_CALL_PRICE_RUPEES: Int = CONSULTATION_PRICE_RUPEES
    /** Dream voice call base price in paise: 49,900 paise */
    const val DREAM_CALL_PRICE_PAISE: Long = CONSULTATION_PRICE_PAISE
    /** Dream voice call base duration: 20 minutes */
    const val DREAM_CALL_DURATION_MINUTES: Int = CONSULTATION_DURATION_MINUTES
    /** Dream voice call base duration in seconds: 1,200 seconds */
    const val DREAM_CALL_DURATION_SECONDS: Int = CONSULTATION_DURATION_SECONDS
    /** Dream voice call extra 5 minutes price: ₹99 */
    const val DREAM_CALL_EXTRA_5MIN_RUPEES: Int = EXTRA_5MIN_PRICE_RUPEES
    /** Dream voice call extra 5 minutes price in paise: 9,900 paise */
    const val DREAM_CALL_EXTRA_5MIN_PAISE: Long = EXTRA_5MIN_PRICE_PAISE

    // ----------------------------------------------------
    // 4. Revenue Sharing (65% Sadhak / 35% Platform)
    // ----------------------------------------------------
    /** Sadhak (Provider) revenue share percentage: 65% */
    const val SADHAK_SHARE_PERCENT: Int = 65
    /** Sadhak (Provider) revenue share ratio: 0.65 */
    const val SADHAK_SHARE_RATIO: Double = 0.65

    /** Platform revenue share percentage: 35% */
    const val PLATFORM_SHARE_PERCENT: Int = 35
    /** Platform revenue share ratio: 0.35 */
    const val PLATFORM_SHARE_RATIO: Double = 0.35

    // ----------------------------------------------------
    // 5. Referral Bonus
    // ----------------------------------------------------
    /** Welcome and referral bonus credited in Indian Rupees: ₹100 */
    const val REFERRAL_BONUS_RUPEES: Int = 100
    /** Welcome and referral bonus in paise: 10,000 paise */
    const val REFERRAL_BONUS_PAISE: Long = 10000L

    // ----------------------------------------------------
    // Calculation & Settlement Helpers
    // ----------------------------------------------------
    /**
     * Calculates total session price in integer paise for a voice call duration in seconds.
     * First 20 mins (<=1200s): ₹499 (49,900 paise)
     * Extra time: +₹99 (9,900 paise) per 5 minutes block (300s)
     */
    fun calculateCallSessionPaise(durationSeconds: Long): Long {
        if (durationSeconds <= 0) return CONSULTATION_PRICE_PAISE
        return if (durationSeconds <= CONSULTATION_DURATION_SECONDS) {
            CONSULTATION_PRICE_PAISE
        } else {
            val extraSeconds = durationSeconds - CONSULTATION_DURATION_SECONDS
            val extraBlocks = ceil(extraSeconds.toDouble() / EXTRA_DURATION_SECONDS.toDouble()).toLong()
            CONSULTATION_PRICE_PAISE + (extraBlocks * EXTRA_5MIN_PRICE_PAISE)
        }
    }

    /**
     * Calculates the sadhak's 65% earnings share in paise from the total session price.
     */
    fun calculateSadhakSharePaise(totalPaise: Long): Long {
        return Math.round(totalPaise * SADHAK_SHARE_RATIO)
    }

    /**
     * Calculates the platform's 35% share in paise from the total session price.
     */
    fun calculatePlatformSharePaise(totalPaise: Long): Long {
        val sadhakShare = calculateSadhakSharePaise(totalPaise)
        return totalPaise - sadhakShare
    }
}
