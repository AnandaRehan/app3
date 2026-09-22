package com.ehan.app3.bot.command.style.styles

import com.ehan.app3.bot.command.style.TextStyle

class BoldStyle : TextStyle {

    override val name = "bold"

    override val description =
        "Teks tebal"

    override fun format(
        text: String
    ): String {
        return "*${text}*"
    }
}