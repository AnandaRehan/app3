package com.ehan.app3

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ehan.app3.bot.BotEngine
import com.ehan.app3.data.ChatDatabase

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