package com.ehan.app3.bot.command.commands

import com.ehan.app3.bot.command.BotCommand

class ToolsCommand : BotCommand {

    override val name = "tools"

    override val description =
        "Menampilkan tools"

    override fun execute(
        argument: String?
    ): String {
        return """
            🛠 TOOLS

            /gaya <teks>
            Format teks untuk WhatsApp.

            Contoh:
            /gaya halo dunia
        """.trimIndent()
    }
}