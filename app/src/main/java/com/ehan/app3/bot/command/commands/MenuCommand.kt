package com.ehan.app3.bot.command.commands

import com.ehan.app3.bot.command.BotCommand

class MenuCommand(
    private val commands: List<BotCommand>
) : BotCommand {

    override val name = "menu"

    override val description =
        "Menampilkan menu utama"

    override val category =
        "General"

    override suspend fun execute(argument: String?): String {

        val categories =
            commands
                .filter {
                    it.name !in listOf(
                        "menu",
                        "help",
                        "allmenu"
                    )
                }
                .groupBy {
                    it.category
                }

        val menuText =
            categories
                .entries
                .joinToString("\n\n") { (category, categoryCommands) ->

                    val commandList =
                        categoryCommands
                            .joinToString("\n") {
                                "/${it.name}"
                            }

                    "${" ".repeat(6)}📂 $category\n$commandList"
                }

        return """
            🤖 MENU BOT${"\n\n"}$menuText${"\n\n"}${" ".repeat(6)}📋 SEMUA MENU${"\n"}/allmenu

            Ketik command di atas untuk membuka fiturnya.
        """.trimIndent()
    }
}