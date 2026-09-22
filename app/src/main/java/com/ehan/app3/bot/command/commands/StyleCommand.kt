package com.ehan.app3.bot.command.commands

import com.ehan.app3.bot.command.BotCommand
import com.ehan.app3.bot.command.style.TextStyleManager

class StyleCommand : BotCommand {

    override val name = "gaya"

    override val description =
        "Membuat teks dengan berbagai gaya"

    private val styleManager =
        TextStyleManager()

    override fun execute(
        argument: String?
    ): String {

        if (argument.isNullOrBlank()) {
            return showHelp()
        }

        val parts =
            argument.trim().split(
                limit = 2,
                delimiters = arrayOf(" ")
            )

        if (parts.size < 2) {
            return showHelp()
        }

        val style =
            parts[0].lowercase()

        val text =
            parts[1].trim()

        return styleManager.format(
            style,
            text
        ) ?: """
            ❌ Gaya "$style" tidak tersedia.

            ${showAvailableStyles()}
        """.trimIndent()
    }

    private fun showHelp(): String {
        return """
            🎨 GAYA TEKS

            /gaya italic <teks>
            /gaya bold <teks>
            /gaya mono <teks>
            /gaya strike <teks>
            /gaya bolditalic <teks>

            Contoh:
            /gaya bold halo dunia
        """.trimIndent()
    }

    private fun showAvailableStyles(): String {
        return """
            Pilihan:
            italic
            bold
            mono
            strike
            bolditalic
        """.trimIndent()
    }
}