package com.ehan.app3.bot

import com.ehan.app3.bot.command.CommandManager

class BotEngine {

    private val commandManager =
        CommandManager()

    suspend fun reply(message: String): String {
        val input = message.trim()

        return if (input.startsWith("/")) {
            commandManager.execute(input)
        } else {
            when (input.lowercase()) {
                "halo",
                "hai",
                "hello" ->
                    "Halo! Ketik /menu untuk melihat fitur bot."

                else ->
                    "Saya belum mengerti pesan itu.\n\nKetik /menu untuk melihat fitur."
            }
        }
    }
}