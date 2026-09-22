package com.ehan.app3.bot.command.commands

import com.ehan.app3.bot.command.BotCommand

class CalcCommand : BotCommand {

    override val name = "calc"

    override val description =
        "Menghitung operasi matematika sederhana"

    override suspend fun execute(argument: String?): String {

        if (argument.isNullOrBlank()) {
            return """
                🧮 KALKULATOR

                Gunakan:
                /calc <angka> <operator> <angka>

                Contoh:
                /calc 10 + 5
                /calc 20 * 3
                /calc 100 / 4
            """.trimIndent()
        }

        val parts = argument.trim().split(Regex("\\s+"))

        if (parts.size != 3) {
            return "❌ Format salah.\n\nContoh:\n/calc 10 + 5"
        }

        val first = parts[0].toDoubleOrNull()
        val operator = parts[1]
        val second = parts[2].toDoubleOrNull()

        if (first == null || second == null) {
            return "❌ Angka tidak valid."
        }

        if (operator !in listOf("+", "-", "*", "/")) {
            return "❌ Operator tidak didukung.\n\nGunakan: +  -  *  /"
        }

        if (operator == "/" && second == 0.0) {
            return "❌ Tidak bisa membagi dengan nol."
        }

        val result = when (operator) {
            "+" -> first + second
            "-" -> first - second
            "*" -> first * second
            "/" -> first / second
            else -> return "❌ Operator tidak didukung."
        }

        return "🧮 $first $operator $second = $result"
    }
}