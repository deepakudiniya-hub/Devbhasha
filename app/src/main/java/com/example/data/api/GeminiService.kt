package com.example.data.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

// 1. डेटा मॉडल जो सर्वर को डेटा भेजने और पाने के लिए काम आएंगे
data class GeminiRequest(val prompt: String)
data class GeminiResponse(val result: String)

// 2. Retrofit इंटरफ़ेस जो सीधे आपके नए सुरक्षित सर्वर से बात करेगा
interface GeminiApi {
    @POST("askGeminiServer")
    suspend fun askGemini(@Body request: GeminiRequest): GeminiResponse
}

// 3. आपकी मुख्य क्लास (GeminiService)
class GeminiService {

    private val api: GeminiApi

    init {
        // यहाँ हमने आपका बिल्कुल नया लाइव सर्वर लिंक जोड़ दिया है
        val retrofit = Retrofit.Builder()
            .baseUrl("https://cloudfunctions.net")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(GeminiApi::class.java)
    }

    // ऐप में जहाँ से भी आप जेमिनी को कॉल करते थे, बस इस फ़ंक्शन को कॉल करना है
    suspend fun generateContent(userPrompt: String): String {
        return try {
            val response = api.askGemini(GeminiRequest(userPrompt))
            response.result // सर्वर से आया हुआ जवाब
        } catch (e: Exception) {
            "त्रुटि: ${e.localizedMessage}"
        }
    }
}
