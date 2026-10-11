package com.ehan.app3.bot.tiktok

import android.content.Context
import com.ehan.app3.App3
import com.ehan.app3.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.NumberFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class TikWmService {

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .build()

    private val api: TikWmApi = Retrofit.Builder()
        .baseUrl("https://api.tikwmapi.com/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(TikWmApi::class.java)

    suspend fun downloadAndFormat(rawUrl: String): String {
        val cleanUrl = extractTikTokUrl(rawUrl)
        if (cleanUrl == null) {
            return """
                ❌ URL TikTok tidak valid.

                Contoh penggunaan:
                /tiktok https://vt.tiktok.com/ZSxxxxxx/
                atau
                /download https://www.tiktok.com/@user/video/123456
            """.trimIndent()
        }

        val apiKey = getActiveApiKey()
        val isPlaceholderKey = apiKey.isEmpty()

        return try {
            val response = api.fetchTikTok(
                url = cleanUrl,
                hd = "1",
                apiKey = apiKey
            )

            if (!response.isSuccessful) {
                val keyHint = if (isPlaceholderKey) {
                    "\n\n💡 API Key belum diatur! Anda bisa mengaturnya dengan salah satu cara:\n" +
                        "1. Ketik langsung di chat: /apikey <KEY_TIKWM_ANDA>\n" +
                        "2. Di GitHub: Settings -> Secrets and variables -> Actions -> New repository secret dengan nama TIKWM_API_KEY\n" +
                        "3. Di AI Studio: masukkan TIKWM_API_KEY di panel Secrets."
                } else {
                    ""
                }
                return "❌ HTTP ${response.code()}: ${response.message()}$keyHint"
            }

            val hasil = response.body()
            if (hasil == null) {
                return "❌ Gagal membaca response JSON dari server TikWM."
            }

            if (hasil.msg != "success" || hasil.data == null) {
                val errDetail = hasil.error ?: hasil.msg ?: "Unknown error"
                val keyHint = if (isPlaceholderKey) {
                    "\n\n💡 API Key belum diatur! Ketik:\n/apikey <KEY_TIKWM_ANDA>\natau tambahkan secret TIKWM_API_KEY di GitHub Actions / AI Studio."
                } else {
                    ""
                }
                return "❌ Gagal mengambil data TikTok: $errDetail$keyHint"
            }

            formatSuccessResult(hasil.data, hasil.processedTime)
        } catch (e: Exception) {
            "❌ Gagal menghubungi server TikTok Downloader: ${e.message ?: "Unknown error"}"
        }
    }

    private fun formatSuccessResult(data: TikWmData, processedTime: Double?): String {
        val sb = StringBuilder()
        val isSlide = data.isPhotoSlide

        sb.appendLine(if (isSlide) "📸 TIKTOK PHOTO SLIDE DOWNLOADER" else "🎬 TIKTOK VIDEO DOWNLOADER")
        sb.appendLine("━━━━━━━━━━━━━━━━━━━━")

        val authorName = data.author?.nickname ?: "Unknown"
        val authorHandle = data.author?.uniqueId?.let { "@$it" } ?: ""
        sb.appendLine("👤 Kreator: $authorName $authorHandle".trim())

        if (!data.region.isNullOrBlank()) {
            sb.appendLine("🌍 Region: ${data.region}")
        }

        if (!data.title.isNullOrBlank()) {
            sb.appendLine("📝 Judul: ${data.title.trim()}")
        }

        if (!isSlide && (data.duration ?: 0) > 0) {
            sb.appendLine("⏱️ Durasi: ${data.duration} detik")
        } else if (isSlide) {
            sb.appendLine("🖼️ Jumlah Slide: ${data.images?.size ?: 0} foto")
        }

        sb.appendLine()
        sb.appendLine("📊 Statistik:")
        sb.appendLine("👁️ ${formatNumber(data.playCount)} views • ❤️ ${formatNumber(data.diggCount)} likes")
        sb.appendLine("💬 ${formatNumber(data.commentCount)} komentar • 🔄 ${formatNumber(data.shareCount)} shares • 🔖 ${formatNumber(data.collectCount)} disimpan")

        data.musicInfo?.let { music ->
            sb.appendLine()
            sb.appendLine("🎵 Musik: ${music.title ?: "-"} (${music.author ?: "-"})")
        }

        if (!data.cover.isNullOrBlank()) {
            sb.appendLine()
            sb.appendLine("🖼️ [Thumbnail Cover](${data.cover})")
        }

        sb.appendLine()
        sb.appendLine("📥 LINK UNDUHAN:")

        if (isSlide) {
            data.images?.forEachIndexed { index, imgUrl ->
                sb.appendLine("• [Unduh Foto Slide #${index + 1}]($imgUrl)")
            }
            val validLiveImages = data.liveImages?.filterNotNull()?.filter { it.isNotBlank() }.orEmpty()
            validLiveImages.forEachIndexed { index, liveUrl ->
                sb.appendLine("• [Unduh Live Photo #${index + 1} (MP4)]($liveUrl)")
            }
        } else {
            if (!data.hdplay.isNullOrBlank()) {
                val hdSizeStr = formatBytes(data.hdSize)
                sb.appendLine("• [Unduh Video HD (No Watermark)$hdSizeStr](${data.hdplay})")
            }
            if (!data.play.isNullOrBlank() && data.play != data.hdplay) {
                val sdSizeStr = formatBytes(data.size)
                sb.appendLine("• [Unduh Video SD (No Watermark)$sdSizeStr](${data.play})")
            }
            if (!data.wmplay.isNullOrBlank() && data.wmplay != data.play) {
                val wmSizeStr = formatBytes(data.wmSize)
                sb.appendLine("• [Unduh Video (Watermark)$wmSizeStr](${data.wmplay})")
            }
        }

        val musicUrl = data.music?.takeIf { it.isNotBlank() } ?: data.musicInfo?.play?.takeIf { it.isNotBlank() }
        if (!musicUrl.isNullOrBlank()) {
            sb.appendLine("• [Unduh Audio / Musik (MP3)]($musicUrl)")
        }

        if (processedTime != null) {
            sb.appendLine()
            sb.append("⚡ Diproses dalam ${processedTime}s")
        }

        return sb.toString().trim()
    }

    private fun formatNumber(value: Long?): String {
        if (value == null) return "0"
        return NumberFormat.getNumberInstance(Locale("id", "ID")).format(value)
    }

    private fun formatBytes(bytes: Long?): String {
        if (bytes == null || bytes <= 0L) return ""
        val mb = bytes.toDouble() / (1024.0 * 1024.0)
        return String.format(Locale.US, " (%.2f MB)", mb)
    }

    companion object {
        private const val PREFS_NAME = "tikwm_config_prefs"
        private const val KEY_CUSTOM_API_KEY = "custom_tikwm_api_key"
        private var inMemoryKey: String? = null

        private val URL_REGEX = Regex("""https?://[^\s]+""")

        fun saveCustomApiKey(newKey: String) {
            val clean = newKey.trim()
            inMemoryKey = clean
            try {
                val prefs = App3.instance.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                prefs.edit().putString(KEY_CUSTOM_API_KEY, clean).apply()
            } catch (_: Exception) {
            }
        }

        fun getActiveApiKey(): String {
            val savedKey = try {
                val prefs = App3.instance.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                prefs.getString(KEY_CUSTOM_API_KEY, null)?.trim()
            } catch (_: Exception) {
                inMemoryKey
            }

            if (!savedKey.isNullOrEmpty()) {
                return savedKey
            }

            val buildConfigKey = BuildConfig.TIKWM_API_KEY.trim()
            return if (buildConfigKey.isNotEmpty() && buildConfigKey != "YOUR_TIKWM_API_KEY") {
                buildConfigKey
            } else {
                ""
            }
        }

        fun extractTikTokUrl(input: String): String? {
            val match = URL_REGEX.find(input)?.value ?: return null
            val lower = match.lowercase()
            return if (lower.contains("tiktok.com")) {
                match
            } else {
                null
            }
        }

        fun containsTikTokUrl(input: String): Boolean {
            return extractTikTokUrl(input) != null
        }
    }
}
