package com.ehan.app3.bot.youtube

import android.content.Context
import com.ehan.app3.App3
import com.ehan.app3.BuildConfig
import com.ehan.app3.bot.baileys.BaileysSocket
import com.ehan.app3.bot.baileys.BotButton
import com.ehan.app3.bot.baileys.BotMessageContent
import com.ehan.app3.bot.baileys.ButtonText
import com.ehan.app3.bot.baileys.MediaSource
import com.ehan.app3.bot.baileys.SendMessageOptions
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

class NazeYoutubeService {

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(25, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val api: NazeYoutubeApi = Retrofit.Builder()
        .baseUrl("https://naze.dev/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(NazeYoutubeApi::class.java)

    /**
     * Download Video YouTube (ytmp4 720p) + menyiapkan tombol Download Audio (ytmp3-flvto)
     * menggunakan sistem sock.sendMessage ala @WhiskeySockets/baileys.
     */
    suspend fun sendYouTubeVideoMessage(
        sock: BaileysSocket,
        jid: String,
        rawUrl: String,
        options: SendMessageOptions = SendMessageOptions()
    ) {
        val cleanUrl = extractYouTubeUrl(rawUrl)
        if (cleanUrl == null) {
            sock.sendMessage(
                jid = jid,
                content = BotMessageContent(
                    text = """
                        ❌ URL YouTube tidak valid.

                        Contoh penggunaan:
                        /yt https://youtu.be/Qonuy-9IKxs
                        atau
                        /ytmp3 https://youtu.be/Qonuy-9IKxs
                    """.trimIndent(),
                    buttons = listOf(
                        BotButton(
                            buttonId = "/menu",
                            buttonText = ButtonText("📋 Menu Utama"),
                            type = BotButton.TYPE_REPLY
                        )
                    )
                ),
                options = options
            )
            return
        }

        val apiKey = getActiveApiKey()
        val isPlaceholderKey = apiKey.isEmpty()

        try {
            val (mp4Response, mp3Result) = coroutineScope {
                val mp4Deferred = async {
                    api.fetchYtMp4(
                        url = cleanUrl,
                        quality = "720",
                        apiKey = apiKey
                    )
                }
                val mp3Deferred = async {
                    runCatching {
                        val res = api.fetchYtMp3(
                            url = cleanUrl,
                            quality = "720",
                            apiKey = apiKey
                        )
                        if (res.isSuccessful && res.body()?.success == true) {
                            res.body()?.result
                        } else {
                            null
                        }
                    }.getOrNull()
                }
                mp4Deferred.await() to mp3Deferred.await()
            }

            if (!mp4Response.isSuccessful) {
                val keyHint = if (isPlaceholderKey) {
                    "\n\n💡 API Key Naze belum diatur! Ketik langsung di chat:\n/yt key <KEY_NAZE_ANDA>\natau\n/apikey naze <KEY_NAZE_ANDA>"
                } else {
                    ""
                }
                sock.sendMessage(
                    jid = jid,
                    content = BotMessageContent(
                        text = "❌ HTTP ${mp4Response.code()}: ${mp4Response.message()}$keyHint"
                    ),
                    options = options
                )
                return
            }

            val data = mp4Response.body()
            val result = data?.result
            if (data == null || data.success != true || result == null || result.downloadUrl.isNullOrBlank()) {
                val errDetail = data?.error ?: data?.message ?: "Gagal mengambil link unduhan YouTube"
                val keyHint = if (isPlaceholderKey) {
                    "\n\n💡 Ketik di chat:\n/yt key <KEY_NAZE_ANDA>\nuntuk memasang API key YouTube Downloader."
                } else {
                    ""
                }
                sock.sendMessage(
                    jid = jid,
                    content = BotMessageContent(
                        text = "❌ Gagal mengambil video YouTube: $errDetail$keyHint"
                    ),
                    options = options
                )
                return
            }

            val videoId = extractYouTubeVideoId(cleanUrl) ?: System.currentTimeMillis().toString()
            val ext = result.ext?.takeIf { it.isNotBlank() } ?: "mp4"
            val videoFileName = "yt-720p-$videoId.$ext"
            val audioFileName = "yt-audio-$videoId.mp3"
            val thumbnailUrl = extractYouTubeVideoId(cleanUrl)?.let {
                "https://i.ytimg.com/vi/$it/hqdefault.jpg"
            }

            val title = result.title?.takeIf { it.isNotBlank() }
                ?: mp3Result?.title?.takeIf { it.isNotBlank() }
                ?: "YouTube Video"
            val fpsTag = if (result.is60fps == true) " • 60fps" else ""
            val durationLine = mp3Result?.duration?.takeIf { it > 0 }?.let {
                "\n⏱️ Durasi : ${formatDurationSeconds(it)}"
            } ?: ""
            val audioSizeLine = formatBytes(mp3Result?.filesize).takeIf { it.isNotEmpty() }?.let {
                "\n🎵 Ukuran Audio : ${it.trim()}"
            } ?: ""

            val captionText = """
                ▶️ YOUTUBE DOWNLOADER (720P)
                ━━━━━━━━━━━━━━━━━━━━
                📝 Judul : $title
                🎞️ Kualitas : 720p (${ext.uppercase()})$fpsTag$durationLine$audioSizeLine
            """.trimIndent()

            val mp3Link = mp3Result?.link?.takeIf { it.isNotBlank() }
            val buttons = buildList {
                add(
                    BotButton(
                        buttonId = "dl_yt_mp4",
                        buttonText = ButtonText("⬇️ Unduh Video MP4 ($videoFileName)"),
                        type = BotButton.TYPE_URL_DOWNLOAD,
                        url = result.downloadUrl,
                        fileName = videoFileName
                    )
                )
                if (mp3Link != null) {
                    val sizeBadge = formatBytes(mp3Result.filesize)
                    add(
                        BotButton(
                            buttonId = "dl_yt_mp3",
                            buttonText = ButtonText("🎵 Download Audio MP3$sizeBadge"),
                            type = BotButton.TYPE_URL_DOWNLOAD,
                            url = mp3Link,
                            fileName = audioFileName
                        )
                    )
                } else {
                    add(
                        BotButton(
                            buttonId = "/ytmp3 $cleanUrl",
                            buttonText = ButtonText("🎵 Download Audio (MP3)"),
                            type = BotButton.TYPE_REPLY
                        )
                    )
                }
            }

            sock.sendMessage(
                jid = jid,
                content = BotMessageContent(
                    video = MediaSource(
                        url = result.downloadUrl,
                        thumbnailUrl = thumbnailUrl,
                        mimeType = "video/mp4",
                        fileName = videoFileName
                    ),
                    audio = mp3Link?.let {
                        MediaSource(
                            url = it,
                            mimeType = "audio/mpeg",
                            fileName = audioFileName
                        )
                    },
                    caption = captionText,
                    footer = "▶️ YouTube Downloader • naze.dev API",
                    buttons = buttons
                ),
                options = options
            )
        } catch (e: Exception) {
            sock.sendMessage(
                jid = jid,
                content = BotMessageContent(
                    text = "❌ Gagal menghubungi server YouTube Downloader: ${e.message ?: "Unknown error"}"
                ),
                options = options
            )
        }
    }

    /**
     * Download Audio YouTube (/api/v1/download/ytmp3-flvto)
     */
    suspend fun sendYouTubeAudioMessage(
        sock: BaileysSocket,
        jid: String,
        rawUrl: String,
        options: SendMessageOptions = SendMessageOptions()
    ) {
        val cleanUrl = extractYouTubeUrl(rawUrl)
        if (cleanUrl == null) {
            sock.sendMessage(
                jid = jid,
                content = BotMessageContent(
                    text = "❌ URL YouTube tidak valid.\n\nContoh:\n/ytmp3 https://youtu.be/Qonuy-9IKxs"
                ),
                options = options
            )
            return
        }

        val apiKey = getActiveApiKey()
        val isPlaceholderKey = apiKey.isEmpty()

        try {
            val response = api.fetchYtMp3(
                url = cleanUrl,
                quality = "720",
                apiKey = apiKey
            )

            if (!response.isSuccessful) {
                val keyHint = if (isPlaceholderKey) {
                    "\n\n💡 API Key Naze belum diatur! Ketik:\n/yt key <KEY_NAZE_ANDA>"
                } else {
                    ""
                }
                sock.sendMessage(
                    jid = jid,
                    content = BotMessageContent(
                        text = "❌ HTTP ${response.code()}: ${response.message()}$keyHint"
                    ),
                    options = options
                )
                return
            }

            val data = response.body()
            val result = data?.result
            if (data == null || data.success != true || result == null || result.link.isNullOrBlank()) {
                val errDetail = data?.error ?: data?.message ?: result?.msg ?: "Gagal mengambil audio MP3"
                val keyHint = if (isPlaceholderKey) {
                    "\n\n💡 Ketik di chat:\n/yt key <KEY_NAZE_ANDA>\nuntuk memasang API key."
                } else {
                    ""
                }
                sock.sendMessage(
                    jid = jid,
                    content = BotMessageContent(
                        text = "❌ Gagal mengambil audio YouTube: $errDetail$keyHint"
                    ),
                    options = options
                )
                return
            }

            val videoId = extractYouTubeVideoId(cleanUrl) ?: System.currentTimeMillis().toString()
            val audioFileName = "yt-audio-$videoId.mp3"
            val thumbnailUrl = extractYouTubeVideoId(cleanUrl)?.let {
                "https://i.ytimg.com/vi/$it/hqdefault.jpg"
            }

            val title = result.title?.takeIf { it.isNotBlank() } ?: "YouTube Audio"
            val durationStr = result.duration?.takeIf { it > 0 }?.let { formatDurationSeconds(it) } ?: "-"
            val sizeStr = formatBytes(result.filesize).trim().ifEmpty { "-" }

            val captionText = """
                🎵 YOUTUBE AUDIO MP3
                ━━━━━━━━━━━━━━━━━━━━
                📝 Judul : $title
                ⏱️ Durasi : $durationStr
                📦 Ukuran : $sizeStr
                ✅ Status : ${result.status ?: "ok"}
            """.trimIndent()

            sock.sendMessage(
                jid = jid,
                content = BotMessageContent(
                    image = thumbnailUrl?.let {
                        MediaSource(
                            url = it,
                            thumbnailUrl = it,
                            mimeType = "image/jpeg",
                            fileName = "yt-thumb-$videoId.jpg"
                        )
                    },
                    audio = MediaSource(
                        url = result.link,
                        mimeType = "audio/mpeg",
                        fileName = audioFileName
                    ),
                    caption = captionText,
                    footer = "🎵 YouTube MP3 • /download/ytmp3-flvto",
                    buttons = listOf(
                        BotButton(
                            buttonId = "dl_yt_mp3_direct",
                            buttonText = ButtonText("🎵 Unduh Audio MP3 ($audioFileName)"),
                            type = BotButton.TYPE_URL_DOWNLOAD,
                            url = result.link,
                            fileName = audioFileName
                        ),
                        BotButton(
                            buttonId = "/yt $cleanUrl",
                            buttonText = ButtonText("🎬 Ambil Versi Video MP4"),
                            type = BotButton.TYPE_REPLY
                        )
                    )
                ),
                options = options
            )
        } catch (e: Exception) {
            sock.sendMessage(
                jid = jid,
                content = BotMessageContent(
                    text = "❌ Gagal menghubungi server YouTube MP3: ${e.message ?: "Unknown error"}"
                ),
                options = options
            )
        }
    }

    suspend fun downloadAndFormat(rawUrl: String, audioOnly: Boolean = false): String {
        var fallbackResult = ""
        val tempSock = BaileysSocket { msg ->
            fallbackResult = msg.text
        }
        if (audioOnly) {
            sendYouTubeAudioMessage(
                sock = tempSock,
                jid = BaileysSocket.DEFAULT_CHAT_JID,
                rawUrl = rawUrl
            )
        } else {
            sendYouTubeVideoMessage(
                sock = tempSock,
                jid = BaileysSocket.DEFAULT_CHAT_JID,
                rawUrl = rawUrl
            )
        }
        return fallbackResult
    }

    private fun formatDurationSeconds(seconds: Double): String {
        val totalSec = seconds.roundToInt()
        val mins = totalSec / 60
        val secs = totalSec % 60
        return if (mins > 0) {
            "${mins}m ${secs}s ($totalSec detik)"
        } else {
            "$totalSec detik"
        }
    }

    private fun formatBytes(bytes: Long?): String {
        if (bytes == null || bytes <= 0L) return ""
        val mb = bytes.toDouble() / (1024.0 * 1024.0)
        return String.format(Locale.US, " (%.2f MB)", mb)
    }

    companion object {
        private const val PREFS_NAME = "naze_config_prefs"
        private const val KEY_CUSTOM_NAZE_API_KEY = "custom_naze_api_key"
        private var inMemoryKey: String? = null

        private val URL_REGEX = Regex("""https?://[^\s]+""")
        private val YT_ID_REGEX = Regex("""(?:v=|youtu\.be/|shorts/|embed/)([A-Za-z0-9_-]{11})""")

        fun saveCustomApiKey(newKey: String) {
            val clean = newKey.trim()
            inMemoryKey = clean
            try {
                val prefs = App3.instance.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                prefs.edit().putString(KEY_CUSTOM_NAZE_API_KEY, clean).apply()
            } catch (_: Exception) {
            }
        }

        fun getActiveApiKey(): String {
            val savedKey = try {
                val prefs = App3.instance.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                prefs.getString(KEY_CUSTOM_NAZE_API_KEY, null)?.trim()
            } catch (_: Exception) {
                inMemoryKey
            }

            if (!savedKey.isNullOrEmpty()) {
                return savedKey
            }

            val buildConfigKey = BuildConfig.NAZE_API_KEY.trim()
            return if (buildConfigKey.isNotEmpty() && buildConfigKey != "YOUR_NAZE_API_KEY") {
                buildConfigKey
            } else {
                ""
            }
        }

        fun extractYouTubeUrl(input: String): String? {
            if (input.isBlank()) return null
            val matches = URL_REGEX.findAll(input)
            for (m in matches) {
                val clean = m.value.trim().trimEnd('.', ',', ')', ']', '}', '"', '\'', ';', ':')
                val lower = clean.lowercase()
                if (lower.contains("youtube.com") || lower.contains("youtu.be")) {
                    return clean
                }
            }
            val fallbackRegex = Regex("""(?i)\b(?:www\.|m\.)?(?:youtube\.com|youtu\.be)/[^\s]+""")
            val fallbackMatch = fallbackRegex.find(input)?.value?.trim()?.trimEnd('.', ',', ')', ']', '}', '"', '\'', ';', ':')
            if (fallbackMatch != null) {
                return if (fallbackMatch.startsWith("http://") || fallbackMatch.startsWith("https://")) {
                    fallbackMatch
                } else {
                    "https://$fallbackMatch"
                }
            }
            return null
        }

        fun extractYouTubeVideoId(url: String): String? {
            return YT_ID_REGEX.find(url)?.groupValues?.getOrNull(1)
        }

        fun containsYouTubeUrl(input: String): Boolean {
            return extractYouTubeUrl(input) != null
        }
    }
}
