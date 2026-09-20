package com.ehan.app3

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ehan.app3.bot.BotEngine
import com.ehan.app3.data.ChatDao
import com.ehan.app3.data.ChatMessage
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel(
    private val chatDao: ChatDao
) : ViewModel() {

    private val botEngine = BotEngine()

    val messages: StateFlow<List<ChatMessage>> =
        chatDao.getAllMessages()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    fun sendMessage(text: String) {

        val message = text.trim()

        if (message.isEmpty()) return

        viewModelScope.launch {

            // Simpan pesan user
            chatDao.insertMessage(
                ChatMessage(
                    text = message,
                    isBot = false
                )
            )

            // Buat balasan bot
            val reply = botEngine.reply(message)

            // Simpan balasan bot
            chatDao.insertMessage(
                ChatMessage(
                    text = reply,
                    isBot = true
                )
            )
        }
    }

    fun clearChat() {

        viewModelScope.launch {
            chatDao.deleteAllMessages()
        }
    }
}