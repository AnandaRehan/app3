package com.ehan.app3.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloaded_media")
data class DownloadedMedia(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val mediaType: String, // "VIDEO", "IMAGE", "AUDIO"
    val sourcePlatform: String, // "TIKTOK", "YOUTUBE", "OTHER"
    val filePath: String? = null,
    val downloadUrl: String,
    val thumbnailUrl: String? = null,
    val fileSizeFormatted: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val durationFormatted: String? = null
)
