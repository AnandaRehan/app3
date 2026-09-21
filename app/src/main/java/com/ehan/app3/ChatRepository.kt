package com.ehan.app3

import com.ehan.app3.data.ChatDao
import com.ehan.app3.data.ChatMessage
import com.ehan.app3.network.ChatApiProvider
import com.ehan.app3.network.ChatRequest
import kotlinx.coroutines.delay

class ChatRepository(
    private val chatDao: ChatDao
) {

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

                delay(700)

                val response =
                    ChatApiProvider.api.sendMessage(
                        ChatRequest(
                            message = message.text
                        )
                    )

                chatDao.insertMessage(
                    ChatMessage(
                        text = response.reply,
                        isBot = true,
                        status = "PROCESSED"
                    )
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