package com.ehan.app3.bot.command.commands

import com.ehan.app3.bot.command.BotCommand

class PingCommand : BotCommand {

    override val name = "ping"

    override val description =
        "Mengecek respons bot"

    override suspend fun execute(argument: String?): String {
        return "🏓 Pong!"
    }
}