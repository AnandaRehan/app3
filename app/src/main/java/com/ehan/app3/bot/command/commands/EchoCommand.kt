package com.ehan.app3.bot.command.commands

import com.ehan.app3.bot.command.BotCommand

class EchoCommand : BotCommand {

    override val name = "echo"

    override val description =
        "Mengulang teks yang diberikan"

    override val category =
        "Tools"

    override suspend fun execute(argument: String?): String {

        if (argument.isNullOrBlank()) {
            return """
                🔊 ECHO

                Gunakan:
                /echo <teks>

                Contoh:
                /echo halo dunia
            """.trimIndent()
        }

        return argument
    }
}