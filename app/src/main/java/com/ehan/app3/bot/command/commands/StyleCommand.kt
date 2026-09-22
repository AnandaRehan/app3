package com.ehan.app3.bot.command.commands

import com.ehan.app3.bot.command.BotCommand

class StyleCommand : BotCommand {

    override val name = "gaya"

    override val description =
        "Membuat teks dengan berbagai gaya"

    override fun execute(
        argument: String?
    ): String {

        if (argument.isNullOrBlank()) {
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

        val parts =
            argument.trim().split(
                limit = 2,
                delimiters = arrayOf(" ")
            )

        if (parts.size < 2) {
            return """
                Format salah.

                Contoh:
                /gaya bold halo dunia
            """.trimIndent()
        }

        val style =
            parts[0].lowercase()

        val text =
            parts[1].trim()

        return when (style) {

            "italic" ->
                "_${text}_"

            "bold" ->
                "*${text}*"

            "mono" ->
                "```$text```"

            "strike" ->
                "~${text}~"

            "bolditalic" ->
                "*_${text}_*"

            else ->
                """
                    ❌ Gaya "$style" tidak tersedia.

                    Pilihan:
                    italic
                    bold
                    mono
                    strike
                    bolditalic
                """.trimIndent()
        }
    }
}