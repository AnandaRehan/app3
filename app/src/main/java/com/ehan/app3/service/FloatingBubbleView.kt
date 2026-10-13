package com.ehan.app3.service

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView

/**
 * Tampilan gelembung melayang (Floating Bubble) beserta menu mini kendali
 * dan banner status pill.
 */
class FloatingBubbleView(
    context: Context,
    private val onBubbleClick: () -> Unit,
    private val onBubbleLongClick: () -> Unit,
    private val onDetectMenuClick: () -> Unit,
    private val onCloseMenuClick: () -> Unit
) : FrameLayout(context) {

    private val density = resources.displayMetrics.density
    private val mainHandler = Handler(Looper.getMainLooper())

    private val rootLayout: LinearLayout
    private val bubbleContainer: FrameLayout
    private val bubbleIcon: ImageView
    private val loadingSpinner: ProgressBar
    private val menuContainer: LinearLayout
    private val statusPill: TextView

    private var pulseAnimator: ObjectAnimator? = null
    var isMenuExpanded: Boolean = false
        private set

    init {
        rootLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            clipChildren = false
            clipToPadding = false
        }

        // 1. Status Pill (Pesan mengambang di atas gelembung)
        statusPill = TextView(context).apply {
            visibility = View.GONE
            textSize = 11f
            setTextColor(Color.WHITE)
            setTypeface(null, Typeface.BOLD)
            maxLines = 2
            ellipsize = android.text.TextUtils.TruncateAt.END
            val padH = (12 * density).toInt()
            val padV = (6 * density).toInt()
            setPadding(padH, padV, padH, padV)
            val pillBg = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 16 * density
                setColor(Color.parseColor("#E60F172A")) // Dark Slate 90%
                setStroke((1 * density).toInt(), Color.parseColor("#40FFFFFF"))
            }
            background = pillBg
            val pillParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = (6 * density).toInt()
                gravity = Gravity.CENTER_HORIZONTAL
            }
            layoutParams = pillParams
        }
        rootLayout.addView(statusPill)

        // Baris Horizontal untuk Gelembung + Menu Mini
        val horizontalRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            clipChildren = false
            clipToPadding = false
        }

        // 2. Gelembung Utama (Bubble 56dp)
        bubbleContainer = FrameLayout(context).apply {
            val size = (56 * density).toInt()
            val bParams = LinearLayout.LayoutParams(size, size).apply {
                marginEnd = (8 * density).toInt()
            }
            layoutParams = bParams
            elevation = 12 * density
            clipToOutline = true
        }

        val bubbleBg = GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            intArrayOf(Color.parseColor("#6366F1"), Color.parseColor("#4F46E5"), Color.parseColor("#4338CA"))
        ).apply {
            shape = GradientDrawable.OVAL
            setStroke((2 * density).toInt(), Color.WHITE)
        }
        bubbleContainer.background = bubbleBg

        // Ikon Unduh di dalam gelembung
        bubbleIcon = ImageView(context).apply {
            setImageResource(android.R.drawable.stat_sys_download)
            setColorFilter(Color.WHITE)
            val iconSize = (28 * density).toInt()
            layoutParams = LayoutParams(iconSize, iconSize).apply {
                gravity = Gravity.CENTER
            }
        }
        bubbleContainer.addView(bubbleIcon)

        // Loading Spinner
        loadingSpinner = ProgressBar(context).apply {
            visibility = View.GONE
            isIndeterminate = true
            val spinSize = (32 * density).toInt()
            layoutParams = LayoutParams(spinSize, spinSize).apply {
                gravity = Gravity.CENTER
            }
        }
        bubbleContainer.addView(loadingSpinner)

        horizontalRow.addView(bubbleContainer)

        // 3. Menu Mini (Terbuka saat Long Press)
        menuContainer = LinearLayout(context).apply {
            visibility = View.GONE
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            val pad = (6 * density).toInt()
            setPadding(pad, pad, pad, pad)
            val menuBg = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 24 * density
                setColor(Color.parseColor("#F20F172A")) // Solid Dark Slate
                setStroke((1.5f * density).toInt(), Color.parseColor("#40FFFFFF"))
            }
            background = menuBg
            elevation = 14 * density
        }

        // Tombol Deteksi Link (Hijau)
        val btnDetect = createMenuButton(
            text = "📥 Unduh Link",
            textColor = Color.WHITE,
            bgColor = Color.parseColor("#10B981") // Emerald Green
        ) {
            collapseMenu()
            onDetectMenuClick()
        }
        menuContainer.addView(btnDetect)

        // Tombol Matikan Pop-up (Merah)
        val btnClose = createMenuButton(
            text = "❌ Matikan",
            textColor = Color.WHITE,
            bgColor = Color.parseColor("#EF4444") // Red
        ) {
            collapseMenu()
            onCloseMenuClick()
        }
        menuContainer.addView(btnClose)

        // Tombol Sembunyikan Menu
        val btnCollapse = createMenuButton(
            text = "✖️",
            textColor = Color.parseColor("#E2E8F0"),
            bgColor = Color.parseColor("#334155")
        ) {
            collapseMenu()
        }
        menuContainer.addView(btnCollapse)

        horizontalRow.addView(menuContainer)
        rootLayout.addView(horizontalRow)

        addView(rootLayout)
    }

    private fun createMenuButton(
        text: String,
        textColor: Int,
        bgColor: Int,
        onClick: () -> Unit
    ): TextView {
        return TextView(context).apply {
            this.text = text
            this.textSize = 12f
            this.setTextColor(textColor)
            this.setTypeface(null, Typeface.BOLD)
            val padH = (10 * density).toInt()
            val padV = (6 * density).toInt()
            this.setPadding(padH, padV, padH, padV)
            val bg = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 16 * density
                setColor(bgColor)
            }
            this.background = bg
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                marginEnd = (6 * density).toInt()
            }
            this.layoutParams = params
            this.setOnClickListener { onClick() }
        }
    }

    fun setBubbleColors(primaryColorHex: String, secondaryColorHex: String) {
        try {
            val pColor = Color.parseColor(primaryColorHex)
            val sColor = Color.parseColor(secondaryColorHex)
            val bubbleBg = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(pColor, sColor)
            ).apply {
                shape = GradientDrawable.OVAL
                setStroke((2 * density).toInt(), Color.WHITE)
            }
            bubbleContainer.background = bubbleBg
        } catch (_: Exception) {
        }
    }

    fun setLoading(isLoading: Boolean) {
        if (isLoading) {
            bubbleIcon.visibility = View.INVISIBLE
            loadingSpinner.visibility = View.VISIBLE
            startPulseAnimation()
        } else {
            bubbleIcon.visibility = View.VISIBLE
            loadingSpinner.visibility = View.GONE
            stopPulseAnimation()
        }
    }

    private fun startPulseAnimation() {
        stopPulseAnimation()
        pulseAnimator = ObjectAnimator.ofFloat(bubbleContainer, "scaleX", 1f, 1.15f, 1f).apply {
            duration = 800
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            start()
        }
    }

    private fun stopPulseAnimation() {
        pulseAnimator?.cancel()
        pulseAnimator = null
        bubbleContainer.scaleX = 1f
        bubbleContainer.scaleY = 1f
    }

    fun toggleMenu(): Boolean {
        return if (isMenuExpanded) {
            collapseMenu()
            false
        } else {
            expandMenu()
            true
        }
    }

    fun expandMenu() {
        isMenuExpanded = true
        menuContainer.visibility = View.VISIBLE
        menuContainer.alpha = 0f
        menuContainer.animate().alpha(1f).setDuration(200).start()
    }

    fun collapseMenu() {
        isMenuExpanded = false
        menuContainer.animate().alpha(0f).setDuration(150).withEndAction {
            menuContainer.visibility = View.GONE
        }.start()
    }

    private var hidePillRunnable: Runnable? = null

    fun showStatusMessage(message: String, isError: Boolean = false, durationMs: Long = 3000L) {
        mainHandler.post {
            hidePillRunnable?.let { mainHandler.removeCallbacks(it) }

            statusPill.text = message
            val pillBg = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 16 * density
                setColor(if (isError) Color.parseColor("#CCBE123C") else Color.parseColor("#E60F172A"))
                setStroke((1 * density).toInt(), Color.parseColor("#80FFFFFF"))
            }
            statusPill.background = pillBg
            statusPill.visibility = View.VISIBLE
            statusPill.alpha = 0f
            statusPill.animate().alpha(1f).setDuration(150).start()

            val runnable = Runnable {
                statusPill.animate().alpha(0f).setDuration(200).withEndAction {
                    statusPill.visibility = View.GONE
                }.start()
            }
            hidePillRunnable = runnable
            mainHandler.postDelayed(runnable, durationMs)
        }
    }

    fun getBubbleView(): View = bubbleContainer
}
