package com.ehan.app3.bot.ai

interface RemoteAiProvider : AiProvider {

    val endpoint: String
}