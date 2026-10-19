package com.example.data.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

// English comment placeholder
data class GeminiRequest(val prompt: String)
data class GeminiResponse(val result: String)

// English comment placeholder
interface GeminiApi {
    @POST("askGeminiServer")
    suspend fun askGemini(@Body request: GeminiRequest): GeminiResponse
}

// English comment placeholder
class GeminiService {

    private val api: GeminiApi

    init {
// English comment placeholder
        val retrofit = Retrofit.Builder()
            .baseUrl("https://cloudfunctions.net")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(GeminiApi::class.java)
    }

// English comment placeholder
    suspend fun generateContent(userPrompt: String): String {
        return try {
            val response = api.askGemini(GeminiRequest(userPrompt))
            response.result // Server response
        } catch (e: Exception) {
            "Error: ${e.localizedMessage}"
        }
    }
}
