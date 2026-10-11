package com.ehan.app3.bot.command.commands

import com.ehan.app3.bot.ai.AiProviderManager
import com.ehan.app3.bot.ai.BackendAiProvider
import com.ehan.app3.bot.command.BotCommand
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import kotlin.coroutines.cancellation.CancellationException

class StatusCommand(
    private val aiProviderManager: AiProviderManager
) : BotCommand {

    override val name = "status"

    override val description =
        "Mengecek provider AI dan koneksi backend"

    override val category =
        "Tools"

    // Timeout pendek supaya chat tidak menggantung
    // kalau server Node.js di Termux belum dijalankan.
    private val client by lazy {
        OkHttpClient.Builder()
            .connectTimeout(3, TimeUnit.SECONDS)
            .readTimeout(3, TimeUnit.SECONDS)
            .callTimeout(5, TimeUnit.SECONDS)
            .build()
    }

    override suspend fun execute(argument: String?): String {

        val lines = mutableListOf<String>()

        lines += "📊 STATUS BOT"
        lines += ""
        lines += "🤖 Provider aktif: ${aiProviderManager.getActiveProviderName()}"

        val backends =
            aiProviderManager.getProviders()
                .filterIsInstance<BackendAiProvider>()

        if (backends.isEmpty()) {
            lines += ""
            lines += "🌐 Backend: tidak ada provider backend."
        }

        for (backend in backends) {
            lines += ""
            lines += "🌐 Backend (${backend.name})"
            lines += "URL: ${backend.endpoint}"
            lines += checkHealth(backend.endpoint)
        }

        return lines.joinToString("\n")
    }

    private suspend fun checkHealth(
        endpoint: String
    ): List<String> {

        val healthUrl =
            endpoint.trimEnd('/') + "/health"

        return try {

            val startNanos =
                System.nanoTime()

            val code =
                withContext(Dispatchers.IO) {

                    val request =
                        Request.Builder()
                            .url(healthUrl)
                            .build()

                    client.newCall(request).execute().use { response ->
                        response.code
                    }
                }

            val durationMs =
                (System.nanoTime() - startNanos) / 1_000_000

            when {
                code in 200..299 ->
                    listOf(
                        "Status: ✅ Aktif (HTTP $code, $durationMs ms)"
                    )

                code == 404 ->
                    listOf(
                        "Status: ⚠️ Server hidup, tapi route /health tidak ditemukan (HTTP 404)"
                    )

                else ->
                    listOf(
                        "Status: ⚠️ Server merespons dengan error (HTTP $code)",
                        "Cek log server Node.js di Termux."
                    )
            }

        } catch (exception: CancellationException) {

            throw exception

        } catch (exception: Exception) {

            // Jangan sampai throw ke atas: ChatRepository akan
            // mengembalikan pesan ke PENDING kalau command error.
            listOf(
                "Status: ❌ Tidak terjangkau",
                "Penyebab: ${exception::class.simpleName}: ${exception.message ?: "-"}",
                "",
                "Cek:",
                "• Server Node.js di Termux sudah dijalankan?",
                "• IP dan port di atas masih benar? IP Wi-Fi bisa berubah."
            )
        }
    }
}