package com.ehan.app3.bot.command.commands

import com.ehan.app3.bot.command.BotCommand
import com.ehan.app3.bot.tiktok.TikWmService

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
            • TikTok Video HD (No Watermark)
            • TikTok Photo Slide & Live Photo
            • TikTok Audio / Musik (MP3)

            Cara pakai:
            • /tiktok <url_tiktok>
            • /download <url_tiktok>
            • Atau langsung tempel link TikTok di chat!
        """.trimIndent()
    }
}
