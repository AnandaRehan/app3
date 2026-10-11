package com.ehan.app3.bot.command.commands

import com.ehan.app3.bot.baileys.BaileysSocket
import com.ehan.app3.bot.baileys.BotButton
import com.ehan.app3.bot.baileys.BotMessageContent
import com.ehan.app3.bot.baileys.ButtonText
import com.ehan.app3.bot.baileys.SendMessageOptions
import com.ehan.app3.bot.command.BotCommand
import com.ehan.app3.data.ChatMessage

class MenuCommand(
    private val commands: List<BotCommand>
) : BotCommand {

    override val name = "menu"

    override val description =
        "Menampilkan menu utama"

    override val category =
        "General"

    override suspend fun execute(argument: String?): String {

        val categoryOrder =
            listOf(
                "General",
                "AI",
                "Tools",
                "Download"
            )

        val categories =
            commands
                .filter {
                    it.name !in listOf(
                        "menu",
                        "help",
                        "allmenu"
                    )
                }
                .groupBy {
                    it.category
                }
                .toSortedMap(
                    compareBy { category ->
                        categoryOrder.indexOf(category)
                            .let { index ->
                                if (index == -1) Int.MAX_VALUE else index
                            }
                    }
                )

        val menuText =
            categories
                .entries
                .joinToString("\n\n") { (category, categoryCommands) ->

                    val commandList =
                        categoryCommands
                            .joinToString("\n") {
                                "• /${it.name} — ${it.description}"
                            }

                    "📂 $category\n$commandList"
                }

        return """
🤖 MENU UTAMA APP3 BOT

$menuText

📋 Lihat detail semua perintah: /allmenu
        """.trimIndent()
    }

    override suspend fun execute(
        sock: BaileysSocket,
        jid: String,
        argument: String?,
        quoted: ChatMessage?
    ) {
        val text = execute(argument)
        sock.sendMessage(
            jid = jid,
            content = BotMessageContent(
                text = text,
                footer = "Pilih tombol cepat di bawah • @WhiskeySockets/baileys",
                buttons = listOf(
                    BotButton(
                        buttonId = "/tiktok",
                        buttonText = ButtonText("🎬 TikTok HD"),
                        type = BotButton.TYPE_REPLY
                    ),
                    BotButton(
                        buttonId = "/ai Halo!",
                        buttonText = ButtonText("🤖 Tanya AI"),
                        type = BotButton.TYPE_REPLY
                    ),
                    BotButton(
                        buttonId = "/tools",
                        buttonText = ButtonText("🛠️ Tools"),
                        type = BotButton.TYPE_REPLY
                    ),
                    BotButton(
                        buttonId = "/allmenu",
                        buttonText = ButtonText("📋 Semua Menu"),
                        type = BotButton.TYPE_REPLY
                    )
                )
            ),
            options = SendMessageOptions(quoted = quoted)
        )
    }
}
