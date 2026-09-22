package com.ehan.app3.bot.ai

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class OpenAiClient(
    private val endpoint: String
) {

    private val api: OpenAiApi =
        Retrofit.Builder()
            .baseUrl(endpoint)
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .build()
            .create(OpenAiApi::class.java)

    suspend fun ask(
        apiKey: String,
        model: String,
        prompt: String
    ): String {

        return try {
            val response = api.createResponse(
                authorization = "Bearer $apiKey",
                request = OpenAiRequest(
                    model = model,
                    input = prompt
                )
            )

            response.output_text
                ?: "AI tidak mengembalikan jawaban."

        } catch (exception: Exception) {
            "❌ Gagal menghubungi AI: ${exception.message ?: "Unknown error"}"
        }
    }
}