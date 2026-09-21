package com.ehan.app3.network

import retrofit2.http.Body
import retrofit2.http.POST

interface ChatApi {

    @POST("api/chat")
    suspend fun sendMessage(
        @Body request: ChatRequest
    ): ChatResponse
}