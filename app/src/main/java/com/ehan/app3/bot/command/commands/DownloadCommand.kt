package com.ehan.app3.bot.command.commands

import com.ehan.app3.bot.baileys.BaileysSocket
import com.ehan.app3.bot.baileys.BotButton
import com.ehan.app3.bot.baileys.BotMessageContent
import com.ehan.app3.bot.baileys.ButtonText
import com.ehan.app3.bot.baileys.SendMessageOptions
import com.ehan.app3.bot.command.BotCommand
import com.ehan.app3.bot.tiktok.TikWmService
import com.ehan.app3.bot.youtube.NazeYoutubeService
import com.ehan.app3.data.ChatMessage

class DownloadCommand(
    private val tikWmService: TikWmService = TikWmService(),
    private val youtubeService: NazeYoutubeService = NazeYoutubeService()
) : BotCommand {

    override val name = "download"

    override val description =
        "Download media dari TikTok (HD/Slide) & YouTube (MP4 720p/MP3)"

    override val category =
        "Download"

    override suspend fun execute(argument: String?): String {
        if (!argument.isNullOrBlank()) {
            return when {
                NazeYoutubeService.containsYouTubeUrl(argument) ->
                    youtubeService.downloadAndFormat(argument)
                else ->
                    tikWmService.downloadAndFormat(argument)
            }
        }

        val tikKeyOk = TikWmService.getActiveApiKey().isNotEmpty()
        val ytKeyOk = NazeYoutubeService.getActiveApiKey().isNotEmpty()

        return """
            📥 DOWNLOADER MENU

            1️⃣ TikTok Downloader (${if (tikKeyOk) "✅ Key Aktif" else "⚠️ Key Belum Diatur"})
            • /tiktok <url_tiktok> — Video HD, Foto, & Slide
            • /tiktok key <apikey> — Pasang API Key TikWM

            2️⃣ YouTube Downloader (${if (ytKeyOk) "✅ Key Aktif" else "⚠️ Key Belum Diatur"})
            • /yt <url_youtube> — Video MP4 720p + Tombol Audio
            • /ytmp3 <url_youtube> — Audio MP3
            • /yt key <apikey> — Pasang API Key Naze

            💡 Anda juga bisa langsung menempelkan link TikTok atau YouTube di chat!
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
            if (NazeYoutubeService.containsYouTubeUrl(argument)) {
                youtubeService.sendYouTubeVideoMessage(
                    sock = sock,
                    jid = jid,
                    rawUrl = argument,
                    options = options
                )
            } else {
                tikWmService.sendTikTokMessage(
                    sock = sock,
                    jid = jid,
                    rawUrl = argument,
                    options = options
                )
            }
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
                        buttonId = "/yt",
                        buttonText = ButtonText("▶️ Panduan YouTube 720p"),
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
