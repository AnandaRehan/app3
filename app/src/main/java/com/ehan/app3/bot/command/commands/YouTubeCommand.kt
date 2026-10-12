package com.ehan.app3.bot.command.commands

import com.ehan.app3.bot.baileys.BaileysSocket
import com.ehan.app3.bot.baileys.BotButton
import com.ehan.app3.bot.baileys.BotMessageContent
import com.ehan.app3.bot.baileys.ButtonText
import com.ehan.app3.bot.baileys.SendMessageOptions
import com.ehan.app3.bot.command.BotCommand
import com.ehan.app3.bot.youtube.NazeYoutubeService
import com.ehan.app3.data.ChatMessage

class YouTubeCommand(
    private val youtubeService: NazeYoutubeService = NazeYoutubeService(),
    private val audioOnlyDefault: Boolean = false
) : BotCommand {

    override val name = if (audioOnlyDefault) "ytmp3" else "youtube"

    override val description = if (audioOnlyDefault) {
        "Download audio MP3 dari video YouTube"
    } else {
        "Download video YouTube 720p (MP4) & Audio (MP3)"
    }

    override val category = "Download"

    override suspend fun execute(argument: String?): String {
        val cleanArg = argument?.trim().orEmpty()
        if (cleanArg.isEmpty()) {
            val hasKey = NazeYoutubeService.getActiveApiKey().isNotEmpty()
            val keyStatus = if (hasKey) "✅ Terpasang (Siap digunakan)" else "⚠️ Belum diatur"
            return """
                ▶️ YOUTUBE DOWNLOADER (MP4 720P & MP3)

                Status API Key Naze: $keyStatus

                Cara penggunaan:
                • /yt <link_youtube> (Video MP4 720p + tombol Audio)
                • /ytmp3 <link_youtube> (Audio MP3 langsung)
                • Atau tempel langsung link YouTube ke chat!

                Cara pasang API Key Naze langsung dari chat:
                • /yt key <key_naze_anda>
                • /apikey naze <key_naze_anda>
            """.trimIndent()
        }

        if (cleanArg.lowercase().startsWith("key ")) {
            val newKey = cleanArg.substringAfter(" ").trim()
            if (newKey.isEmpty()) {
                return "❌ Masukkan API Key Naze setelah kata 'key'. Contoh:\n/yt key abc123xyz"
            }
            NazeYoutubeService.saveCustomApiKey(newKey)
            val masked = if (newKey.length > 6) {
                newKey.take(3) + "***" + newKey.takeLast(3)
            } else {
                "***"
            }
            return "✅ API Key Naze YouTube ($masked) berhasil disimpan! Sekarang Anda bisa langsung kirim link YouTube untuk download MP4 / MP3."
        }

        return youtubeService.downloadAndFormat(cleanArg, audioOnly = audioOnlyDefault)
    }

    override suspend fun execute(
        sock: BaileysSocket,
        jid: String,
        argument: String?,
        quoted: ChatMessage?
    ) {
        val cleanArg = argument?.trim().orEmpty()
        val options = SendMessageOptions(quoted = quoted)

        if (cleanArg.isEmpty() || cleanArg.lowercase().startsWith("key ")) {
            val replyText = execute(argument)
            sock.sendMessage(
                jid = jid,
                content = BotMessageContent(
                    text = replyText,
                    footer = "App3 Bot • YouTube Downloader (naze.dev)",
                    buttons = listOf(
                        BotButton(
                            buttonId = "/menu",
                            buttonText = ButtonText("📋 Menu Utama"),
                            type = BotButton.TYPE_REPLY
                        ),
                        BotButton(
                            buttonId = "/download",
                            buttonText = ButtonText("📥 Semua Downloader"),
                            type = BotButton.TYPE_REPLY
                        )
                    )
                ),
                options = options
            )
            return
        }

        if (audioOnlyDefault) {
            youtubeService.sendYouTubeAudioMessage(
                sock = sock,
                jid = jid,
                rawUrl = cleanArg,
                options = options
            )
        } else {
            youtubeService.sendYouTubeVideoMessage(
                sock = sock,
                jid = jid,
                rawUrl = cleanArg,
                options = options
            )
        }
    }
}
