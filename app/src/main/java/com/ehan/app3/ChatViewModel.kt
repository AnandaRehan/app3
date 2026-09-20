package com.ehan.app3

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ehan.app3.bot.BotEngine
import com.ehan.app3.data.ChatDao
import com.ehan.app3.data.ChatMessage
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private val _isTyping =
        MutableStateFlow(false)

    val isTyping: StateFlow<Boolean> =
        _isTyping.asStateFlow()

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

            // Bot sedang mengetik
            _isTyping.value = true

            // Simulasi proses berpikir bot
            delay(700)

            // Buat balasan
            val reply = botEngine.reply(message)

            // Simpan balasan
            chatDao.insertMessage(
                ChatMessage(
                    text = reply,
                    isBot = true
                )
            )

            // Bot selesai mengetik
            _isTyping.value = false
        }
    }

    fun clearChat() {

        viewModelScope.launch {
            chatDao.deleteAllMessages()
        }
    }
}