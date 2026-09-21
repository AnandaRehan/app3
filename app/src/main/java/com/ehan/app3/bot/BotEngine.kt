package com.ehan.app3.bot

class BotEngine {
    fun reply(message: String): String {
        return when (message.lowercase().trim()) {

            "halo",
            "hai",
            "hello" ->
                "Halo! Ada yang bisa saya bantu?"

            "menu" ->
                """
                Menu Bot:

                1. halo
                2. menu
                3. info
                """.trimIndent()

            "info" ->
                "Saya adalah bot Android sederhana menggunakan Kotlin dan Jetpack Compose."

            else ->
                "Maaf, saya belum mengerti pesan itu."
        }
    }
}