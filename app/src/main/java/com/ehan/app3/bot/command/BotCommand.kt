package com.ehan.app3.bot.command

interface BotCommand {

    val name: String

    val description: String

    suspend fun execute(argument: String?): String
}