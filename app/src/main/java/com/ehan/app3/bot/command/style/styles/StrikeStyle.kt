package com.ehan.app3.bot.command.style.styles

import com.ehan.app3.bot.command.style.TextStyle

class StrikeStyle : TextStyle {

    override val name = "strike"

    override val description =
        "Teks coret"

    override fun format(
        text: String
    ): String {
        return "~${text}~"
    }
}