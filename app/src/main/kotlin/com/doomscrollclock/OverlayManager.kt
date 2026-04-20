package com.doomscrollclock

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.TextView

object OverlayManager {

    private const val HIDE_DELAY_MS = 1500L
    private const val PREFS_NAME = "doom_scroll_prefs"
    private const val KEY_DISPLAY_MODE = "pill_display_mode"

    private lateinit var windowManager: WindowManager
    private lateinit var appContext: Context
    private var timerTextView: TextView? = null
    private var overlayView: GlowPillView? = null
    private val handler = Handler(Looper.getMainLooper())
    private var isShowing = false
    private var initialized = false

    private val hideRunnable = Runnable {
        TimerManager.stopTicking()
        removeOverlayView()
        NotificationHelper.showSummary()
    }

    private class GlowPillView(ctx: Context) : FrameLayout(ctx) {

        private val density = ctx.resources.displayMetrics.density
        private val cornerRadius = 24 * density
        private val glowPad = (32 * density).toInt()

        private var glowAlpha = 0.35f

        private val outerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#8B5CF6")
            style = Paint.Style.FILL
            maskFilter = BlurMaskFilter(28 * density, BlurMaskFilter.Blur.NORMAL)
        }
        private val innerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
            maskFilter = BlurMaskFilter(10 * density, BlurMaskFilter.Blur.NORMAL)
        }

        private val animator = ValueAnimator.ofFloat(0.35f, 1.0f).apply {
            duration = 1500
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener { glowAlpha = it.animatedValue as Float; invalidate() }
        }

        init {
            setLayerType(LAYER_TYPE_SOFTWARE, null)
            setWillNotDraw(false)
            setPadding(glowPad, glowPad, glowPad, glowPad)
        }

        fun startPulsing() {
            if (!animator.isRunning) animator.start()
        }

        fun stopPulsing() {
            animator.cancel()
            glowAlpha = 0.35f
            invalidate()
        }

        override fun onDraw(canvas: Canvas) {
            val child = getChildAt(0) ?: return super.onDraw(canvas)
            val l = child.left.toFloat()
            val t = child.top.toFloat()
            val r = child.right.toFloat()
            val b = child.bottom.toFloat()
            val expand = 2 * density
            val outerRect = RectF(l - expand, t - expand, r + expand, b + expand)
            val pillRect = RectF(l, t, r, b)

            outerPaint.alpha = (glowAlpha * 0.65f * 255).toInt()
            canvas.drawRoundRect(outerRect, cornerRadius, cornerRadius, outerPaint)

            innerPaint.alpha = (glowAlpha * 0.55f * 255).toInt()
            canvas.drawRoundRect(pillRect, cornerRadius, cornerRadius, innerPaint)

            super.onDraw(canvas)
        }
    }

    fun init(context: Context) {
        if (initialized) return
        appContext = context.applicationContext
        windowManager = appContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        overlayView = buildOverlayView()
        initialized = true
    }

    fun show() {
        if (!initialized || isShowing) return
        try {
            windowManager.addView(overlayView, buildLayoutParams())
            isShowing = true
            TimerManager.startTicking()
            overlayView?.startPulsing()
        } catch (e: Exception) {
            // SYSTEM_ALERT_WINDOW not granted or revoked at runtime
        }
    }

    fun scheduleHide() {
        handler.removeCallbacks(hideRunnable)
        handler.postDelayed(hideRunnable, HIDE_DELAY_MS)
    }

    fun updateDisplay(@Suppress("UNUSED_PARAMETER") totalSeconds: Long) {
        val mode = if (::appContext.isInitialized) {
            appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_DISPLAY_MODE, "time") ?: "time"
        } else "time"

        timerTextView?.text = if (mode == "distance") {
            val metres = TimerManager.getScrollEvents() * 0.02
            if (metres < 1000) "~${metres.toInt()}m" else "~${"%.1f".format(metres / 1000)}km"
        } else {
            TimerManager.getFormattedTime()
        }
    }

    fun cleanup() {
        handler.removeCallbacksAndMessages(null)
        TimerManager.stopTicking()
        if (isShowing) removeOverlayView()
        overlayView?.stopPulsing()
        initialized = false
        timerTextView = null
        overlayView = null
    }

    private fun removeOverlayView() {
        overlayView?.stopPulsing()
        try {
            windowManager.removeView(overlayView)
        } catch (e: IllegalArgumentException) {
            // Already removed
        }
        isShowing = false
    }

    private fun buildOverlayView(): GlowPillView {
        val density = appContext.resources.displayMetrics.density
        val hPad = (14 * density).toInt()
        val vPad = (5 * density).toInt()
        val cornerRadius = 24 * density

        val background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            this.cornerRadius = cornerRadius
            setColor(0xE6000000.toInt())
        }

        val tv = TextView(appContext).apply {
            setPadding(hPad, vPad, hPad, vPad)
            setTextColor(0xFFFFFFFF.toInt())
            textSize = 13f
            typeface = Typeface.MONOSPACE
            text = "0s"
            setBackground(background)
        }

        return GlowPillView(appContext).also { wrapper ->
            wrapper.addView(tv)
            timerTextView = tv
        }
    }

    private fun buildLayoutParams(): WindowManager.LayoutParams {
        val density = appContext.resources.displayMetrics.density
        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = (56 * density).toInt()
        }
    }
}
