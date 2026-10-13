package com.ehan.app3.service

import android.app.Activity
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager

/**
 * Trampoline Activity transparan (tak terlihat) untuk membaca papan klip (Clipboard)
 * pada Android 10+ (API 29 ke atas) di mana akses papan klip dibatasi hanya untuk
 * aplikasi yang memiliki fokus jendela (Window Focus).
 *
 * Activity ini diluncurkan sekilas (tanpa animasi transisi), membaca clipboard segera
 * setelah jendela mendapatkan fokus, meneruskan teks ke FloatingDownloaderService,
 * dan langsung memanggil finish() tanpa mengganggu layar atau aplikasi yang sedang dibuka pengguna.
 */
class ClipboardCaptureActivity : Activity() {

    private val mainHandler = Handler(Looper.getMainLooper())
    private var hasCaptured = false

    private val timeoutRunnable = Runnable {
        if (!hasCaptured && !isFinishing) {
            readAndDispatchClipboard()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Pastikan tidak ada animasi jendela dan jendela tidak menyala/dim
        window?.apply {
            setLayout(1, 1)
            addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
            clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        }

        // Batas waktu pengaman 1200ms jika fokus jendela tidak terpicu
        mainHandler.postDelayed(timeoutRunnable, 1200)
    }

    override fun onResume() {
        super.onResume()
        // Coba baca di onResume jika jendela sudah siap
        if (hasWindowFocus() && !hasCaptured) {
            readAndDispatchClipboard()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && !hasCaptured) {
            readAndDispatchClipboard()
        }
    }

    private fun readAndDispatchClipboard() {
        if (hasCaptured) return
        hasCaptured = true
        mainHandler.removeCallbacks(timeoutRunnable)

        val clipboardText = try {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            val clipData = clipboard?.primaryClip
            if (clipData != null && clipData.itemCount > 0) {
                clipData.getItemAt(0)?.coerceToText(this)?.toString()?.trim()
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }

        // Teruskan hasil ke FloatingDownloaderService
        FloatingDownloaderService.onClipboardCaptured(this, clipboardText)

        finishWithoutAnimation()
    }

    private fun finishWithoutAnimation() {
        finish()
        if (Build.VERSION.SDK_INT >= 34) {
            overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, 0, 0)
        } else {
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }
    }

    override fun onDestroy() {
        mainHandler.removeCallbacks(timeoutRunnable)
        super.onDestroy()
    }

    companion object {
        fun capture(context: Context) {
            val intent = Intent(context, ClipboardCaptureActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            context.startActivity(intent)
        }
    }
}
