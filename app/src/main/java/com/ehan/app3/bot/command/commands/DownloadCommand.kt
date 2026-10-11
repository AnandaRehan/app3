package com.ehan.app3.bot.command.commands

import com.ehan.app3.bot.baileys.BaileysSocket
import com.ehan.app3.bot.baileys.BotButton
import com.ehan.app3.bot.baileys.BotMessageContent
import com.ehan.app3.bot.baileys.ButtonText
import com.ehan.app3.bot.baileys.SendMessageOptions
import com.ehan.app3.bot.command.BotCommand
import com.ehan.app3.bot.tiktok.TikWmService
import com.ehan.app3.data.ChatMessage

class DownloadCommand(
    private val tikWmService: TikWmService = TikWmService()
) : BotCommand {

    override val name = "download"

    override val description =
        "Download video & slide foto TikTok HD tanpa watermark"

    override val category =
        "Download"

    override suspend fun execute(argument: String?): String {
        if (!argument.isNullOrBlank()) {
            return tikWmService.downloadAndFormat(argument)
        }

        return """
            📥 DOWNLOADER MENU

            Fitur downloader aktif:
            • TikTok Video HD (Langsung putar & unduh)
            • TikTok Foto Tunggal
            • TikTok Slide (Gabungan Foto & Live Video menyatu)

            Cara pakai:
            • /tiktok <url_tiktok>
            • /download <url_tiktok>
            • Atau langsung tempel link TikTok di chat!
        """.trimIndent()
    }

    override suspend fun execute(
        sock: BaileysSocket,
        jid: String,
        argument: String?,
        quoted: ChatMessage?
    ) {
        val options = SendMessageOptions(quoted = quoted)
        if (!argument.isNullOrBlank()) {
            tikWmService.sendTikTokMessage(
                sock = sock,
                jid = jid,
                rawUrl = argument,
                options = options
            )
            return
        }

        sock.sendMessage(
            jid = jid,
            content = BotMessageContent(
                text = execute(null),
                footer = "App3 Bot Downloader • sock.sendMessage",
                buttons = listOf(
                    BotButton(
                        buttonId = "/tiktok",
                        buttonText = ButtonText("🎬 Panduan TikTok HD"),
                        type = BotButton.TYPE_REPLY
                    ),
                    BotButton(
                        buttonId = "/menu",
                        buttonText = ButtonText("📋 Menu Utama"),
                        type = BotButton.TYPE_REPLY
                    )
                )
            ),
            options = options
        )
    }
}
