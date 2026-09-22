package com.ehan.app3.bot.command.commands

import com.ehan.app3.bot.command.BotCommand

class AllMenuCommand : BotCommand {

    override val name = "allmenu"

    override val description =
        "Menampilkan semua fitur bot"

    override suspend fun execute(argument: String?): String {

        return """
            📋 SEMUA MENU BOT

            📥 DOWNLOAD
            /download

            🤖 AI
            /ai <pertanyaan>
            /aiprovider

            🛠 TOOLS
            /tools
            /gaya italic <teks>
            /gaya bold <teks>
            /gaya mono <teks>
            /gaya strike <teks>
            /gaya bolditalic <teks>
            /gaya quote <teks>

            ℹ️ UMUM
            /menu
            /help
            /allmenu
        """.trimIndent()
    }
}