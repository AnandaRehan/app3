package com.ehan.app3.bot.command.commands

import com.ehan.app3.bot.command.BotCommand

class AllMenuCommand(
    private val commands: List<BotCommand>
) : BotCommand {

    override val name = "allmenu"

    override val description =
        "Menampilkan semua fitur bot"

    override val category =
        "General"

    override suspend fun execute(argument: String?): String {

        val categoryOrder =
            listOf(
                "General",
                "AI",
                "Tools",
                "Download"
            )

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
                .toSortedMap(
                    compareBy { category ->
                        categoryOrder.indexOf(category)
                            .let { index ->
                                if (index == -1) Int.MAX_VALUE else index
                            }
                    }
                )

        val menuText =
            categories
                .entries
                .joinToString("\n\n") { (category, categoryCommands) ->

                    val commandList =
                        categoryCommands
                            .joinToString("\n") {
                                "/${it.name} - ${it.description}"
                            }

                    "${" ".repeat(6)}📂 $category\n$commandList"
                }

        return """
${" ".repeat(12)}📋 SEMUA MENU BOT${"\n\n"}$menuText
""".trimIndent()
    }
}

