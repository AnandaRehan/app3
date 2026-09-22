package com.ehan.app3.bot.command

import com.ehan.app3.bot.ai.AiProviderManager
import com.ehan.app3.bot.command.commands.AiCommand
import com.ehan.app3.bot.command.commands.AiProviderCommand
import com.ehan.app3.bot.command.commands.AllMenuCommand
import com.ehan.app3.bot.command.commands.DownloadCommand
import com.ehan.app3.bot.command.commands.HelpCommand
import com.ehan.app3.bot.command.commands.MenuCommand
import com.ehan.app3.bot.command.commands.StyleCommand
import com.ehan.app3.bot.command.commands.ToolsCommand
import com.ehan.app3.bot.command.commands.PingCommand
import com.ehan.app3.bot.command.commands.EchoCommand
import com.ehan.app3.bot.command.commands.CalcCommand

class CommandManager {

    private val aiProviderManager =
        AiProviderManager()

    private val commands: List<BotCommand> =
        listOf(
            MenuCommand(),
            HelpCommand(),
            ToolsCommand(),
            StyleCommand(),
            AllMenuCommand(),
            DownloadCommand(),
            AiCommand(aiProviderManager),
            AiProviderCommand(aiProviderManager),
            PingCommand(),
            EchoCommand(),
            CalcCommand()
        )

    private val commandMap =
        commands.associateBy {
            it.name.lowercase()
        }

    suspend fun execute(input: String): String {

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