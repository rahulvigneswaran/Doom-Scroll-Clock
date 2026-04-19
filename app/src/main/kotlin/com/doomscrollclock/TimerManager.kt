package com.doomscrollclock

import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import java.time.LocalDate

object TimerManager {

    private const val PREFS_NAME = "doom_scroll_prefs"
    private const val KEY_TOTAL_SECONDS = "total_seconds_today"
    private const val KEY_RESET_DATE = "last_reset_date"

    private lateinit var prefs: SharedPreferences
    private val handler = Handler(Looper.getMainLooper())
    private var isRunning = false
    private var totalSeconds: Long = 0

    private val tickRunnable = object : Runnable {
        override fun run() {
            totalSeconds++
            OverlayManager.updateDisplay(totalSeconds)
            prefs.edit().putLong(KEY_TOTAL_SECONDS, totalSeconds).apply()
            handler.postDelayed(this, 1000)
        }
    }

    fun init(context: Context) {
        if (::prefs.isInitialized) return
        prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        checkAndResetIfNeeded()
        totalSeconds = prefs.getLong(KEY_TOTAL_SECONDS, 0)
    }

    fun checkAndResetIfNeeded() {
        val today = LocalDate.now().toString()
        if (prefs.getString(KEY_RESET_DATE, "") != today) {
            reset()
            prefs.edit().putString(KEY_RESET_DATE, today).apply()
        }
    }

    fun startTicking() {
        if (isRunning) return
        isRunning = true
        handler.post(tickRunnable)
    }

    fun stopTicking() {
        if (!isRunning) return
        isRunning = false
        handler.removeCallbacks(tickRunnable)
        prefs.edit().putLong(KEY_TOTAL_SECONDS, totalSeconds).apply()
    }

    fun reset() {
        stopTicking()
        totalSeconds = 0
        prefs.edit()
            .putLong(KEY_TOTAL_SECONDS, 0)
            .putString(KEY_RESET_DATE, LocalDate.now().toString())
            .apply()
    }

    fun getFormattedTime(): String {
        val s = totalSeconds
        return when {
            s < 60 -> "${s}s"
            s < 3600 -> "${s / 60}m ${s % 60}s"
            else -> "${s / 3600}h ${(s % 3600) / 60}m"
        }
    }

    fun getTotalSeconds(): Long = totalSeconds
}
