package com.ehan.app3.bot.command.commands

import com.ehan.app3.bot.command.BotCommand

class MenuCommand(
    private val commands: List<BotCommand>
) : BotCommand {

    override val name = "menu"

    override val description =
        "Menampilkan menu utama"

    override val category =
        "General"

    override suspend fun execute(argument: String?): String {

        val commandList =
            commands
                .filter {
                    it.name !in listOf(
                        "menu",
                        "help",
                        "allmenu"
                    )
                }
                .joinToString("\n") {
                    "/${it.name}"
                }

        return """
            🤖 MENU BOT${"\n\n"}$commandList${"\n"}/allmenu

            Ketik command di atas untuk membuka fiturnya.
        """.trimIndent()
    }
}