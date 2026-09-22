package com.ehan.app3.bot.ai

interface AiService {

    suspend fun ask(
        prompt: String
    ): String
}