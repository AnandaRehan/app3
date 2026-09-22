package com.ehan.app3.bot.ai

class LocalAiService : AiService {

    override suspend fun ask(prompt: String): String {
        val input = prompt.trim()

        return when {
            input.equals("halo", ignoreCase = true) ->
                "Halo! Saya AI lokal bot."

            input.equals("siapa kamu", ignoreCase = true) ->
                "Saya AI lokal yang berjalan langsung di aplikasi."

            input.equals("help", ignoreCase = true) ->
                """
                🤖 AI LOCAL

                Contoh:
                /ai halo
                /ai siapa kamu
                /ai help

                Saat ini AI masih berjalan secara lokal.
                """.trimIndent()

            else ->
                """
                🤖 AI LOCAL

                Saya menerima prompt:

                "$input"

                AI provider belum terhubung.
                """.trimIndent()
        }
    }
}