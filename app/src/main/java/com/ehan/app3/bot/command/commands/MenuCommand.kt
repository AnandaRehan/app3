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
            /download

            🤖 AI
            /ai

            🛠 TOOLS
            /tools

            📋 SEMUA MENU
            /allmenu

            Ketik command di atas untuk membuka kategorinya.
        """.trimIndent()
    }
}