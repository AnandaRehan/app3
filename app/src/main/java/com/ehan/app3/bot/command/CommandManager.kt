package com.ehan.app3.bot.command

import com.ehan.app3.bot.command.commands.HelpCommand
import com.ehan.app3.bot.command.commands.MenuCommand
import com.ehan.app3.bot.command.commands.StyleCommand
import com.ehan.app3.bot.command.commands.ToolsCommand

class CommandManager {

    private val commands: List<BotCommand> =
        listOf(
            MenuCommand(),
            HelpCommand(),
            ToolsCommand(),
            StyleCommand()
        )

    private val commandMap =
        commands.associateBy {
            it.name.lowercase()
        }

    fun execute(input: String): String {

        val cleanInput =
            input.trim()

        if (!cleanInput.startsWith("/")) {
            return "Ketik /menu untuk melihat fitur bot."
        }

        val content =
            cleanInput.removePrefix("/")

        val parts =
            content.split(
                limit = 2,
                delimiters = arrayOf(" ")
            )

        val commandName =
            parts[0].lowercase()

        val argument =
            parts.getOrNull(1)?.trim()

        val command =
            commandMap[commandName]

        if (command == null) {
            return """
                ❌ Command tidak ditemukan.

                Ketik /menu untuk melihat daftar fitur.
            """.trimIndent()
        }

        return command.execute(argument)
    }
}