package com.ehan.app3.bot.command.commands

import com.ehan.app3.bot.command.BotCommand

class HelpCommand(
    private val commands: List<BotCommand>
) : BotCommand {

    override val name = "help"

    override val description =
        "Menampilkan bantuan"

    override suspend fun execute(argument: String?): String {

        val commandList =
            commands
                .filter { it.name != "help" }
                .joinToString("\n") {
                    "/${it.name} - ${it.description}"
                }

        return """
            🆘 BANTUAN${"\n"}${"\n"}$commandList
        """.trimIndent()
    }
}