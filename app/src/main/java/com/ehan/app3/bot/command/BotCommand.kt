package com.ehan.app3.bot.command

import com.ehan.app3.bot.baileys.BaileysSocket
import com.ehan.app3.bot.baileys.BotMessageContent
import com.ehan.app3.bot.baileys.SendMessageOptions
import com.ehan.app3.data.ChatMessage

interface BotCommand {

    val name: String

    val description: String

    val category: String

    suspend fun execute(argument: String?): String

    /**
     * Eksekusi command menggunakan sistem mirip @WhiskeySockets/baileys (`sock.sendMessage`).
     * Command dapat meng-override fungsi ini untuk mengirim video, foto, slide carousel,
     * audio, maupun pesan tombol (buttons).
     */
    suspend fun execute(
        sock: BaileysSocket,
        jid: String,
        argument: String?,
        quoted: ChatMessage? = null
    ) {
        val textReply = execute(argument)
        sock.sendMessage(
            jid = jid,
            content = BotMessageContent(text = textReply),
            options = SendMessageOptions(quoted = quoted)
        )
    }
}
