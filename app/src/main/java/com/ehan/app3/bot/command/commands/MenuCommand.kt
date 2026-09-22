package com.ehan.app3.bot.command.commands

import com.ehan.app3.bot.command.BotCommand

class MenuCommand : BotCommand {

    override val name = "menu"

    override val description =
        "Menampilkan menu utama"

    override fun execute(
        argument: String?
    ): String {

        return """
            🤖 MENU BOT

            📥 DOWNLOAD
            Kumpulan fitur download.

            🤖 AI
            Kumpulan fitur AI.

            🛠 TOOLS
            /tools

            📋 SEMUA MENU
            /allmenu

            Ketik /tools untuk melihat tools.
        """.trimIndent()
    }
}