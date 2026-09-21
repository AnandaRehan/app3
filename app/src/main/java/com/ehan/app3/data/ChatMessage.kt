package com.ehan.app3.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class ChatMessage(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val text: String,

    val isBot: Boolean,

    val status: String = if (isBot) {
        "PROCESSED"
    } else {
        "PENDING"
    },

    val timestamp: Long = System.currentTimeMillis()
)