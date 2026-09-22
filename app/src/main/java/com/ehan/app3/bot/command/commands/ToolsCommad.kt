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

            🎨 GAYA TEKS
            /gaya italic <teks>
            /gaya bold <teks>
            /gaya mono <teks>
            /gaya strike <teks>
            /gaya bolditalic <teks>

            Contoh:
            /gaya bold halo dunia
        """.trimIndent()
    }
}