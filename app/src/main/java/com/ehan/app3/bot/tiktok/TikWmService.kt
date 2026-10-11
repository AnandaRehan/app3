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
import okhttp3.OkHttpClient
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
        .build()

    private val api: TikWmApi = Retrofit.Builder()
        .baseUrl("https://api.tikwmapi.com/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(TikWmApi::class.java)

    /**
     * Mengambil data TikTok dari API dan langsung mengirimkannya via `sock.sendMessage`
     * ala @WhiskeySockets/baileys:
     * - Jika Video -> kirim pesan Video + Caption (Kreator, Region, Durasi, Dibuat, Caption)
     * - Jika 1 Foto -> kirim pesan Foto (Image) + Caption
     * - Jika Slide (banyak foto atau gabungan foto + live_images video) -> kirim Carousel Slide menyatu + Caption
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
                        /tiktok https://vt.tiktok.com/ZSxxxxxx/
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
            val response = api.fetchTikTok(
                url = cleanUrl,
                hd = "1",
                apiKey = apiKey
            )

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
            val captionText = buildTikTokCaption(data)
            val musicUrl = data.music?.takeIf { it.isNotBlank() }
                ?: data.musicInfo?.play?.takeIf { it.isNotBlank() }

            val images = data.images.orEmpty().filter { it.isNotBlank() }
            val liveImages = data.liveImages.orEmpty()
            val hasAnyLiveVideo = liveImages.any { !it.isNullOrBlank() }

            when {
                // Kasus 1: Hanya 1 Foto dan bukan Live Video -> Kirim Foto tunggal (image)
                images.size == 1 && !hasAnyLiveVideo -> {
                    val photoUrl = images.first()
                    val buttons = buildList {
                        add(
                            BotButton(
                                buttonId = "dl_photo_1",
                                buttonText = ButtonText("⬇️ Unduh Foto HD"),
                                type = BotButton.TYPE_URL_DOWNLOAD,
                                url = photoUrl
                            )
                        )
                        if (!musicUrl.isNullOrBlank()) {
                            add(
                                BotButton(
                                    buttonId = "dl_audio",
                                    buttonText = ButtonText("🎵 Unduh Audio MP3"),
                                    type = BotButton.TYPE_URL_DOWNLOAD,
                                    url = musicUrl
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
                                mimeType = "image/jpeg"
                            ),
                            audio = musicUrl?.let { MediaSource(url = it, mimeType = "audio/mpeg") },
                            caption = captionText,
                            footer = "📸 TikTok Photo • sock.sendMessage",
                            buttons = buttons
                        ),
                        options = options
                    )
                }

                // Kasus 2: Slide Foto / Gabungan Foto & Live Video -> Kirim Carousel Slide menyatu
                images.isNotEmpty() || hasAnyLiveVideo -> {
                    val totalSlides = max(images.size, liveImages.size)
                    val slideItems = (0 until totalSlides).mapNotNull { index ->
                        val imgUrl = images.getOrNull(index)?.takeIf { it.isNotBlank() }
                        val liveVidUrl = liveImages.getOrNull(index)?.takeIf { it.isNotBlank() }

                        when {
                            liveVidUrl != null -> SlideMediaItem(
                                type = SlideMediaType.VIDEO,
                                url = liveVidUrl,
                                imageUrl = imgUrl ?: data.cover,
                                videoUrl = liveVidUrl,
                                title = "Slide ${index + 1}/$totalSlides • Live Video"
                            )
                            imgUrl != null -> SlideMediaItem(
                                type = SlideMediaType.IMAGE,
                                url = imgUrl,
                                imageUrl = imgUrl,
                                videoUrl = null,
                                title = "Slide ${index + 1}/$totalSlides • Foto"
                            )
                            else -> null
                        }
                    }

                    val buttons = buildList {
                        if (!musicUrl.isNullOrBlank()) {
                            add(
                                BotButton(
                                    buttonId = "dl_audio",
                                    buttonText = ButtonText("🎵 Unduh Audio MP3"),
                                    type = BotButton.TYPE_URL_DOWNLOAD,
                                    url = musicUrl
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
                            audio = musicUrl?.let { MediaSource(url = it, mimeType = "audio/mpeg") },
                            caption = captionText,
                            footer = "🖼️ Geser untuk lihat semua slide (${slideItems.size} media foto/video)",
                            buttons = buttons
                        ),
                        options = options
                    )
                }

                // Kasus 3: Video TikTok -> Kirim Video + Caption sesuai format yang diminta
                else -> {
                    val videoUrl = data.hdplay?.takeIf { it.isNotBlank() }
                        ?: data.play?.takeIf { it.isNotBlank() }
                        ?: data.wmplay.orEmpty()

                    val buttons = buildList {
                        if (videoUrl.isNotBlank()) {
                            add(
                                BotButton(
                                    buttonId = "dl_video_hd",
                                    buttonText = ButtonText("⬇️ Unduh Video HD"),
                                    type = BotButton.TYPE_URL_DOWNLOAD,
                                    url = videoUrl
                                )
                            )
                        }
                        if (!data.play.isNullOrBlank() && data.play != videoUrl) {
                            add(
                                BotButton(
                                    buttonId = "dl_video_sd",
                                    buttonText = ButtonText("⬇️ Video SD (No WM)"),
                                    type = BotButton.TYPE_URL_DOWNLOAD,
                                    url = data.play
                                )
                            )
                        }
                        if (!musicUrl.isNullOrBlank()) {
                            add(
                                BotButton(
                                    buttonId = "dl_audio",
                                    buttonText = ButtonText("🎵 Unduh Audio MP3"),
                                    type = BotButton.TYPE_URL_DOWNLOAD,
                                    url = musicUrl
                                )
                            )
                        }
                    }

                    sock.sendMessage(
                        jid = jid,
                        content = BotMessageContent(
                            video = MediaSource(
                                url = videoUrl,
                                thumbnailUrl = data.cover ?: data.originCover,
                                mimeType = "video/mp4"
                            ),
                            audio = musicUrl?.let { MediaSource(url = it, mimeType = "audio/mpeg") },
                            caption = captionText,
                            footer = "🎬 TikTok Video HD • sock.sendMessage",
                            buttons = buttons
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
