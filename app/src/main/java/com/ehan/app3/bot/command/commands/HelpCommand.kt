package com.ehan.app3.bot.command.commands

import com.ehan.app3.bot.command.BotCommand

class HelpCommand : BotCommand {

    override val name = "help"

    override val description =
        "Menampilkan bantuan"

    override suspend fun execute(argument: String?): String {
        return """
            🆘 BANTUAN

            /menu
            Menampilkan menu utama.

            /tools
            Menampilkan tools.

            /echo <teks>
            Mengulang teks yang diberikan

            /gaya <teks>
            Membuat teks dengan format WhatsApp.

            Contoh:
            /gaya halo dunia
        """.trimIndent()
    }
}