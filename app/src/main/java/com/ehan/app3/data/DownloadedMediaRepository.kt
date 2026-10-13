package com.ehan.app3.data

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class DownloadedMediaRepository(
    private val downloadedMediaDao: DownloadedMediaDao
) {

    val allMedia: Flow<List<DownloadedMedia>> = downloadedMediaDao.getAllMedia()

    val mediaCount: Flow<Int> = downloadedMediaDao.countAll()

    fun getMediaByType(type: String): Flow<List<DownloadedMedia>> {
        return downloadedMediaDao.getMediaByType(type)
    }

    suspend fun insertMedia(media: DownloadedMedia): Long {
        return downloadedMediaDao.insertMedia(media)
    }

    suspend fun deleteMedia(media: DownloadedMedia) {
        downloadedMediaDao.deleteMedia(media)
    }

    suspend fun deleteById(id: Long) {
        downloadedMediaDao.deleteById(id)
    }

    suspend fun clearAll() {
        downloadedMediaDao.clearAll()
    }

    companion object {
        @Volatile
        private var INSTANCE: DownloadedMediaRepository? = null

        fun getInstance(context: Context): DownloadedMediaRepository {
            return INSTANCE ?: synchronized(this) {
                val db = ChatDatabase.getDatabase(context.applicationContext)
                val repo = DownloadedMediaRepository(db.downloadedMediaDao())
                INSTANCE = repo
                repo
            }
        }

        fun recordDownload(
            context: Context,
            title: String,
            mediaType: String,
            sourcePlatform: String,
            downloadUrl: String,
            thumbnailUrl: String? = null,
            fileSizeFormatted: String? = null,
            durationFormatted: String? = null,
            filePath: String? = null
        ) {
            val repo = getInstance(context)
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    repo.insertMedia(
                        DownloadedMedia(
                            title = title.ifBlank { "Unduhan $sourcePlatform" },
                            mediaType = mediaType,
                            sourcePlatform = sourcePlatform,
                            downloadUrl = downloadUrl,
                            thumbnailUrl = thumbnailUrl,
                            fileSizeFormatted = fileSizeFormatted,
                            durationFormatted = durationFormatted,
                            filePath = filePath,
                            timestamp = System.currentTimeMillis()
                        )
                    )
                } catch (_: Exception) {
                    // Ignored
                }
            }
        }
    }
}
