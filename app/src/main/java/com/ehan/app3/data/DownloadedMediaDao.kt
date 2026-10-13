package com.ehan.app3.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadedMediaDao {

    @Query("SELECT * FROM downloaded_media ORDER BY timestamp DESC")
    fun getAllMedia(): Flow<List<DownloadedMedia>>

    @Query("SELECT * FROM downloaded_media WHERE mediaType = :type ORDER BY timestamp DESC")
    fun getMediaByType(type: String): Flow<List<DownloadedMedia>>

    @Query("SELECT COUNT(*) FROM downloaded_media")
    fun countAll(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedia(media: DownloadedMedia): Long

    @Delete
    suspend fun deleteMedia(media: DownloadedMedia)

    @Query("DELETE FROM downloaded_media WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM downloaded_media")
    suspend fun clearAll()
}
