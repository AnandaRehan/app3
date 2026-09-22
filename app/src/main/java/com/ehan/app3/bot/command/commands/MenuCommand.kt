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

            📥 /download
            🤖 /ai
            🛠 /tools
            📋 /allmenu

            Ketik /tools untuk melihat fitur tools.
        """.trimIndent()
    }
}