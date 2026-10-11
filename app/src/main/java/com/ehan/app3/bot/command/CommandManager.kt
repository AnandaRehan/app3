package com.ehan.app3.bot.command

import com.ehan.app3.bot.ai.AiProviderManager
import com.ehan.app3.bot.baileys.BaileysSocket
import com.ehan.app3.bot.baileys.BotButton
import com.ehan.app3.bot.baileys.BotMessageContent
import com.ehan.app3.bot.baileys.ButtonText
import com.ehan.app3.bot.baileys.SendMessageOptions
import com.ehan.app3.bot.command.commands.AiCommand
import com.ehan.app3.bot.command.commands.AiProviderCommand
import com.ehan.app3.bot.command.commands.AllMenuCommand
import com.ehan.app3.bot.command.commands.CalcCommand
import com.ehan.app3.bot.command.commands.DownloadCommand
import com.ehan.app3.bot.command.commands.EchoCommand
import com.ehan.app3.bot.command.commands.HelpCommand
import com.ehan.app3.bot.command.commands.MenuCommand
import com.ehan.app3.bot.command.commands.PingCommand
import com.ehan.app3.bot.command.commands.StatusCommand
import com.ehan.app3.bot.command.commands.StyleCommand
import com.ehan.app3.bot.command.commands.TikTokCommand
import com.ehan.app3.bot.command.commands.ToolsCommand
import com.ehan.app3.bot.tiktok.TikWmService
import com.ehan.app3.data.ChatMessage

class CommandManager(
    private val tikWmService: TikWmService = TikWmService()
) {

    private val aiProviderManager =
        AiProviderManager()

    private lateinit var commands: List<BotCommand>

    val allCommands: List<BotCommand>
        get() = commands

    init {
        val basicCommands =
            listOf<BotCommand>(
                TikTokCommand(tikWmService),
                DownloadCommand(tikWmService),
                ToolsCommand(),
                StyleCommand(),
                AiCommand(aiProviderManager),
                AiProviderCommand(aiProviderManager),
                PingCommand(),
                EchoCommand(),
                CalcCommand(),
                StatusCommand(aiProviderManager)
            )

        commands =
            listOf(
                MenuCommand(basicCommands),
                AllMenuCommand(basicCommands)
            ) +
            basicCommands +
            HelpCommand(basicCommands)
    }

    private val commandMap
        get() = commands.associateBy {
            it.name.lowercase()
        }

    private fun parseCommandAndArgument(input: String): Pair<BotCommand?, String?> {
        val cleanInput = input.trim()
        val content = cleanInput.removePrefix("/")
        val parts = content.split(limit = 2, delimiters = arrayOf(" "))
        val rawCommandName = parts[0].lowercase()

        val commandName = when (rawCommandName) {
            "tt", "tikwm" -> "tiktok"
            "dl" -> "download"
            "apikey", "setkey" -> "tiktok"
            else -> rawCommandName
        }

        val rawArgument = parts.getOrNull(1)?.trim()
        val argument = if (rawCommandName == "apikey" || rawCommandName == "setkey") {
            if (rawArgument.isNullOrBlank()) null else "key $rawArgument"
        } else {
            rawArgument
        }

        return commandMap[commandName] to argument
    }

    suspend fun execute(input: String): String {
        val cleanInput = input.trim()
        if (!cleanInput.startsWith("/")) {
            return "Ketik /menu untuk melihat fitur bot."
        }

        val (command, argument) = parseCommandAndArgument(cleanInput)
        if (command == null) {
            return """
                ❌ Command tidak ditemukan.

                Ketik /menu untuk melihat daftar fitur.
            """.trimIndent()
        }

        return command.execute(argument)
    }

    suspend fun execute(
        sock: BaileysSocket,
        jid: String,
        input: String,
        quoted: ChatMessage? = null
    ) {
        val cleanInput = input.trim()
        if (!cleanInput.startsWith("/")) {
            sock.sendMessage(
                jid = jid,
                content = BotMessageContent(
                    text = "Ketik /menu untuk melihat fitur bot.",
                    buttons = listOf(
                        BotButton(
                            buttonId = "/menu",
                            buttonText = ButtonText("📋 Menu Utama"),
                            type = BotButton.TYPE_REPLY
                        )
                    )
                ),
                options = SendMessageOptions(quoted = quoted)
            )
            return
        }

        val (command, argument) = parseCommandAndArgument(cleanInput)
        if (command == null) {
            sock.sendMessage(
                jid = jid,
                content = BotMessageContent(
                    text = """
                        ❌ Command tidak ditemukan.

                        Ketik /menu untuk melihat daftar fitur.
                    """.trimIndent(),
                    buttons = listOf(
                        BotButton(
                            buttonId = "/menu",
                            buttonText = ButtonText("📋 Buka Menu"),
                            type = BotButton.TYPE_REPLY
                        )
                    )
                ),
                options = SendMessageOptions(quoted = quoted)
            )
            return
        }

        command.execute(
            sock = sock,
            jid = jid,
            argument = argument,
            quoted = quoted
        )
    }
}
