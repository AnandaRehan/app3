package com.ehan.app3.bot.ai

data class AiConfig(
    val model: String = "gpt-5.6-luna",
    val endpoint: String = "https://api.openai.com/v1/"
)