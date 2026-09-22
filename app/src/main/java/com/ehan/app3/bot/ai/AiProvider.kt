package com.ehan.app3.bot.ai

interface AiProvider {

    val name: String

    suspend fun ask(prompt: String): String
}