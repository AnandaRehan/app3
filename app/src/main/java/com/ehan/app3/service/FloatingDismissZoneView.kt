package com.ehan.app3.service

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

/**
 * Tampilan zona target tutup (Drop Zone) di bagian bawah layar.
 * Muncul saat gelembung mulai diseret dan menyala merah saat gelembung
 * didekatkan ke zona ini.
 */
class FloatingDismissZoneView(context: Context) : FrameLayout(context) {

    private val container: LinearLayout
    private val iconView: ImageView
    private val textView: TextView
    private val circleTarget: FrameLayout

    var isTargetHovered: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                updateVisualState()
            }
        }

    init {
        visibility = View.GONE

        container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
        }

        circleTarget = FrameLayout(context).apply {
            val size = (68 * resources.displayMetrics.density).toInt()
            layoutParams = LayoutParams(size, size).apply {
                gravity = Gravity.CENTER
            }
        }

        iconView = ImageView(context).apply {
            setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            setColorFilter(Color.WHITE)
            val iconSize = (32 * resources.displayMetrics.density).toInt()
            layoutParams = LayoutParams(iconSize, iconSize).apply {
                gravity = Gravity.CENTER
            }
        }
        circleTarget.addView(iconView)

        textView = TextView(context).apply {
            text = "Lepas di sini untuk menutup pop-up"
            textSize = 12f
            setTextColor(Color.WHITE)
            setPadding(0, (6 * resources.displayMetrics.density).toInt(), 0, (12 * resources.displayMetrics.density).toInt())
            gravity = Gravity.CENTER
        }

        container.addView(circleTarget)
        container.addView(textView)

        val rootParams = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            bottomMargin = (32 * resources.displayMetrics.density).toInt()
        }
        addView(container, rootParams)

        updateVisualState()
    }

    private fun updateVisualState() {
        val density = resources.displayMetrics.density
        val bgDrawable = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            if (isTargetHovered) {
                setColor(Color.parseColor("#E11D48")) // Rose red
                setStroke((3 * density).toInt(), Color.WHITE)
            } else {
                setColor(Color.parseColor("#990F172A")) // Semi-transparent dark slate
                setStroke((2 * density).toInt(), Color.parseColor("#80FFFFFF"))
            }
        }
        circleTarget.background = bgDrawable

        if (isTargetHovered) {
            circleTarget.scaleX = 1.15f
            circleTarget.scaleY = 1.15f
            textView.setTextColor(Color.parseColor("#FDA4AF"))
            textView.text = "Lepas sekarang untuk mematikan"
        } else {
            circleTarget.scaleX = 1.0f
            circleTarget.scaleY = 1.0f
            textView.setTextColor(Color.parseColor("#E2E8F0"))
            textView.text = "Geser ke sini untuk mematikan pop-up"
        }
    }

    /**
     * Memeriksa apakah koordinat layar (screenX, screenY) berada dalam jangkauan
     * target tutup ini.
     */
    fun isPointInsideTarget(screenX: Int, screenY: Int): Boolean {
        val location = IntArray(2)
        circleTarget.getLocationOnScreen(location)
        val targetLeft = location[0]
        val targetTop = location[1]
        val targetRight = targetLeft + circleTarget.width
        val targetBottom = targetTop + circleTarget.height

        // Berikan margin toleransi 30dp agar mudah terkena
        val tolerance = (30 * resources.displayMetrics.density).toInt()
        return screenX in (targetLeft - tolerance)..(targetRight + tolerance) &&
                screenY in (targetTop - tolerance)..(targetBottom + tolerance)
    }
}
