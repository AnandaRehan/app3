package com.ehan.app3

import com.ehan.app3.bot.BotEngine
import com.ehan.app3.bot.baileys.BaileysSocket
import com.ehan.app3.data.ChatDao
import com.ehan.app3.data.ChatMessage
import kotlinx.coroutines.delay

class ChatRepository(
    private val chatDao: ChatDao
) {

    private val botEngine =
        BotEngine()

    /**
     * Instance socket ala @WhiskeySockets/baileys (`sock.sendMessage`)
     * yang otomatis menyimpan setiap pesan keluar ke database Room.
     */
    val sock = BaileysSocket { botMessage ->
        chatDao.insertMessage(botMessage)
    }

    fun getMessages() =
        chatDao.getAllMessages()

    suspend fun saveUserMessage(
        text: String
    ) {
        chatDao.insertMessage(
            ChatMessage(
                text = text,
                isBot = false,
                status = "PENDING"
            )
        )
    }

    suspend fun processPendingMessages(): Boolean {

        val pendingMessages =
            chatDao.getPendingMessages()

        for (message in pendingMessages) {

            val locked =
                chatDao.markAsProcessing(
                    message.id
                )

            if (locked == 0) {
                continue
            }

            try {

                delay(400)

                botEngine.handleMessage(
                    sock = sock,
                    jid = BaileysSocket.DEFAULT_CHAT_JID,
                    userMessage = message
                )

                chatDao.markAsProcessed(
                    message.id
                )

            } catch (exception: Exception) {

                chatDao.resetToPending(
                    message.id
                )

                return false
            }
        }

        return true
    }

    suspend fun clearMessages() {
        chatDao.deleteAllMessages()
    }
}
