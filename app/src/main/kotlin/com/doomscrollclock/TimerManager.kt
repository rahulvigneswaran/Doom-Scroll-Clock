package com.doomscrollclock

import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import org.json.JSONObject
import java.time.LocalDate

object TimerManager {

    private const val PREFS_NAME = "doom_scroll_prefs"
    private const val KEY_TOTAL_SECONDS = "total_seconds_today"
    private const val KEY_RESET_DATE = "last_reset_date"
    private const val KEY_SCROLL_EVENTS = "scroll_events_today"
    private const val KEY_LIFETIME_SECONDS = "lifetime_seconds"
    private const val KEY_DAY_HISTORY = "day_history"

    private lateinit var prefs: SharedPreferences
    private val handler = Handler(Looper.getMainLooper())
    private var isRunning = false
    private var totalSeconds: Long = 0
    private var scrollEventsToday: Long = 0
    private var lifetimeSeconds: Long = 0

    data class DayStats(val date: String, val seconds: Long, val scrollEvents: Long)

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
        totalSeconds = prefs.getLong(KEY_TOTAL_SECONDS, 0)
        scrollEventsToday = prefs.getLong(KEY_SCROLL_EVENTS, 0)
        lifetimeSeconds = prefs.getLong(KEY_LIFETIME_SECONDS, 0)
        checkAndResetIfNeeded()
    }

    fun checkAndResetIfNeeded() {
        val today = LocalDate.now().toString()
        if (prefs.getString(KEY_RESET_DATE, "") != today) {
            archiveTodayToHistory()
            reset()
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
        prefs.edit()
            .putLong(KEY_TOTAL_SECONDS, totalSeconds)
            .putLong(KEY_SCROLL_EVENTS, scrollEventsToday)
            .apply()
    }

    fun incrementScrollEvent() {
        scrollEventsToday++
    }

    fun getScrollEvents(): Long = scrollEventsToday

    fun getLifetimeSeconds(): Long = lifetimeSeconds + totalSeconds

    fun getTotalSeconds(): Long = totalSeconds

    fun getFormattedTime(): String = formatSeconds(totalSeconds)

    fun formatSeconds(s: Long): String = when {
        s < 60 -> "${s}s"
        s < 3600 -> "${s / 60}m ${s % 60}s"
        else -> "${s / 3600}h ${(s % 3600) / 60}m"
    }

    fun getHistory(): List<DayStats> {
        val today = LocalDate.now()
        val historyJson = prefs.getString(KEY_DAY_HISTORY, "{}") ?: "{}"
        val obj = try { JSONObject(historyJson) } catch (e: Exception) { JSONObject() }

        val result = mutableListOf<DayStats>()
        for (i in 6 downTo 1) {
            val date = today.minusDays(i.toLong()).toString()
            val day = if (obj.has(date)) obj.getJSONObject(date) else null
            result.add(DayStats(date, day?.optLong("s", 0) ?: 0, day?.optLong("e", 0) ?: 0))
        }
        result.add(DayStats(today.toString(), totalSeconds, scrollEventsToday))
        return result
    }

    private fun archiveTodayToHistory() {
        if (totalSeconds == 0L) return
        val lastDate = prefs.getString(KEY_RESET_DATE, "") ?: ""
        if (lastDate.isEmpty()) return
        val historyJson = prefs.getString(KEY_DAY_HISTORY, "{}") ?: "{}"
        val obj = try { JSONObject(historyJson) } catch (e: Exception) { JSONObject() }
        obj.put(lastDate, JSONObject().put("s", totalSeconds).put("e", scrollEventsToday))
        val keys = obj.keys().asSequence().sorted().toList()
        if (keys.size > 30) keys.take(keys.size - 30).forEach { obj.remove(it) }
        val newLifetime = lifetimeSeconds + totalSeconds
        prefs.edit()
            .putString(KEY_DAY_HISTORY, obj.toString())
            .putLong(KEY_LIFETIME_SECONDS, newLifetime)
            .apply()
        lifetimeSeconds = newLifetime
    }

    fun reset() {
        stopTicking()
        totalSeconds = 0
        scrollEventsToday = 0
        prefs.edit()
            .putLong(KEY_TOTAL_SECONDS, 0)
            .putLong(KEY_SCROLL_EVENTS, 0)
            .putString(KEY_RESET_DATE, LocalDate.now().toString())
            .apply()
    }
}
