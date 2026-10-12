package com.ehan.app3.bot.baileys

import com.ehan.app3.data.ChatMessage

/**
 * Representasi sumber media (URL / Thumbnail / MimeType / FileName)
 * mirip konfigurasi media di @WhiskeySockets/baileys: { url: "...", fileName: "..." }
 */
data class MediaSource(
    val url: String,
    val thumbnailUrl: String? = null,
    val mimeType: String? = null,
    val fileName: String? = null
)

enum class SlideMediaType {
    IMAGE,
    VIDEO
}

/**
 * Item untuk Slide / Carousel (bisa gabung Foto & Video Live seperti di TikTok)
 */
data class SlideMediaItem(
    val type: SlideMediaType,
    val url: String,
    val imageUrl: String? = null,
    val videoUrl: String? = null,
    val title: String? = null,
    val fileName: String? = null,
    val imageFileName: String? = null,
    val videoFileName: String? = null
)

/**
 * Teks tombol ala Baileys: buttonText: { displayText: "..." }
 */
data class ButtonText(
    val displayText: String
)

/**
 * Struktur tombol ala @WhiskeySockets/baileys:
 * {
 *   buttonId: "/menu",
 *   buttonText: { displayText: "Menu Utama" },
 *   type: 1, // 1 = Quick Reply Command, 2 = URL / Download Media
 *   url: "https://...",
 *   fileName: "hd-7688592181992541458.mp4"
 * }
 */
data class BotButton(
    val buttonId: String,
    val buttonText: ButtonText,
    val type: Int = 1,
    val url: String? = null,
    val fileName: String? = null
) {
    companion object {
        const val TYPE_REPLY = 1
        const val TYPE_URL_DOWNLOAD = 2
    }
}

/**
 * Payload utama untuk sock.sendMessage(jid, content, options)
 * Mirip AnyMessageContent di @WhiskeySockets/baileys
 */
data class BotMessageContent(
    val text: String? = null,
    val caption: String? = null,
    val footer: String? = null,
    val image: MediaSource? = null,
    val video: MediaSource? = null,
    val audio: MediaSource? = null,
    val carousel: List<SlideMediaItem>? = null,
    val buttons: List<BotButton>? = null,
    val headerType: Int = 1,
    val quotedMessageId: Long? = null,
    val quotedText: String? = null
)

/**
 * Opsi tambahan pada sock.sendMessage(jid, content, options)
 * Mirip MiscMessageGenerationOptions di @WhiskeySockets/baileys: { quoted: msg }
 */
data class SendMessageOptions(
    val quoted: ChatMessage? = null
)
