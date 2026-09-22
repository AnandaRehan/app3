package com.ehan.app3.bot.ai

import retrofit2.http.Body
import retrofit2.http.POST

interface AiProxyApi {

    @POST("api/ai")
    suspend fun ask(
        @Body request: AiProxyRequest
    ): AiProxyResponse
}