package com.ehan.app3.bot.command.commands

import com.ehan.app3.bot.command.BotCommand

class DownloadCommand : BotCommand {

    override val name = "download"

    override val description =
        "Menampilkan fitur download"

    override fun execute(
        argument: String?
    ): String {

        return """
            📥 DOWNLOAD

            Fitur download akan tersedia di sini.

            Contoh fitur:
            • Download video
            • Download audio
            • Download gambar

            ⚠️ Fitur masih dalam pengembangan.
        """.trimIndent()
    }
}