package com.example.utils

/**
 * Shared central pricing constants for Devbhasha.
 *
 * - Text question: ₹51
 * - Dream chat: ₹199 for 5 minutes
 * - Dream call: ₹299 for 5 minutes
 * - Referral bonus: ₹100
 *
 * Real consultation session rates come directly from the Sadhak's profile:
 * - CHAT_PER_MIN (chatPricePerMin: ₹5 - ₹100/min)
 * - CALL_PER_MIN (callPricePerMin: ₹5 - ₹100/min)
 * - SESSION_30 (sessionPrice30Min: ₹199 - ₹2,999 for 30 min)
 */
object Pricing {
    // ----------------------------------------------------
    // 1. Text Question
    // ----------------------------------------------------
    /** Text question fee in Indian Rupees: ₹51 */
    const val TEXT_QUESTION_PRICE_RUPEES: Int = 51
    /** Text question fee in paise: 5,100 paise */
    const val TEXT_QUESTION_PRICE_PAISE: Long = 5100L

    // ----------------------------------------------------
    // 2. Dream Guidance (Fixed Platform Prices)
    // ----------------------------------------------------
    /** Dream chat / text interpretation price in Rupees: ₹199 */
    const val DREAM_CHAT_PRICE_RUPEES: Int = 199
    /** Dream chat / text interpretation price in paise: 19,900 paise */
    const val DREAM_CHAT_PRICE_PAISE: Long = 19900L
    const val DREAM_CHAT_DURATION_MINUTES: Int = 5

    /** Dream voice call base price in Rupees: ₹299 for 5 minutes */
    const val DREAM_CALL_PRICE_RUPEES: Int = 299
    /** Dream voice call base price in paise: 29,900 paise */
    const val DREAM_CALL_PRICE_PAISE: Long = 29900L
    /** Dream voice call base duration: 5 minutes */
    const val DREAM_CALL_DURATION_MINUTES: Int = 5
    /** Dream voice call base duration in seconds: 300 seconds */
    const val DREAM_CALL_DURATION_SECONDS: Int = 5 * 60

    // ----------------------------------------------------
    // 3. Referral Bonus
    // ----------------------------------------------------
    /** Welcome and referral bonus credited in Indian Rupees: ₹100 */
    const val REFERRAL_BONUS_RUPEES: Int = 100
    /** Welcome and referral bonus in paise: 10,000 paise */
    const val REFERRAL_BONUS_PAISE: Long = 10000L
}
