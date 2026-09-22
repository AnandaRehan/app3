package com.ehan.app3.bot.command.commands

import com.ehan.app3.bot.ai.AiService
import com.ehan.app3.bot.command.BotCommand

class AiCommand(
    private val aiService: AiService
) : BotCommand {

    override val name = "ai"

    override val description =
        "Menggunakan fitur AI"

    override suspend fun execute(
        argument: String?
    ): String {

        if (argument.isNullOrBlank()) {
            return """
                🤖 AI

                Gunakan:
                /ai <pertanyaan>

                Contoh:
                /ai jelaskan fotosintesis
            """.trimIndent()
        }

        return aiService.ask(
            argument.trim()
        )
    }
}