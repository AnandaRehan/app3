package com.ehan.app3

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ehan.app3.bot.BotEngine
import com.ehan.app3.data.ChatDatabase
import com.ehan.app3.data.ChatMessage
import kotlinx.coroutines.delay

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

        val database =
            ChatDatabase.getDatabase(
                applicationContext
            )

        val chatDao =
            database.chatDao()

        val pendingMessages =
            chatDao.getPendingMessages()

        for (message in pendingMessages) {

            if (!isInternetAvailable()) {
                chatDao.resetToPending(message.id)
                return Result.retry()
            }

            val locked =
                chatDao.markAsProcessing(
                    message.id
                )

            // Pesan sudah diproses oleh worker lain
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

                return Result.retry()
            }
        }

        return Result.success()
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