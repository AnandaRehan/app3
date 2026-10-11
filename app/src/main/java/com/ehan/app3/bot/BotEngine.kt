package com.ehan.app3.bot

import com.ehan.app3.bot.command.CommandManager
import com.ehan.app3.bot.tiktok.TikWmService

class BotEngine {

    private val tikWmService = TikWmService()

    private val commandManager =
        CommandManager(tikWmService)

    suspend fun reply(message: String): String {
        val input = message.trim()

        return when {
            input.startsWith("/") -> {
                commandManager.execute(input)
            }
            TikWmService.containsTikTokUrl(input) -> {
                tikWmService.downloadAndFormat(input)
            }
            else -> {
                when (input.lowercase()) {
                    "halo",
                    "hai",
                    "hello" ->
                        "Halo! Ketik /menu untuk melihat fitur bot atau tempel link TikTok untuk mengunduh video/slide HD."

                    else ->
                        "Saya belum mengerti pesan itu.\n\nKetik /menu untuk melihat fitur, atau kirim link TikTok untuk download otomatis."
                }
            }
        }
    }
}
