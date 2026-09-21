package com.ehan.app3.network

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ChatRequest(
    val message: String
)

@JsonClass(generateAdapter = true)
data class ChatResponse(
    val reply: String
)