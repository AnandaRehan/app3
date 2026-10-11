package com.ehan.app3.bot.command.commands

import com.ehan.app3.bot.command.BotCommand
import com.ehan.app3.bot.tiktok.TikWmService

class TikTokCommand(
    private val tikWmService: TikWmService = TikWmService()
) : BotCommand {

    override val name = "tiktok"

    override val description =
        "Download video, foto slide, & audio TikTok tanpa watermark (HD)"

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
                • /tiktok key <key_tikwm_anda>
            """.trimIndent()
        }

        if (cleanArg.lowercase().startsWith("key ")) {
            val newKey = cleanArg.substringAfter(" ").trim()
            if (newKey.isEmpty()) {
                return "❌ Masukkan API Key setelah kata 'key'. Contoh:\n/tiktok key abc123xyz"
            }
            TikWmService.saveCustomApiKey(newKey)
            val masked = if (newKey.length > 6) {
                newKey.take(3) + "***" + newKey.takeLast(3)
            } else {
                "***"
            }
            return "✅ API Key TikWM ($masked) berhasil disimpan di aplikasi! Sekarang Anda bisa langsung kirim link TikTok untuk download."
        }

        return tikWmService.downloadAndFormat(cleanArg)
    }
}
