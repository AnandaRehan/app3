package com.ehan.app3.bot.command.commands

import com.ehan.app3.bot.command.BotCommand

class StyleCommand : BotCommand {

    override val name = "gaya"

    override val description =
        "Membuat teks miring"

    override fun execute(
        argument: String?
    ): String {

        if (argument.isNullOrBlank()) {
            return "Contoh:\n/gaya halo dunia"
        }

        return "_${argument.trim()}_"
    }
}