package com.example.data.api

import android.util.Log
import com.google.firebase.functions.FirebaseFunctions

/**
 * Firebase-native Gemini content generation.
 * Calls the Cloud Function "askGemini" which uses the Gemini SDK server-side.
 * This avoids shipping API keys in the app and keeps sensitive logic on the server.
 */
class GeminiService {
    private val functions: FirebaseFunctions = FirebaseFunctions.getInstance()
    private const val TAG = "GeminiService"

    suspend fun generateContent(userPrompt: String): String {
        return try {
            val data = mapOf(
                "prompt" to userPrompt
            )
            val result = functions
                .getHttpsCallable("askGemini")
                .call(data)
                .await()
            
            @Suppress("UNCHECKED_CAST")
            val resultData = result.data as? Map<String, Any>
            val response = resultData?.get("result") as? String
            
            if (response.isNullOrBlank()) {
                Log.w(TAG, "Empty response from askGemini")
                "त्रुटि: सर्वर से कोई प्रतिक्रिया नहीं"
            } else {
                response
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to generate content", e)
            "त्रुटि: ${e.localizedMessage ?: "Content generation failed"}"
        }
    }
}
