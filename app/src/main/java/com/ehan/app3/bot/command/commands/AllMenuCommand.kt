package com.ehan.app3.bot.command.commands

import com.ehan.app3.bot.command.BotCommand

class AllMenuCommand : BotCommand {

    override val name = "allmenu"

    override val description =
        "Menampilkan semua fitur bot"

    override fun execute(
        argument: String?
    ): String {

        return """
            📋 SEMUA MENU BOT

            📥 DOWNLOAD
            Fitur download akan ditambahkan nanti.

            🤖 AI
            Fitur AI akan ditambahkan nanti.

            🛠 TOOLS
            /gaya <teks>
            Format teks untuk WhatsApp.

            ℹ️ UMUM
            /menu
            /help
            /allmenu
        """.trimIndent()
    }
}