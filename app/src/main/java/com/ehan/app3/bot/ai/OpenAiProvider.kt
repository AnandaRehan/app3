package com.ehan.app3.bot.ai

class OpenAiProvider(
    override val endpoint: String
) : RemoteAiProvider {

    override val name = "openai"

    override suspend fun ask(prompt: String): String {
        return """
            🤖 OPENAI

            Prompt:
            $prompt

            Provider OpenAI sudah terdaftar,
            tetapi koneksi API belum diaktifkan.
        """.trimIndent()
    }
}