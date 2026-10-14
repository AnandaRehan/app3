package com.ehan.app3.bot.tiktok

import android.content.Context
import com.ehan.app3.App3
import com.ehan.app3.BuildConfig
import com.ehan.app3.bot.baileys.BaileysSocket
import com.ehan.app3.bot.baileys.BotButton
import com.ehan.app3.bot.baileys.BotMessageContent
import com.ehan.app3.bot.baileys.ButtonText
import com.ehan.app3.bot.baileys.MediaSource
import com.ehan.app3.bot.baileys.SendMessageOptions
import com.ehan.app3.bot.baileys.SlideMediaItem
import com.ehan.app3.bot.baileys.SlideMediaType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.max

class TikWmService {

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    private val api: TikWmApi = Retrofit.Builder()
        .baseUrl("https://api.tikwmapi.com/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(TikWmApi::class.java)

    /**
     * Menyelesaikan redirect untuk shortlink seperti tt.site atau vt.tiktok.com
     * agar mendapatkan full URL TikTok standar jika diperlukan oleh backend TikWM.
     */

     suspend fun resolveFinalUrl(url: String): String {
            val lower = url.lowercase()
            if (!lower.contains("tt.site") && !lower.contains("vt.tiktok.com") && !lower.contains("vm.tiktok.com")) {
                return url
            }
            return withContext(Dispatchers.IO) {
                try {
                    val request = Request.Builder()
                        .url(url)
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                        .build()
                    okHttpClient.newCall(request).execute().use { response ->
                        val finalUrl = response.request.url.toString()
                        if (finalUrl.isNotBlank()) finalUrl else url
                    }
                } catch (_: Exception) {
                        url
                }
            }
     }

    /**
     * Mengambil data TikTok dari API dan langsung mengirimkannya via `sock.sendMessage`
     * ala @WhiskeySockets/baileys:
     * - Nama file Video: ("hd" atau "sd") + "-" + (data.id ?: data.aweme_id ?: timestamp) + ".mp4"
     * - Nama file Audio: (data.music_info.id ?: timestamp) + ".mp3"
     * - Nama file Gambar/Slide: "img-" + baseId + (-slideN) + ".jpg"
     */
    suspend fun sendTikTokMessage(
        sock: BaileysSocket,
        jid: String,
        rawUrl: String,
        options: SendMessageOptions = SendMessageOptions()
    ) {
        val cleanUrl = extractTikTokUrl(rawUrl)
        if (cleanUrl == null) {
            sock.sendMessage(
                jid = jid,
                content = BotMessageContent(
                    text = """
                        ❌ URL TikTok tidak valid.

                        Contoh penggunaan:
                        • /tiktok https://vt.tiktok.com/ZSxxxxxx/
                        • /tiktok https://www.tt.site/t/ZSbEW9ejA/
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

        val targetUrl = resolveFinalUrl(cleanUrl)

        val apiKey = getActiveApiKey()
        val isPlaceholderKey = apiKey.isEmpty()

        try {
            var response = api.fetchTikTok(
                url = targetUrl,
                hd = "1",
                apiKey = apiKey
            )

            if (!response.isSuccessful && targetUrl != cleanUrl) {
                val fallbackResponse = api.fetchTikTok(
                    url = cleanUrl,
                    hd = "1",
                    apiKey = apiKey
                )
                if (fallbackResponse.isSuccessful) {
                    response = fallbackResponse
                }
            }

            if (!response.isSuccessful) {
                val keyHint = if (isPlaceholderKey) {
                    "\n\n💡 API Key belum diatur! Ketik langsung di chat:\n/apikey <KEY_TIKWM_ANDA>"
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

            val hasil = response.body()
            if (hasil == null || hasil.msg != "success" || hasil.data == null) {
                val errDetail = hasil?.error ?: hasil?.msg ?: "Unknown error"
                val keyHint = if (isPlaceholderKey) {
                    "\n\n💡 Ketik di chat:\n/apikey <KEY_TIKWM_ANDA>\nuntuk memasang API key."
                } else {
                    ""
                }
                sock.sendMessage(
                    jid = jid,
                    content = BotMessageContent(
                        text = "❌ Gagal mengambil data TikTok: $errDetail$keyHint"
                    ),
                    options = options
                )
                return
            }

            val data = hasil.data
            val captionText = buildTikTokCaption(data) + "\n" + targetUrl + "\n" + cleanUrl + "\n" + rawUrl

            // ID utama untuk penamaan file: data.id -> data.aweme_id -> System.currentTimeMillis() (Date.now())
            val baseMediaId = resolveMediaId(data)
            val audioFileName = resolveAudioFileName(data)

            val musicUrl = data.music?.takeIf { it.isNotBlank() }
                ?: data.musicInfo?.play?.takeIf { it.isNotBlank() }

            val images = data.images.orEmpty().filter { it.isNotBlank() }
            val liveImages = data.liveImages.orEmpty()
            val hasAnyLiveVideo = liveImages.any { !it.isNullOrBlank() }

            when {
                // Kasus 1: Hanya 1 Foto dan bukan Live Video -> Kirim Foto tunggal (image)
                images.size == 1 && !hasAnyLiveVideo -> {
                    val photoUrl = images.first()
                    val singlePhotoFileName = "img-$baseMediaId.jpg"
                    val buttons = buildList {
                        add(
                            BotButton(
                                buttonId = "dl_photo_1",
                                buttonText = ButtonText("⬇️ Unduh Foto ($singlePhotoFileName)"),
                                type = BotButton.TYPE_URL_DOWNLOAD,
                                url = photoUrl,
                                fileName = singlePhotoFileName,
                                originalUrl = cleanUrl
                            )
                        )
                        if (!musicUrl.isNullOrBlank()) {
                            add(
                                BotButton(
                                    buttonId = "dl_audio",
                                    buttonText = ButtonText("🎵 Unduh Audio ($audioFileName)"),
                                    type = BotButton.TYPE_URL_DOWNLOAD,
                                    url = musicUrl,
                                    fileName = audioFileName,
                                    originalUrl = cleanUrl
                                )
                            )
                        }
                    }

                    sock.sendMessage(
                        jid = jid,
                        content = BotMessageContent(
                            image = MediaSource(
                                url = photoUrl,
                                thumbnailUrl = data.cover ?: photoUrl,
                                mimeType = "image/jpeg",
                                fileName = singlePhotoFileName
                            ),
                            audio = musicUrl?.let {
                                MediaSource(
                                    url = it,
                                    mimeType = "audio/mpeg",
                                    fileName = audioFileName
                                )
                            },
                            caption = captionText,
                            footer = "📸 TikTok Photo • sock.sendMessage",
                            buttons = buttons,
                            sourceUrl = cleanUrl
                        ),
                        options = options
                    )
                }

                // Kasus 2: Slide Foto / Gabungan Foto & Live Video -> Kirim Carousel Slide menyatu
                images.isNotEmpty() || hasAnyLiveVideo -> {
                    val totalSlides = max(images.size, liveImages.size)
                    val slideItems = (0 until totalSlides).mapNotNull { index ->
                        val slideNo = index + 1
                        val imgUrl = images.getOrNull(index)?.takeIf { it.isNotBlank() }
                        val liveVidUrl = liveImages.getOrNull(index)?.takeIf { it.isNotBlank() }

                        val imgFileName = "img-$baseMediaId-slide$slideNo.jpg"
                        val vidFileName = "hd-$baseMediaId-slide$slideNo.mp4"

                        when {
                            liveVidUrl != null -> SlideMediaItem(
                                type = SlideMediaType.VIDEO,
                                url = liveVidUrl,
                                imageUrl = imgUrl ?: data.cover,
                                videoUrl = liveVidUrl,
                                title = "Slide $slideNo/$totalSlides • Live Video",
                                fileName = vidFileName,
                                imageFileName = imgFileName,
                                videoFileName = vidFileName
                            )
                            imgUrl != null -> SlideMediaItem(
                                type = SlideMediaType.IMAGE,
                                url = imgUrl,
                                imageUrl = imgUrl,
                                videoUrl = null,
                                title = "Slide $slideNo/$totalSlides • Foto",
                                fileName = imgFileName,
                                imageFileName = imgFileName,
                                videoFileName = null
                            )
                            else -> null
                        }
                    }

                    val buttons = buildList {
                        if (!musicUrl.isNullOrBlank()) {
                            add(
                                BotButton(
                                    buttonId = "dl_audio",
                                    buttonText = ButtonText("🎵 Unduh Audio ($audioFileName)"),
                                    type = BotButton.TYPE_URL_DOWNLOAD,
                                    url = musicUrl,
                                    fileName = audioFileName,
                                    originalUrl = cleanUrl
                                )
                            )
                        }
                        add(
                            BotButton(
                                buttonId = "/menu",
                                buttonText = ButtonText("📋 Menu Bot"),
                                type = BotButton.TYPE_REPLY
                            )
                        )
                    }

                    sock.sendMessage(
                        jid = jid,
                        content = BotMessageContent(
                            carousel = slideItems,
                            audio = musicUrl?.let {
                                MediaSource(
                                    url = it,
                                    mimeType = "audio/mpeg",
                                    fileName = audioFileName
                                )
                            },
                            caption = captionText,
                            footer = "🖼️ Geser untuk lihat semua slide (${slideItems.size} media foto/video)",
                            buttons = buttons,
                            sourceUrl = cleanUrl
                        ),
                        options = options
                    )
                }

                // Kasus 3: Video TikTok -> Kirim Video + Caption dengan nama file hd-<id>.mp4 dan sd-<id>.mp4
                else -> {
                    val hdUrl = data.hdplay?.takeIf { it.isNotBlank() }
                    val sdUrl = data.play?.takeIf { it.isNotBlank() } ?: data.wmplay?.takeIf { it.isNotBlank() }

                    val primaryVideoUrl = hdUrl ?: sdUrl.orEmpty()
                    val isPrimaryHd = hdUrl != null
                    val primaryVideoFileName = if (isPrimaryHd) {
                        "hd-$baseMediaId.mp4"
                    } else {
                        "sd-$baseMediaId.mp4"
                    }

                    val hdFileName = "hd-$baseMediaId.mp4"
                    val sdFileName = "sd-$baseMediaId.mp4"

                    val buttons = buildList {
                        if (hdUrl != null) {
                            add(
                                BotButton(
                                    buttonId = "dl_video_hd",
                                    buttonText = ButtonText("⬇️ Unduh Video HD ($hdFileName)"),
                                    type = BotButton.TYPE_URL_DOWNLOAD,
                                    url = hdUrl,
                                    fileName = hdFileName,
                                    originalUrl = cleanUrl
                                )
                            )
                        }
                        if (sdUrl != null && sdUrl != hdUrl) {
                            add(
                                BotButton(
                                    buttonId = "dl_video_sd",
                                    buttonText = ButtonText("⬇️ Unduh Video SD ($sdFileName)"),
                                    type = BotButton.TYPE_URL_DOWNLOAD,
                                    url = sdUrl,
                                    fileName = sdFileName,
                                    originalUrl = cleanUrl
                                )
                            )
                        } else if (hdUrl == null && sdUrl != null) {
                            add(
                                BotButton(
                                    buttonId = "dl_video_sd",
                                    buttonText = ButtonText("⬇️ Unduh Video SD ($sdFileName)"),
                                    type = BotButton.TYPE_URL_DOWNLOAD,
                                    url = sdUrl,
                                    fileName = sdFileName,
                                    originalUrl = cleanUrl
                                )
                            )
                        }
                        if (!musicUrl.isNullOrBlank()) {
                            add(
                                BotButton(
                                    buttonId = "dl_audio",
                                    buttonText = ButtonText("🎵 Unduh Audio ($audioFileName)"),
                                    type = BotButton.TYPE_URL_DOWNLOAD,
                                    url = musicUrl,
                                    fileName = audioFileName,
                                    originalUrl = cleanUrl
                                )
                            )
                        }
                    }

                    sock.sendMessage(
                        jid = jid,
                        content = BotMessageContent(
                            video = MediaSource(
                                url = primaryVideoUrl,
                                thumbnailUrl = data.cover ?: data.originCover,
                                mimeType = "video/mp4",
                                fileName = primaryVideoFileName
                            ),
                            audio = musicUrl?.let {
                                MediaSource(
                                    url = it,
                                    mimeType = "audio/mpeg",
                                    fileName = audioFileName
                                )
                            },
                            caption = captionText,
                            footer = "🎬 TikTok Video • sock.sendMessage",
                            buttons = buttons,
                            sourceUrl = cleanUrl
                        ),
                        options = options
                    )
                }
            }
        } catch (e: Exception) {
            sock.sendMessage(
                jid = jid,
                content = BotMessageContent(
                    text = "❌ Gagal menghubungi server TikTok Downloader: ${e.message ?: "Unknown error"}"
                ),
                options = options
            )
        }
    }

    suspend fun downloadAndFormat(rawUrl: String): String {
        var fallbackResult = ""
        val tempSock = BaileysSocket { msg ->
            fallbackResult = msg.text
        }
        sendTikTokMessage(
            sock = tempSock,
            jid = BaileysSocket.DEFAULT_CHAT_JID,
            rawUrl = rawUrl
        )
        return fallbackResult
    }

    /**
     * Format teks balasan sesuai permintaan:
     * Kreator, Region, Durasi, Buat video nya kapan (Rabu, 21 Januari  pukul 23.34.42),
     * terus Caption (judul).
     */
    fun buildTikTokCaption(data: TikWmData): String {
        val authorNickname = data.author?.nickname?.takeIf { it.isNotBlank() } ?: "Unknown"
        val authorUsername = data.author?.uniqueId?.takeIf { it.isNotBlank() }?.let { "(@$it)" } ?: ""
        val kreator = "$authorNickname $authorUsername".trim()

        val region = data.region?.takeIf { it.isNotBlank() } ?: "-"
        val durasi = when {
            (data.duration ?: 0) > 0 -> "${data.duration} detik"
            !data.images.isNullOrEmpty() -> "${data.images.size} slide"
            else -> "0 detik"
        }
        val dibuatKapan = formatCreateTime(data.createTime)
        val captionJudul = data.title?.takeIf { it.isNotBlank() }
            ?: data.contentDesc?.joinToString(" ")?.takeIf { it.isNotBlank() }
            ?: "-"

        return """
            👤 Kreator : $kreator
            🌍 Region : $region
            ⏱️ Durasi : $durasi
            📅 Dibuat : $dibuatKapan

            📝 Caption :
            ${captionJudul.trim()}
        """.trimIndent()
    }

    companion object {
        private const val PREFS_NAME = "tikwm_config_prefs"
        private const val KEY_CUSTOM_API_KEY = "custom_tikwm_api_key"
        private var inMemoryKey: String? = null

        private val URL_REGEX = Regex("""https?://[^\s]+""")

        /**
         * Resolusi ID media: data.id -> data.aweme_id -> System.currentTimeMillis()
         */
        fun resolveMediaId(data: TikWmData): String {
            return data.id?.trim()?.takeIf { it.isNotEmpty() }
                ?: data.awemeId?.trim()?.takeIf { it.isNotEmpty() }
                ?: System.currentTimeMillis().toString()
        }

        /**
         * Resolusi nama file audio: (data.music_info.id ?: System.currentTimeMillis()) + ".mp3"
         */
        fun resolveAudioFileName(data: TikWmData): String {
            val musicId = data.musicInfo?.id?.trim()?.takeIf { it.isNotEmpty() }
                ?: System.currentTimeMillis().toString()
            return "$musicId.mp3"
        }

        /**
         * Contoh output: "Rabu, 21 Januari  pukul 23.34.42"
         */
        fun formatCreateTime(epochSeconds: Long?): String {
            if (epochSeconds == null || epochSeconds <= 0L) return "-"
            val date = Date(epochSeconds * 1000L)
            val localeId = Locale("id", "ID")
            val hari = SimpleDateFormat("EEEE", localeId).format(date)
            val tanggalBulan = SimpleDateFormat("d MMMM", localeId).format(date)
            val pukul = SimpleDateFormat("HH.mm.ss", localeId).format(date)
            return "$hari, $tanggalBulan  pukul $pukul"
        }

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
            if (input.isBlank()) return null
            val matches = URL_REGEX.findAll(input)
            for (m in matches) {
                val cleanUrl = m.value.trim().trimEnd('.', ',', ')', ']', '}', '"', '\'', ';', ':')
                val lower = cleanUrl.lowercase()
                if (
                    lower.contains("tiktok.com") ||
                    lower.contains("tt.site") ||
                    lower.contains("tiktokv.com") ||
                    lower.contains("douyin.com")
                ) {
                    return cleanUrl
                }
            }
            // Dukung jika pengguna menyalin link tanpa awalan https://
            val fallbackRegex = Regex("""(?i)\b(?:www\.|vt\.|vm\.|v\.)?(?:tiktok\.com|tt\.site|tiktokv\.com|douyin\.com)/[^\s]+""")
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

        fun containsTikTokUrl(input: String): Boolean {
            return extractTikTokUrl(input) != null
        }
    }
}
