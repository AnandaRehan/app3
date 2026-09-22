package com.ehan.app3.bot.command.commands

import com.ehan.app3.bot.command.BotCommand

class AiCommand : BotCommand {

    override val name = "ai"

    override val description =
        "Menampilkan fitur AI"

    override fun execute(
        argument: String?
    ): String {

        return """
            🤖 AI

            Fitur AI akan tersedia di sini.

            Contoh fitur:
            • Tanya AI
            • Ringkas teks
            • Terjemahan
            • Analisis teks

            ⚠️ Fitur masih dalam pengembangan.
        """.trimIndent()
    }
}