package com.ehan.app3.bot.command.style

import com.ehan.app3.bot.command.style.styles.BoldItalicStyle
import com.ehan.app3.bot.command.style.styles.BoldStyle
import com.ehan.app3.bot.command.style.styles.ItalicStyle
import com.ehan.app3.bot.command.style.styles.MonoStyle
import com.ehan.app3.bot.command.style.styles.StrikeStyle

class TextStyleManager {

    private val styles: List<TextStyle> =
        listOf(
            ItalicStyle(),
            BoldStyle(),
            MonoStyle(),
            StrikeStyle(),
            BoldItalicStyle()
        )

    private val styleMap =
        styles.associateBy {
            it.name.lowercase()
        }

    fun format(
        styleName: String,
        text: String
    ): String? {

        return styleMap[
            styleName.lowercase()
        ]?.format(text)
    }

    fun getStyles(): List<TextStyle> {
        return styles
    }
}