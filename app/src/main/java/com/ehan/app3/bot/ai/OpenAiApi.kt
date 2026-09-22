package com.ehan.app3.bot.ai

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface OpenAiApi {

    @POST("responses")
    suspend fun createResponse(
        @Header("Authorization") authorization: String,
        @Body request: OpenAiRequest
    ): OpenAiResponse
}