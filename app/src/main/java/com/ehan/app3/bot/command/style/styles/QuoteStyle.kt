package com.ehan.app3.bot.command.style.styles

import com.ehan.app3.bot.command.style.TextStyle

class QuoteStyle : TextStyle {

    override val name = "quote"

    override val description =
        "Membuat teks menjadi quote"

    override fun format(
        text: String
    ): String {
        return "> $text"
    }
}