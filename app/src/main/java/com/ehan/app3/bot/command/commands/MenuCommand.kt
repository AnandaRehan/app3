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
                                "/${it.name}"
                            }

                    "${" ".repeat(6)}📂 $category\n$commandList"
                }

        return """
${" ".repeat(12)}🤖 MENU BOT${"\n\n"}$menuText${"\n\n"}${" ".repeat(6)}📋 SEMUA MENU${"\n"}/allmenu

${" ".repeat(12)}Ketik command di atas untuk membuka fiturnya.
""".trimIndent()
    }
}