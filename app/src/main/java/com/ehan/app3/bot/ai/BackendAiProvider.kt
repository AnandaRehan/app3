package com.ehan.app3.bot.ai

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class BackendAiProvider(
    private val baseUrl: String
) : AiProvider {

    override val name = "backend"

    private val api: AiProxyApi =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .build()
            .create(AiProxyApi::class.java)

    override suspend fun ask(prompt: String): String {
        return try {
            val response =
                api.ask(
                    AiProxyRequest(
                        prompt = prompt
                    )
                )

            response.text
                ?: response.error
                ?: "AI tidak memberikan respons."

        } catch (exception: Exception) {
            "❌ Gagal menghubungi server AI."
        }
    }
}