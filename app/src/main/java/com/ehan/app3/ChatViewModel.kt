package com.ehan.app3

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ehan.app3.bot.BotEngine
import com.ehan.app3.data.ChatDao
import com.ehan.app3.data.ChatMessage
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel(
    private val chatDao: ChatDao,
    private val context: Context
) : ViewModel() {

    val messages: StateFlow<List<ChatMessage>> =
        chatDao.getAllMessages()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> =
        _isTyping

    private val _isOnline = MutableStateFlow(false)
    val isOnline: StateFlow<Boolean> =
        _isOnline

    fun updateNetworkStatus(isOnline: Boolean) {
        _isOnline.value = isOnline

        if (isOnline) {
            ChatWorkScheduler.schedule(context)
        }
    }

    fun sendMessage(text: String) {

        val cleanText = text.trim()

        if (cleanText.isEmpty()) return

        viewModelScope.launch {

            chatDao.insertMessage(
                ChatMessage(
                    text = cleanText,
                    isBot = false
                )
            )

            if (!_isOnline.value) {
                ChatWorkScheduler.schedule(context)
                return@launch
            }

            processBotReply(cleanText)
        }
    }

    private suspend fun processBotReply(
        text: String
    ) {
        _isTyping.value = true

        delay(700)

        val reply = BotEngine.reply(text)

        chatDao.insertMessage(
            ChatMessage(
                text = reply,
                isBot = true
            )
        )

        _isTyping.value = false
    }

    fun clearChat() {
        viewModelScope.launch {
            chatDao.deleteAllMessages()
        }
    }
}