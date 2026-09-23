package com.ehan.app3.bot.command.commands

import com.ehan.app3.bot.command.BotCommand

class AllMenuCommand(
    private val commands: List<BotCommand>
) : BotCommand {

    override val name = "allmenu"

    override val description =
        "Menampilkan semua fitur bot"

    override suspend fun execute(argument: String?): String {

        val commandList =
            commands
                .filter { it.name != "allmenu" }
                .joinToString("\n") {
                    "/${it.name} - ${it.description}"
                }

        return """
            📋 SEMUA MENU BOT${"\n\n"}$commandList
        """.trimIndent()
    }
}