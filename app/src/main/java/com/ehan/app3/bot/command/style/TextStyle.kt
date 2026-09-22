package com.ehan.app3.bot.command.style

interface TextStyle {

    val name: String

    val description: String

    fun format(text: String): String
}