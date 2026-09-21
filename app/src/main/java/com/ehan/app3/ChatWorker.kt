package com.ehan.app3

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ehan.app3.data.ChatDatabase

class ChatWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(
    appContext,
    workerParams
) {

    override suspend fun doWork(): Result {

        val database =
            ChatDatabase.getDatabase(applicationContext)

        val chatDao =
            database.chatDao()

        // Untuk tahap ini kita baru menyiapkan
        // background processing.
        //
        // Pemrosesan pesan pending akan kita tambahkan
        // pada langkah berikutnya.

        return Result.success()
    }
}