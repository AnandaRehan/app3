package com.ehan.app3.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.ehan.app3.MainActivity
import com.ehan.app3.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

/**
 * Layanan latar depan (Foreground Service) yang menampilkan Floating Bubble Downloader
 * di atas aplikasi lain (Overlay Window) serta notifikasi persisten cepat.
 */
class FloatingDownloaderService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val mainHandler = Handler(Looper.getMainLooper())

    private lateinit var windowManager: WindowManager
    private lateinit var floatingBubbleView: FloatingBubbleView
    private lateinit var dismissZoneView: FloatingDismissZoneView
    private lateinit var downloadProcessor: FloatingDownloadProcessor

    private lateinit var bubbleParams: WindowManager.LayoutParams
    private lateinit var dismissParams: WindowManager.LayoutParams

    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var isDragging = false
    private var isLongPressTriggered = false
    private var lastHoverState = false

    private val longPressRunnable = Runnable {
        isLongPressTriggered = true
        vibrate(35)
        floatingBubbleView.toggleMenu()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        instance = this
        _isServiceRunning.value = true

        if (!canDrawOverlays(this)) {
            stopSelf()
            return
        }

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        downloadProcessor = FloatingDownloadProcessor(this)

        startForegroundServiceNotification()
        setupOverlayViews()
        registerClipboardListener()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        when (action) {
            ACTION_DETECT_CLIPBOARD -> {
                detectAndDownloadFromClipboard()
            }
            ACTION_PROCESS_CAPTURED_TEXT -> {
                val text = intent.getStringExtra(EXTRA_CAPTURED_TEXT)
                processCapturedClipboardText(text)
            }
            ACTION_STOP_SERVICE -> {
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun startForegroundServiceNotification() {
        createNotificationChannel()

        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action 1: Deteksi & Download Langsung
        val detectIntent = Intent(this, FloatingActionReceiver::class.java).apply {
            action = ACTION_DETECT_CLIPBOARD
        }
        val detectPendingIntent = PendingIntent.getBroadcast(
            this,
            1,
            detectIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action 2: Matikan Pop-up
        val stopIntent = Intent(this, FloatingActionReceiver::class.java).apply {
            action = ACTION_STOP_SERVICE
        }
        val stopPendingIntent = PendingIntent.getBroadcast(
            this,
            2,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("App3 Downloader Melayang Aktif")
            .setContentText("Ketuk bubble melayang untuk download otomatis link dari clipboard")
            .setSmallIcon(R.drawable.ic_popup_notification)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(openAppPendingIntent)
            .addAction(R.drawable.ic_detect_link, "🔗 Deteksi & Unduh", detectPendingIntent)
            .addAction(R.drawable.ic_close_notification, "❌ Nonaktifkan", stopPendingIntent)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Downloader Melayang",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Layanan pop-up melayang untuk unduh cepat media dari clipboard"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupOverlayViews() {
        val overlayType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        // 1. Inisialisasi Dismiss Drop Zone
        dismissZoneView = FloatingDismissZoneView(this)
        dismissParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        }

        try {
            windowManager.addView(dismissZoneView, dismissParams)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 2. Inisialisasi Floating Bubble View
        floatingBubbleView = FloatingBubbleView(
            context = this,
            onBubbleClick = {
                // Di-handle di onTouch
            },
            onBubbleLongClick = {
                // Di-handle di onTouch
            },
            onDetectMenuClick = {
                detectAndDownloadFromClipboard()
            },
            onCloseMenuClick = {
                Toast.makeText(this, "Pop-up downloader dinonaktifkan", Toast.LENGTH_SHORT).show()
                stopSelf()
            }
        )

        bubbleParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 24
            y = 350
        }

        // Pasang touch listener pada gelembung
        floatingBubbleView.getBubbleView().setOnTouchListener { _, event ->
            handleBubbleTouch(event)
        }

        try {
            windowManager.addView(floatingBubbleView, bubbleParams)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun handleBubbleTouch(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                initialX = bubbleParams.x
                initialY = bubbleParams.y
                initialTouchX = event.rawX
                initialTouchY = event.rawY
                isDragging = false
                isLongPressTriggered = false
                lastHoverState = false

                // Pasang timeout long press (500ms)
                mainHandler.postDelayed(longPressRunnable, 500)
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                val dx = abs(event.rawX - initialTouchX)
                val dy = abs(event.rawY - initialTouchY)

                if (!isDragging && (dx > 12 || dy > 12)) {
                    isDragging = true
                    mainHandler.removeCallbacks(longPressRunnable)
                    dismissZoneView.visibility = View.VISIBLE
                }

                if (isDragging) {
                    bubbleParams.x = initialX + (event.rawX - initialTouchX).toInt()
                    bubbleParams.y = initialY + (event.rawY - initialTouchY).toInt()

                    try {
                        windowManager.updateViewLayout(floatingBubbleView, bubbleParams)
                    } catch (_: Exception) {
                    }

                    // Cek collision dengan drop target di bawah layar
                    val bubbleCenterX = bubbleParams.x + floatingBubbleView.width / 2
                    val bubbleCenterY = bubbleParams.y + floatingBubbleView.height / 2
                    val isInsideTarget = dismissZoneView.isPointInsideTarget(bubbleCenterX, bubbleCenterY)

                    dismissZoneView.isTargetHovered = isInsideTarget

                    if (isInsideTarget && !lastHoverState) {
                        vibrate(25)
                    }
                    lastHoverState = isInsideTarget
                }
                return true
            }

            MotionEvent.ACTION_UP -> {
                mainHandler.removeCallbacks(longPressRunnable)
                dismissZoneView.visibility = View.GONE

                if (dismissZoneView.isTargetHovered) {
                    vibrate(40)
                    Toast.makeText(this, "Pop-up downloader dinonaktifkan", Toast.LENGTH_SHORT).show()
                    stopSelf()
                    return true
                }

                if (!isDragging && !isLongPressTriggered) {
                    // Single Click biasa -> Langsung deteksi & download dari papan klip
                    if (floatingBubbleView.isMenuExpanded) {
                        floatingBubbleView.collapseMenu()
                    } else {
                        detectAndDownloadFromClipboard()
                    }
                } else if (isDragging) {
                    // Snap ke tepi layar (Kiri / Kanan) untuk kenyamanan pengguna
                    snapBubbleToScreenEdge()
                }

                dismissZoneView.isTargetHovered = false
                return true
            }

            MotionEvent.ACTION_CANCEL -> {
                mainHandler.removeCallbacks(longPressRunnable)
                dismissZoneView.visibility = View.GONE
                dismissZoneView.isTargetHovered = false
                return true
            }
        }
        return false
    }

    private fun snapBubbleToScreenEdge() {
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val middle = screenWidth / 2

        val targetX = if (bubbleParams.x + floatingBubbleView.width / 2 < middle) {
            16
        } else {
            screenWidth - floatingBubbleView.width - 16
        }

        bubbleParams.x = targetX
        try {
            windowManager.updateViewLayout(floatingBubbleView, bubbleParams)
        } catch (_: Exception) {
        }
    }

    private var clipboardListener: ClipboardManager.OnPrimaryClipChangedListener? = null
    private var lastKnownClipboardText: String? = null

    private fun registerClipboardListener() {
        try {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            clipboardListener = ClipboardManager.OnPrimaryClipChangedListener {
                // Saat ada perubahan clip, simpan jika bisa diakses
                val text = getClipboardText()
                if (!text.isNullOrBlank()) {
                    lastKnownClipboardText = text
                }
            }
            clipboard?.addPrimaryClipChangedListener(clipboardListener)
        } catch (_: Exception) {
        }
    }

    private fun unregisterClipboardListener() {
        try {
            clipboardListener?.let {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                clipboard?.removePrimaryClipChangedListener(it)
            }
        } catch (_: Exception) {
        }
    }

    /**
     * Membaca teks terakhir dari papan klip.
     * Pada Android 10+ (API 29+), jika pembacaan latar belakang dibatasi,
     * sistem akan meluncurkan ClipboardCaptureActivity transparan sekejap
     * untuk memperoleh window focus dan membaca clipboard secara legal.
     */
    fun detectAndDownloadFromClipboard() {
        floatingBubbleView.setLoading(true)
        floatingBubbleView.showStatusMessage("Mendeteksi papan klip...", isError = false, durationMs = 2500)

        // Cek apakah ada teks langsung yang bisa dibaca (misal di Android 9 atau jika app aktif)
        val directText = getClipboardText()
        if (!directText.isNullOrBlank()) {
            lastKnownClipboardText = directText
            processCapturedClipboardText(directText)
            return
        }

        // Jika directText kosong tetapi ada lastKnownClipboardText yang baru saja disalin
        if (!lastKnownClipboardText.isNullOrBlank()) {
            val cached = lastKnownClipboardText
            lastKnownClipboardText = null
            processCapturedClipboardText(cached)
            return
        }

        // Luncurkan ClipboardCaptureActivity (Translucent Trampoline) tanpa animasi
        try {
            ClipboardCaptureActivity.capture(this)
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback jika peluncuran activity gagal
            processCapturedClipboardText(null)
        }
    }

    /**
     * Memproses teks papan klip yang telah berhasil ditangkap (baik dari direct read maupun ClipboardCaptureActivity).
     */
    fun processCapturedClipboardText(clipboardText: String?) {
        serviceScope.launch {
            floatingBubbleView.setLoading(true)

            if (clipboardText.isNullOrBlank()) {
                floatingBubbleView.setLoading(false)
                floatingBubbleView.showStatusMessage("⚠️ Papan klip kosong!", isError = true, durationMs = 3000)
                Toast.makeText(
                    this@FloatingDownloaderService,
                    "Papan klip kosong. Silakan salin tautan TikTok atau YouTube terlebih dahulu!",
                    Toast.LENGTH_SHORT
                ).show()
                return@launch
            }

            floatingBubbleView.showStatusMessage("🔍 Menganalisis tautan...", isError = false, durationMs = 2000)

            // Proses tautan lewat DownloadProcessor (TikTok SD No-Watermark atau YouTube)
            val result = downloadProcessor.processClipboardText(clipboardText)
            
            floatingBubbleView.setLoading(false)
            when (result) {
                is DownloadDetectionResult.Success -> {
                    vibrate(35)
                    val successMsg = "✅ Mengunduh ${result.platform}: ${result.fileName}"
                    floatingBubbleView.showStatusMessage(successMsg, isError = false, durationMs = 4500)
                    Toast.makeText(this@FloatingDownloaderService, successMsg, Toast.LENGTH_LONG).show()
                }
                is DownloadDetectionResult.Error -> {
                    vibrate(50)
                    floatingBubbleView.showStatusMessage("❌ ${result.message}", isError = true, durationMs = 4000)
                    Toast.makeText(this@FloatingDownloaderService, result.message, Toast.LENGTH_LONG).show()
                }
                is DownloadDetectionResult.InvalidLink -> {
                    vibrate(40)
                    val preview = if (clipboardText.length > 28) clipboardText.take(28) + "..." else clipboardText
                    val msg = "⚠️ Teks papan klip bukan link TikTok/YouTube valid: \"$preview\""
                    floatingBubbleView.showStatusMessage("⚠️ Bukan link valid!", isError = true, durationMs = 3500)
                    Toast.makeText(this@FloatingDownloaderService, msg, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun getClipboardText(): String? {
        return try {
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
    }

    private fun vibrate(durationMs: Long) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                vibrator?.vibrate(durationMs)
            }
        } catch (_: Exception) {
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (instance == this) {
            instance = null
        }
        _isServiceRunning.value = false
        unregisterClipboardListener()
        serviceScope.cancel()

        try {
            if (::floatingBubbleView.isInitialized) {
                windowManager.removeView(floatingBubbleView)
            }
            if (::dismissZoneView.isInitialized) {
                windowManager.removeView(dismissZoneView)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        const val CHANNEL_ID = "channel_floating_downloader"
        const val NOTIFICATION_ID = 2001

        const val ACTION_DETECT_CLIPBOARD = "com.ehan.app3.action.DETECT_CLIPBOARD"
        const val ACTION_PROCESS_CAPTURED_TEXT = "com.ehan.app3.action.PROCESS_CAPTURED_TEXT"
        const val ACTION_STOP_SERVICE = "com.ehan.app3.action.STOP_FLOATING_SERVICE"
        const val EXTRA_CAPTURED_TEXT = "extra_captured_text"

        @Volatile
        private var instance: FloatingDownloaderService? = null

        private val _isServiceRunning = MutableStateFlow(false)
        val isServiceRunning = _isServiceRunning.asStateFlow()

        /**
         * Dipanggil oleh ClipboardCaptureActivity saat berhasil membaca isi papan klip.
         */
        fun onClipboardCaptured(context: Context, text: String?) {
            val current = instance
            if (current != null) {
                current.processCapturedClipboardText(text)
            } else {
                val intent = Intent(context, FloatingDownloaderService::class.java).apply {
                    action = ACTION_PROCESS_CAPTURED_TEXT
                    putExtra(EXTRA_CAPTURED_TEXT, text)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            }
        }

        fun canDrawOverlays(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Settings.canDrawOverlays(context)
            } else {
                true
            }
        }

        fun start(context: Context) {
            val intent = Intent(context, FloatingDownloaderService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, FloatingDownloaderService::class.java)
            context.stopService(intent)
        }
    }
}
