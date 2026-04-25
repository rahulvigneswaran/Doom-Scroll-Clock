package com.doomscrollclock

import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

object TimerManager {

    private const val PREFS_NAME = "doom_scroll_prefs"
    private const val KEY_TOTAL_SECONDS = "total_seconds_today"
    private const val KEY_RESET_DATE = "last_reset_date"
    private const val KEY_SCROLL_EVENTS = "scroll_events_today"
    private const val KEY_SCROLL_METRES = "scroll_metres_today"
    private const val KEY_LIFETIME_SECONDS = "lifetime_seconds"
    private const val KEY_DAY_HISTORY = "day_history"
    private const val KEY_LAST_DAILY_LEVEL = "last_daily_level_idx"
    private const val KEY_LAST_LIFETIME_LEVEL = "last_lifetime_level_idx"
    private const val KEY_ACHIEVEMENT_HISTORY = "achievement_history"

    private const val FALLBACK_METRES_PER_EVENT = 0.002 // 2mm fallback for API < 28

    private lateinit var prefs: SharedPreferences
    private val handler = Handler(Looper.getMainLooper())
    private var isRunning = false
    private var totalSeconds: Long = 0
    private var scrollEventsToday: Long = 0
    private var scrollMetresToday: Double = 0.0
    private var lifetimeSeconds: Long = 0
    private var screenDpi: Float = 160f

    var achievementListener: ((FunFacts.Level) -> Unit)? = null

    data class DayStats(val date: String, val seconds: Long, val scrollEvents: Long)
    data class Achievement(
        val date: String,
        val type: String,
        val emoji: String,
        val title: String,
        val tagline: String
    )

    private val tickRunnable = object : Runnable {
        override fun run() {
            totalSeconds++
            OverlayManager.updateDisplay(totalSeconds)
            prefs.edit().putLong(KEY_TOTAL_SECONDS, totalSeconds).apply()
            checkAndFireAchievements()
            handler.postDelayed(this, 1000)
        }
    }

    fun init(context: Context) {
        if (::prefs.isInitialized) return
        prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        screenDpi = context.applicationContext.resources.displayMetrics.densityDpi.toFloat()
        totalSeconds = prefs.getLong(KEY_TOTAL_SECONDS, 0)
        scrollEventsToday = prefs.getLong(KEY_SCROLL_EVENTS, 0)
        scrollMetresToday = prefs.getFloat(KEY_SCROLL_METRES, 0f).toDouble()
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
            .putFloat(KEY_SCROLL_METRES, scrollMetresToday.toFloat())
            .apply()
    }

    fun incrementScrollEvent(deltaPixels: Int = 0) {
        scrollEventsToday++
        val metres = if (deltaPixels > 0 && screenDpi > 0) {
            deltaPixels * 25.4 / (screenDpi * 1000.0)
        } else {
            FALLBACK_METRES_PER_EVENT
        }
        scrollMetresToday += metres
    }

    fun getScrollEvents(): Long = scrollEventsToday
    fun getScrollMetres(): Double = scrollMetresToday
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

    fun getAchievementHistory(): List<Achievement> {
        val json = prefs.getString(KEY_ACHIEVEMENT_HISTORY, "[]") ?: "[]"
        val arr = try { JSONArray(json) } catch (e: Exception) { return emptyList() }
        val result = mutableListOf<Achievement>()
        for (i in arr.length() - 1 downTo 0) {
            val obj = arr.optJSONObject(i) ?: continue
            result.add(
                Achievement(
                    obj.optString("date", ""),
                    obj.optString("type", "daily"),
                    obj.optString("emoji", ""),
                    obj.optString("title", ""),
                    obj.optString("tagline", "")
                )
            )
        }
        return result
    }

    private fun checkAndFireAchievements() {
        val dailyLevel = FunFacts.getDailyLevel(totalSeconds)
        val dailyIdx = FunFacts.dailyLevels.indexOf(dailyLevel)
        val lastDailyIdx = prefs.getInt(KEY_LAST_DAILY_LEVEL, 0)
        if (dailyIdx > lastDailyIdx) {
            prefs.edit().putInt(KEY_LAST_DAILY_LEVEL, dailyIdx).apply()
            if (dailyIdx > 0) {
                recordAchievement(dailyLevel, "daily")
                achievementListener?.invoke(dailyLevel)
            }
        }

        val lifetimeLevel = FunFacts.getLifetimeLevel(lifetimeSeconds + totalSeconds)
        val lifetimeIdx = FunFacts.lifetimeLevels.indexOf(lifetimeLevel)
        val lastLifetimeIdx = prefs.getInt(KEY_LAST_LIFETIME_LEVEL, 0)
        if (lifetimeIdx > lastLifetimeIdx) {
            prefs.edit().putInt(KEY_LAST_LIFETIME_LEVEL, lifetimeIdx).apply()
            if (lifetimeIdx > 0) {
                recordAchievement(lifetimeLevel, "lifetime")
                achievementListener?.invoke(lifetimeLevel)
            }
        }
    }

    private fun recordAchievement(level: FunFacts.Level, type: String) {
        val json = prefs.getString(KEY_ACHIEVEMENT_HISTORY, "[]") ?: "[]"
        val arr = try { JSONArray(json) } catch (e: Exception) { JSONArray() }
        arr.put(
            JSONObject()
                .put("date", LocalDate.now().toString())
                .put("type", type)
                .put("emoji", level.emoji)
                .put("title", level.title)
                .put("tagline", level.tagline)
        )
        // Cap at 100 entries
        val trimmed = if (arr.length() > 100) {
            val fresh = JSONArray()
            for (i in (arr.length() - 100) until arr.length()) fresh.put(arr.get(i))
            fresh
        } else arr
        prefs.edit().putString(KEY_ACHIEVEMENT_HISTORY, trimmed.toString()).apply()
    }

    private fun archiveTodayToHistory() {
        if (totalSeconds == 0L) return
        val lastDate = prefs.getString(KEY_RESET_DATE, "") ?: ""
        if (lastDate.isEmpty()) return
        val historyJson = prefs.getString(KEY_DAY_HISTORY, "{}") ?: "{}"
        val obj = try { JSONObject(historyJson) } catch (e: Exception) { JSONObject() }
        obj.put(
            lastDate,
            JSONObject()
                .put("s", totalSeconds)
                .put("e", scrollEventsToday)
                .put("m", scrollMetresToday)
        )
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
        scrollMetresToday = 0.0
        prefs.edit()
            .putLong(KEY_TOTAL_SECONDS, 0)
            .putLong(KEY_SCROLL_EVENTS, 0)
            .putFloat(KEY_SCROLL_METRES, 0f)
            .putString(KEY_RESET_DATE, LocalDate.now().toString())
            .putInt(KEY_LAST_DAILY_LEVEL, 0)
            .apply()
    }
}
