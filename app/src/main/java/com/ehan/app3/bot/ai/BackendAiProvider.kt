package com.ehan.app3.bot.ai

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class BackendAiProvider(
    private val baseUrl: String
) : RemoteAiProvider {

    override val name = "backend"

    override val endpoint: String
        get() = baseUrl

    private val api =
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

            response.text.ifBlank {
                "❌ Server AI tidak mengembalikan teks."
            }

        } catch (exception: Exception) {
            "❌ Gagal menghubungi server AI:\n${exception::class.simpleName}\n${exception.message}"
        }
    }
}