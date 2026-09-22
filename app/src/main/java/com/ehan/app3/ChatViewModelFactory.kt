package com.ehan.app3

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ehan.app3.data.ChatDao

class ChatViewModelFactory(
    private val chatDao: ChatDao
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {

            return ChatViewModel(
                chatDao = chatDao
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class"
        )
    }
}