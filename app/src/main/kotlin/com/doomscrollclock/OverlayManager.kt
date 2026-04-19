package com.doomscrollclock

import android.content.Context
import android.graphics.PixelFormat
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import com.doomscrollclock.databinding.OverlayTimerBinding

object OverlayManager {

    private const val HIDE_DELAY_MS = 1500L

    private lateinit var windowManager: WindowManager
    private lateinit var appContext: Context
    private var overlayView: View? = null
    private var binding: OverlayTimerBinding? = null

    // All calls to show/hide/scheduleHide originate from onAccessibilityEvent or
    // onUnbind, both of which run on the main thread — no handler.post needed.
    private val handler = Handler(Looper.getMainLooper())
    private var isShowing = false
    private var initialized = false

    private val hideRunnable = Runnable {
        TimerManager.stopTicking()
        removeOverlayView()
    }

    fun init(context: Context) {
        if (initialized) return
        appContext = context.applicationContext
        windowManager = appContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        binding = OverlayTimerBinding.inflate(LayoutInflater.from(appContext))
        overlayView = binding!!.root
        initialized = true
    }

    fun show() {
        if (!initialized || isShowing) return
        try {
            windowManager.addView(overlayView, buildLayoutParams())
            isShowing = true
            TimerManager.startTicking()
        } catch (e: Exception) {
            // Overlay permission revoked at runtime
        }
    }

    fun scheduleHide() {
        handler.removeCallbacks(hideRunnable)
        handler.postDelayed(hideRunnable, HIDE_DELAY_MS)
    }

    fun updateDisplay(@Suppress("UNUSED_PARAMETER") totalSeconds: Long) {
        binding?.timerText?.text = TimerManager.getFormattedTime()
    }

    fun cleanup() {
        handler.removeCallbacksAndMessages(null)
        TimerManager.stopTicking()
        if (isShowing) removeOverlayView()
        initialized = false
        binding = null
        overlayView = null
    }

    private fun removeOverlayView() {
        try {
            windowManager.removeView(overlayView)
        } catch (e: IllegalArgumentException) {
            // Already removed
        }
        isShowing = false
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
