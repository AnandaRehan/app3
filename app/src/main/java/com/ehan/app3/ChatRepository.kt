package com.ehan.app3

import com.ehan.app3.bot.BotEngine
import com.ehan.app3.data.ChatDao
import com.ehan.app3.data.ChatMessage
import kotlinx.coroutines.delay

class ChatRepository(
    private val chatDao: ChatDao
) {

    fun getMessages() =
        chatDao.getAllMessages()

    suspend fun saveUserMessage(
        text: String
    ) {
        chatDao.insertMessage(
            ChatMessage(
                text = text,
                isBot = false,
                status = "PENDING"
            )
        )
    }

    suspend fun processPendingMessages(): Boolean {

        val pendingMessages =
            chatDao.getPendingMessages()

        for (message in pendingMessages) {

            val locked =
                chatDao.markAsProcessing(
                    message.id
                )

            if (locked == 0) {
                continue
            }

            try {

                delay(700)
                val botEngine = BotEngine()
                val reply =
                    botEngine.reply(message.text)

                chatDao.insertMessage(
                    ChatMessage(
                        text = reply,
                        isBot = true,
                        status = "PROCESSED"
                    )
                )

                chatDao.markAsProcessed(
                    message.id
                )

            } catch (exception: Exception) {

                chatDao.resetToPending(
                    message.id
                )

                return false
            }
        }

        return true
    }

    suspend fun clearMessages() {
        chatDao.deleteAllMessages()
    }
}
:::

Sekarang `ChatRepository` menjadi tempat yang bertanggung jawab terhadap operasi chat.

### 2. Update `ChatViewModel.kt`

:::writing{variant="document" id="17403" title="ChatViewModel.kt"}
```kotlin
package com.example.whatsappbot

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.whatsappbot.data.ChatDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel(
    chatDao: ChatDao,
    private val context: Context
) : ViewModel() {

    private val repository =
        ChatRepository(chatDao)

    val messages: StateFlow<List<com.example.whatsappbot.data.ChatMessage>> =
        repository.getMessages()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    private val _isTyping =
        MutableStateFlow(false)

    val isTyping: StateFlow<Boolean> =
        _isTyping

    private val _isOnline =
        MutableStateFlow(false)

    val isOnline: StateFlow<Boolean> =
        _isOnline

    private val _errorMessage =
        MutableStateFlow<String?>(null)

    val errorMessage: StateFlow<String?> =
        _errorMessage

    fun updateNetworkStatus(
        isOnline: Boolean
    ) {
        _isOnline.value = isOnline

        if (isOnline) {
            ChatWorkScheduler.schedule(context)
        }
    }

    fun sendMessage(text: String) {

        val cleanText = text.trim()

        if (cleanText.isEmpty()) {
            return
        }

        viewModelScope.launch {

            try {

                repository.saveUserMessage(
                    cleanText
                )

                if (!_isOnline.value) {
                    ChatWorkScheduler.schedule(context)
                    return@launch
                }

                _isTyping.value = true

                val success =
                    repository.processPendingMessages()

                if (!success) {
                    _errorMessage.value =
                        "Pesan belum berhasil diproses."
                }

            } catch (exception: Exception) {

                _errorMessage.value =
                    "Terjadi kesalahan saat memproses pesan."

            } finally {

                _isTyping.value = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearChat() {

        viewModelScope.launch {

            try {
                repository.clearMessages()

            } catch (exception: Exception) {

                _errorMessage.value =
                    "Gagal menghapus riwayat chat."
            }
        }
    }
}
:::

Sekarang ada `try/catch/finally`, jadi kalau terjadi error, indikator typing tidak akan nyangkut terus.

### 3. Update `ChatWorker.kt`

Worker juga menggunakan repository yang sama.

:::writing{variant="document" id="74163" title="ChatWorker.kt"}
```kotlin
package com.example.whatsappbot

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.whatsappbot.data.ChatDatabase

class ChatWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(
    appContext,
    workerParams
) {

    override suspend fun doWork(): Result {

        if (!isInternetAvailable()) {
            return Result.retry()
        }

        return try {

            val database =
                ChatDatabase.getDatabase(
                    applicationContext
                )

            val repository =
                ChatRepository(
                    database.chatDao()
                )

            val success =
                repository.processPendingMessages()

            if (success) {
                Result.success()
            } else {
                Result.retry()
            }

        } catch (exception: Exception) {

            Result.retry()
        }
    }

    private fun isInternetAvailable(): Boolean {

        val connectivityManager =
            applicationContext.getSystemService(
                Context.CONNECTIVITY_SERVICE
            ) as ConnectivityManager

        val network =
            connectivityManager.activeNetwork
                ?: return false

        val capabilities =
            connectivityManager.getNetworkCapabilities(
                network
            ) ?: return false

        return capabilities.hasCapability(
            NetworkCapabilities.NET_CAPABILITY_INTERNET
        )
    }
}
:::

Sekarang Worker jauh lebih sederhana. Dia tidak perlu tahu detail bagaimana pesan diproses.

### 4. Tambahkan error message ke `ChatScreen`

Kita perlu memberi tahu user kalau ada masalah. Jangan membuat aplikasi gagal diam-diam, karena manusia sudah cukup sering bingung tanpa bantuan software.

Di `ChatScreen`, tambahkan:

:::writing{variant="document" id="93517" title="ChatScreen.kt"}
```kotlin
package com.example.whatsappbot

import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

// Di dalam ChatScreen:

val errorMessage by viewModel.errorMessage.collectAsState()

LaunchedEffect(errorMessage) {
    if (errorMessage != null) {
        // Error sudah tersedia untuk UI.
        // Snackbar dapat ditambahkan pada langkah UI polish.
    }
}