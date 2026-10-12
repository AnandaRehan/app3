package com.ehan.app3.bot

import com.ehan.app3.bot.baileys.BaileysSocket
import com.ehan.app3.bot.baileys.BotButton
import com.ehan.app3.bot.baileys.BotMessageContent
import com.ehan.app3.bot.baileys.ButtonText
import com.ehan.app3.bot.baileys.SendMessageOptions
import com.ehan.app3.bot.command.CommandManager
import com.ehan.app3.bot.tiktok.TikWmService
import com.ehan.app3.bot.youtube.NazeYoutubeService
import com.ehan.app3.data.ChatMessage

class BotEngine {

    private val tikWmService = TikWmService()
    private val youtubeService = NazeYoutubeService()

    private val commandManager =
        CommandManager(tikWmService, youtubeService)

    /**
     * Handler utama dengan arsitektur @WhiskeySockets/baileys (`sock.sendMessage`).
     */
    suspend fun handleMessage(
        sock: BaileysSocket,
        jid: String = BaileysSocket.DEFAULT_CHAT_JID,
        userMessage: ChatMessage
    ) {
        val input = userMessage.text.trim()
        val options = SendMessageOptions(quoted = userMessage)

        when {
            input.startsWith("/") -> {
                commandManager.execute(
                    sock = sock,
                    jid = jid,
                    input = input,
                    quoted = userMessage
                )
            }
            TikWmService.containsTikTokUrl(input) -> {
                tikWmService.sendTikTokMessage(
                    sock = sock,
                    jid = jid,
                    rawUrl = input,
                    options = options
                )
            }
            NazeYoutubeService.containsYouTubeUrl(input) -> {
                youtubeService.sendYouTubeVideoMessage(
                    sock = sock,
                    jid = jid,
                    rawUrl = input,
                    options = options
                )
            }
            else -> {
                val replyText = when (input.lowercase()) {
                    "halo",
                    "hai",
                    "hello",
                    "p",
                    "pagi",
                    "siang",
                    "malam" ->
                        "Halo! 👋 Ketik /menu untuk melihat fitur bot, atau tempel langsung link TikTok / YouTube untuk download otomatis."

                    else ->
                        "Saya belum mengerti pesan itu.\n\nKetik /menu untuk melihat fitur, atau kirim link TikTok / YouTube untuk download otomatis."
                }

                sock.sendMessage(
                    jid = jid,
                    content = BotMessageContent(
                        text = replyText,
                        footer = "App3 Bot • @WhiskeySockets/baileys",
                        buttons = listOf(
                            BotButton(
                                buttonId = "/menu",
                                buttonText = ButtonText("📋 Menu Utama"),
                                type = BotButton.TYPE_REPLY
                            ),
                            BotButton(
                                buttonId = "/tiktok",
                                buttonText = ButtonText("🎬 TikTok HD"),
                                type = BotButton.TYPE_REPLY
                            ),
                            BotButton(
                                buttonId = "/yt",
                                buttonText = ButtonText("▶️ YouTube 720p"),
                                type = BotButton.TYPE_REPLY
                            )
                        )
                    ),
                    options = options
                )
            }
        }
    }

    suspend fun reply(message: String): String {
        var lastText = ""
        val sock = BaileysSocket { chatMessage ->
            lastText = chatMessage.text
        }
        handleMessage(
            sock = sock,
            userMessage = ChatMessage(
                text = message,
                isBot = false
            )
        )
        return lastText
    }
}
