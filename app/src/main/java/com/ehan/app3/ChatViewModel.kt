package com.ehan.app3

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ehan.app3.bot.BotEngine
import com.ehan.app3.data.ChatDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel(
    chatDao: ChatDao
) : ViewModel() {

    private val repository =
        ChatRepository(chatDao)

    val messages: StateFlow<List<com.ehan.app3.data.ChatMessage>> =
        repository.getMessages()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    private val _isTyping =
        MutableStateFlow(false)

    val isTyping: StateFlow<Boolean> =
        _isTyping

    private val _isOnline =
        MutableStateFlow(false)

    val isOnline: StateFlow<Boolean> =
        _isOnline

    private val _errorMessage =
        MutableStateFlow<String?>(null)

    val errorMessage: StateFlow<String?> =
        _errorMessage

    fun updateNetworkStatus(
        isOnline: Boolean
    ) {
        _isOnline.value = isOnline
    }

    fun sendMessage(text: String) {

        val cleanText = text.trim()

        if (cleanText.isEmpty()) {
            return
        }

        viewModelScope.launch {

            try {

                repository.saveUserMessage(
                    cleanText
                )

                _isTyping.value = true

                val success =
                    repository.processPendingMessages()

                if (!success) {
                    _errorMessage.value =
                        "Pesan belum berhasil diproses."
                }

            } catch (exception: Exception) {

                _errorMessage.value =
                    "Terjadi kesalahan saat memproses pesan."

            } finally {

                _isTyping.value = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearChat() {

        viewModelScope.launch {

            try {
                repository.clearMessages()

            } catch (exception: Exception) {

                _errorMessage.value =
                    "Gagal menghapus riwayat chat."
            }
        }
    }
}