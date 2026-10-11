package com.ehan.app3.bot.baileys

import com.ehan.app3.data.ChatMessage
import com.google.gson.Gson

/**
 * Implementasi Socket ala @WhiskeySockets/baileys (`sock.sendMessage`).
 * Memungkinkan setiap command / fitur mengirim berbagai tipe pesan:
 * - Text biasa
 * - Video + Caption + Audio + Tombol
 * - Foto Tunggal (Image) + Caption + Tombol
 * - Slide / Carousel (Gabungan Foto & Video menyatu ala TikTok) + Caption + Tombol
 * - Button Message dengan footer & quoted message
 */
class BaileysSocket(
    private val onEmitMessage: suspend (ChatMessage) -> Unit
) {
    private val gson = Gson()
    private val sentMessages = mutableListOf<ChatMessage>()

    val dispatchedMessages: List<ChatMessage>
        get() = sentMessages

    suspend fun sendMessage(
        jid: String = DEFAULT_CHAT_JID,
        content: BotMessageContent,
        options: SendMessageOptions = SendMessageOptions()
    ): ChatMessage {
        val enrichedContent = if (options.quoted != null && content.quotedText == null) {
            content.copy(
                quotedMessageId = options.quoted.id,
                quotedText = options.quoted.text.take(120)
            )
        } else {
            content
        }

        val fallbackDisplayText = enrichedContent.caption
            ?: enrichedContent.text
            ?: when {
                enrichedContent.video != null -> "🎬 Video"
                enrichedContent.image != null -> "📸 Foto"
                !enrichedContent.carousel.isNullOrEmpty() -> "🖼️ Slide (${enrichedContent.carousel.size} media)"
                enrichedContent.audio != null -> "🎵 Audio"
                else -> ""
            }

        val jsonPayload = gson.toJson(enrichedContent)

        val chatMessage = ChatMessage(
            text = fallbackDisplayText,
            isBot = true,
            status = "PROCESSED",
            timestamp = System.currentTimeMillis(),
            payloadJson = jsonPayload
        )

        sentMessages.add(chatMessage)
        onEmitMessage(chatMessage)
        return chatMessage
    }

    companion object {
        const val DEFAULT_CHAT_JID = "user@s.whatsapp.net"
        private val sharedGson = Gson()

        fun parsePayload(payloadJson: String?): BotMessageContent? {
            if (payloadJson.isNullOrBlank()) return null
            return try {
                sharedGson.fromJson(payloadJson, BotMessageContent::class.java)
            } catch (_: Exception) {
                null
            }
        }
    }
}
