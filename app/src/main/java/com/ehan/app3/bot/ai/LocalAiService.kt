package com.ehan.app3.bot.ai

class LocalAiService : AiService {

    override suspend fun ask(
        prompt: String
    ): String {

        return """
            🤖 AI LOCAL

            Prompt:
            $prompt

            AI belum terhubung ke provider.
        """.trimIndent()
    }
}