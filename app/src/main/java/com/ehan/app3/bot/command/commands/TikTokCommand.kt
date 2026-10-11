package com.ehan.app3.bot.command.commands

import com.ehan.app3.bot.baileys.BaileysSocket
import com.ehan.app3.bot.baileys.BotButton
import com.ehan.app3.bot.baileys.BotMessageContent
import com.ehan.app3.bot.baileys.ButtonText
import com.ehan.app3.bot.baileys.SendMessageOptions
import com.ehan.app3.bot.command.BotCommand
import com.ehan.app3.bot.tiktok.TikWmService
import com.ehan.app3.data.ChatMessage

class TikTokCommand(
    private val tikWmService: TikWmService = TikWmService()
) : BotCommand {

    override val name = "tiktok"

    override val description =
        "Download video, foto tunggal, & slide TikTok tanpa watermark (HD)"

    override val category =
        "Download"

    override suspend fun execute(argument: String?): String {
        val cleanArg = argument?.trim().orEmpty()
        if (cleanArg.isEmpty()) {
            val hasKey = TikWmService.getActiveApiKey().isNotEmpty()
            val keyStatus = if (hasKey) "✅ Terpasang (Siap digunakan)" else "⚠️ Belum diatur"
            return """
                🎬 TIKTOK DOWNLOADER (HD)

                Status API Key: $keyStatus

                Cara penggunaan:
                • /tiktok <link_tiktok>
                • /download <link_tiktok>
                • Atau tempel langsung link TikTok ke chat!

                Cara pasang API Key langsung dari chat:
                • /apikey <key_tikwm_anda>
            """.trimIndent()
        }

        if (cleanArg.lowercase().startsWith("key ")) {
            val newKey = cleanArg.substringAfter(" ").trim()
            if (newKey.isEmpty()) {
                return "❌ Masukkan API Key setelah kata 'key'. Contoh:\n/apikey abc123xyz"
            }
            TikWmService.saveCustomApiKey(newKey)
            val masked = if (newKey.length > 6) {
                newKey.take(3) + "***" + newKey.takeLast(3)
            } else {
                "***"
            }
            return "✅ API Key TikWM ($masked) berhasil disimpan! Sekarang Anda bisa langsung kirim link TikTok untuk download video atau slide."
        }

        return tikWmService.downloadAndFormat(cleanArg)
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
                    footer = "App3 Bot • @WhiskeySockets/baileys system",
                    buttons = listOf(
                        BotButton(
                            buttonId = "/menu",
                            buttonText = ButtonText("📋 Menu Utama"),
                            type = BotButton.TYPE_REPLY
                        ),
                        BotButton(
                            buttonId = "/status",
                            buttonText = ButtonText("⚡ Cek Status"),
                            type = BotButton.TYPE_REPLY
                        )
                    )
                ),
                options = options
            )
            return
        }

        tikWmService.sendTikTokMessage(
            sock = sock,
            jid = jid,
            rawUrl = cleanArg,
            options = options
        )
    }
}
