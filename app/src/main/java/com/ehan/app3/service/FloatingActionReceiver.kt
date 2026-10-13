package com.ehan.app3.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Receiver untuk menangani tombol aksi cepat dari notifikasi persisten
 * Floating Downloader (Deteksi & Download, atau Nonaktifkan).
 */
class FloatingActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        when (action) {
            FloatingDownloaderService.ACTION_DETECT_CLIPBOARD -> {
                val serviceIntent = Intent(context, FloatingDownloaderService::class.java).apply {
                    this.action = FloatingDownloaderService.ACTION_DETECT_CLIPBOARD
                }
                context.startService(serviceIntent)
            }
            FloatingDownloaderService.ACTION_STOP_SERVICE -> {
                val serviceIntent = Intent(context, FloatingDownloaderService::class.java).apply {
                    this.action = FloatingDownloaderService.ACTION_STOP_SERVICE
                }
                context.startService(serviceIntent)
            }
        }
    }
}
