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
        if (argument.isNullOrBlank()) {
            return """
                🎬 TIKTOK DOWNLOADER (HD)

                Unduh video TikTok tanpa watermark (HD), foto slide, Live Photo, dan musik MP3!

                Cara penggunaan:
                • /tiktok <link_tiktok>
                • /download <link_tiktok>
                • Atau tempel langsung link TikTok ke chat!

                Contoh:
                /tiktok https://vt.tiktok.com/ZSxxxxxx/
            """.trimIndent()
        }

        return tikWmService.downloadAndFormat(argument)
    }
}
