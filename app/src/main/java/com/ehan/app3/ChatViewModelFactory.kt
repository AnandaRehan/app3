package com.ehan.app3

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ehan.app3.data.ChatDao

class ChatViewModelFactory(
    private val chatDao: ChatDao,
    private val context: Context
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {

            return ChatViewModel(
                chatDao = chatDao,
                context = context.applicationContext
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class"
        )
    }
}