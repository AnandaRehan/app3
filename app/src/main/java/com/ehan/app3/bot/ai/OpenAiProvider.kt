package com.ehan.app3.bot.ai

class OpenAiProvider(
    private val config: AiConfig
) : RemoteAiProvider {

    override val name = "openai"

    override val endpoint: String
        get() = config.endpoint

    override suspend fun ask(prompt: String): String {
        return """
            🤖 OPENAI

            Model:
            ${config.model}

            Prompt:
            $prompt

            Provider sudah dikonfigurasi,
            tetapi autentikasi API belum diaktifkan.
        """.trimIndent()
    }
}