package com.ehan.app3.service

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.ehan.app3.App3
import com.ehan.app3.bot.tiktok.TikWmApi
import com.ehan.app3.bot.tiktok.TikWmService
import com.ehan.app3.bot.youtube.NazeYoutubeApi
import com.ehan.app3.bot.youtube.NazeYoutubeService
import com.ehan.app3.data.ChatDatabase
import com.ehan.app3.data.DownloadedMediaRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

sealed class DownloadDetectionResult {
    data class Success(val title: String, val fileName: String, val platform: String) : DownloadDetectionResult()
    data class Error(val message: String) : DownloadDetectionResult()
    object InvalidLink : DownloadDetectionResult()
}

/**
 * Processor untuk mengeksekusi pengunduhan otomatis dari Floating Pop-up
 * tanpa membuka aplikasi atau menampilkan dialog.
 * - TikTok: Diutamakan video biasa tanpa watermark (SD No-Watermark via data.play),
 *   nama berkas "sd-<id>.mp4". Jika slide foto, unduh foto-fotonya.
 * - YouTube: Diutamakan video 720p MP4 via Naze API.
 */
class FloatingDownloadProcessor(private val context: Context) {

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(25, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .build()

    private val tikWmApi: TikWmApi = Retrofit.Builder()
        .baseUrl("https://api.tikwmapi.com/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(TikWmApi::class.java)

    private val nazeYoutubeApi: NazeYoutubeApi = Retrofit.Builder()
        .baseUrl("https://naze.dev/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(NazeYoutubeApi::class.java)

    suspend fun processClipboardText(rawText: String): DownloadDetectionResult = withContext(Dispatchers.IO) {
        val cleanText = rawText.trim()
        if (cleanText.isEmpty()) {
            return@withContext DownloadDetectionResult.InvalidLink
        }

        val tiktokUrl = TikWmService.extractTikTokUrl(cleanText)
        val youtubeUrl = NazeYoutubeService.extractYouTubeUrl(cleanText)

        when {
            tiktokUrl != null -> processTikTokDownload(TikWmService.resolveFinalUrl(tiktokUrl))
            youtubeUrl != null -> processYouTubeDownload(youtubeUrl)
            else -> DownloadDetectionResult.InvalidLink
        }
    }

    private suspend fun processTikTokDownload(targetUrl: String): DownloadDetectionResult {
        return try {
            showToast("processYouTubeDownload" + targetUrl)
            val apiKey = TikWmService.getActiveApiKey()
            val response = tikWmApi.fetchTikTok(
                url = targetUrl,
                hd = "0", // Tidak memprioritaskan HD, ambil SD biasa no-watermark
                apiKey = apiKey
            )

            if (!response.isSuccessful) {
                return DownloadDetectionResult.Error("HTTP ${response.code()}: Gagal menghubungi server TikTok")
            }

            val body = response.body()
            val data = body?.data
            if (body == null || body.msg != "success" || data == null) {
                val err = body?.error ?: body?.msg ?: "Gagal memproses link TikTok"
                return DownloadDetectionResult.Error(err)
            }

            val baseId = TikWmService.resolveMediaId(data)
            val title = data.title?.takeIf { it.isNotBlank() } ?: "TikTok Video $baseId"

            val images = data.images.orEmpty().filter { it.isNotBlank() }
            if (images.isNotEmpty()) {
                // Unduh foto slide pertama / semua foto slide
                images.forEachIndexed { index, imgUrl ->
                    val fileName = "img-$baseId-slide${index + 1}.jpg"
                    enqueueDownload(
                        url = imgUrl,
                        fileName = fileName,
                        title = "$title (Slide ${index + 1})",
                        thumbnailUrl = imgUrl,
                        sourcePlatform = "TIKTOK",
                        mediaType = "IMAGE",
                        userOriginalUrl = targetUrl
                    )
                }
                DownloadDetectionResult.Success(
                    title = title,
                    fileName = "img-$baseId (Slide 1..${images.size})",
                    platform = "TIKTOK"
                )
            } else {
                // Video Biasa Tanpa Watermark (SD No-Watermark via data.play)
                val sdPlayUrl = data.play?.takeIf { it.isNotBlank() }
                    ?: data.wmplay?.takeIf { it.isNotBlank() }
                    ?: data.hdplay?.takeIf { it.isNotBlank() }

                if (sdPlayUrl.isNullOrBlank()) {
                    return DownloadDetectionResult.Error("Link unduhan video TikTok tidak ditemukan")
                }

                val fileName = "sd-$baseId.mp4"
                enqueueDownload(
                    url = sdPlayUrl,
                    fileName = fileName,
                    title = title,
                    thumbnailUrl = data.cover ?: data.originCover,
                    sourcePlatform = "TIKTOK",
                    mediaType = "VIDEO",
                    userOriginalUrl = targetUrl
                )

                DownloadDetectionResult.Success(
                    title = title,
                    fileName = fileName,
                    platform = "TIKTOK"
                )
            }
        } catch (e: Exception) {
            DownloadDetectionResult.Error("Kesalahan TikTok: ${e.message ?: "Koneksi terputus"}")
        }
    }

    private suspend fun processYouTubeDownload(targetUrl: String): DownloadDetectionResult {
        return try {
            val apiKey = NazeYoutubeService.getActiveApiKey()
            val response = nazeYoutubeApi.fetchYtMp4(
                url = targetUrl,
                quality = "720",
                apiKey = apiKey
            )

            if (!response.isSuccessful) {
                return DownloadDetectionResult.Error("HTTP ${response.code()}: Gagal menghubungi server YouTube")
            }

            val body = response.body()
            val result = body?.result
            if (body == null || body.success != true || result == null) {
                val err = body?.error ?: "Gagal memproses video YouTube"
                return DownloadDetectionResult.Error(err)
            }

            val downloadUrl = result.downloadUrl?.takeIf { it.isNotBlank() }
            if (downloadUrl.isNullOrBlank()) {
                return DownloadDetectionResult.Error("Link unduh video YouTube tidak tersedia")
            }

            val rawTitle = result.title?.trim()?.takeIf { it.isNotEmpty() } ?: "YouTube Video"
            val sanitizedTitle = rawTitle.replace(Regex("[^a-zA-Z0-9._ -]"), "_")
            val fileName = "${sanitizedTitle.take(50)}.mp4"

            enqueueDownload(
                url = downloadUrl,
                fileName = fileName,
                title = rawTitle,
                thumbnailUrl = null,
                sourcePlatform = "YOUTUBE",
                mediaType = "VIDEO",
                userOriginalUrl = targetUrl
            )

            DownloadDetectionResult.Success(
                title = rawTitle,
                fileName = fileName,
                platform = "YOUTUBE"
            )
        } catch (e: Exception) {
            DownloadDetectionResult.Error("Kesalahan YouTube: ${e.message ?: "Koneksi terputus"}")
        }
    }

    private fun enqueueDownload(
        url: String,
        fileName: String,
        title: String,
        thumbnailUrl: String?,
        sourcePlatform: String,
        mediaType: String,
        userOriginalUrl: String
    ) {
        try {
            val request = DownloadManager.Request(Uri.parse(url))
                .setTitle(fileName)
                .setDescription("Mengunduh media $title")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

            val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
            dm?.enqueue(request)

            // Catat ke Room Database dengan link yang dikirim pengguna (bukan link hasil dari API)
            DownloadedMediaRepository.recordDownload(
                context = context,
                title = title,
                mediaType = mediaType,
                sourcePlatform = sourcePlatform,
                downloadUrl = userOriginalUrl.ifBlank { url },
                thumbnailUrl = thumbnailUrl,
                fileSizeFormatted = null,
                durationFormatted = null,
                filePath = "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath}/$fileName"
            )
        } catch (e: Exception) {
            showToast("Gagal memasukkan ke antrean unduh: ${e.message}")
        }
    }

    private fun showToast(msg: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }
}
