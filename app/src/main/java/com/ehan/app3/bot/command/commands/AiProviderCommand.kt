package com.ehan.app3.bot.command.commands

import com.ehan.app3.bot.ai.AiProviderManager
import com.ehan.app3.bot.command.BotCommand

class AiProviderCommand(
    private val aiProviderManager: AiProviderManager
) : BotCommand {

    override val name = "aiprovider"
    override val description = "Melihat dan mengganti AI provider"

    override suspend fun execute(argument: String?): String {

        if (argument.isNullOrBlank()) {
            return showProviders()
        }

        val providerName = argument.trim()

        val changed =
            aiProviderManager.setProvider(providerName)

        if (!changed) {
            return """
                ❌ Provider "$providerName" tidak ditemukan.

                Provider tersedia:
                ${aiProviderManager.getProviders()
                    .joinToString("\n") { "• ${it.name}" }}
            """.trimIndent()
        }

        return """
            ✅ AI provider berhasil diganti.

            Provider aktif:
            ${aiProviderManager.getActiveProviderName()}
        """.trimIndent()
    }

    private fun showProviders(): String {
        val providers =
            aiProviderManager.getProviders()
                .joinToString("\n") {
                    if (it.name == aiProviderManager.getActiveProviderName()) {
                        "• ${it.name} ← aktif"
                    } else {
                        "• ${it.name}"
                    }
                }

        return """
            🤖 AI PROVIDER

            Provider tersedia:
            $providers

            Ganti provider:
            /aiprovider <nama>

            Contoh:
            /aiprovider local
        """.trimIndent()
    }
}