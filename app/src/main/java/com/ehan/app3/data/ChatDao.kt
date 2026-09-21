package com.ehan.app3.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {

    @Query(
        "SELECT * FROM messages ORDER BY timestamp ASC"
    )
    fun getAllMessages(): Flow<List<ChatMessage>>

    @Insert
    suspend fun insertMessage(
        message: ChatMessage
    )

    @Query(
        """
        SELECT * FROM messages
        WHERE isBot = 0
        AND status = 'PENDING'
        ORDER BY timestamp ASC
        """
    )
    suspend fun getPendingMessages(): List<ChatMessage>

    @Query(
        """
        UPDATE messages
        SET status = 'PROCESSING'
        WHERE id = :messageId
        AND status = 'PENDING'
        """
    )
    suspend fun markAsProcessing(
        messageId: Long
    ): Int

    @Query(
        """
        UPDATE messages
        SET status = 'PROCESSED'
        WHERE id = :messageId
        """
    )
    suspend fun markAsProcessed(
        messageId: Long
    )

    @Query(
        """
        UPDATE messages
        SET status = 'PENDING'
        WHERE id = :messageId
        """
    )
    suspend fun resetToPending(
        messageId: Long
    )

    @Query("DELETE FROM messages")
    suspend fun deleteAllMessages()
}