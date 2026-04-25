package com.doomscrollclock

import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

object TimerManager {

    private const val FALLBACK_METRES_PER_EVENT = 0.002 // 2mm for devices without scrollDeltaY (API < 28)

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

    // ── Ticker ────────────────────────────────────────────────────────────────

    private val tickRunnable = object : Runnable {
        override fun run() {
            totalSeconds++
            OverlayManager.updateDisplay(totalSeconds)
            prefs.edit().putLong(Prefs.KEY_TOTAL_SECONDS, totalSeconds).apply()
            checkAndFireAchievements()
            handler.postDelayed(this, 1000)
        }
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    fun init(context: Context) {
        if (::prefs.isInitialized) return
        prefs = context.applicationContext.getSharedPreferences(Prefs.NAME, Context.MODE_PRIVATE)
        screenDpi = context.applicationContext.resources.displayMetrics.densityDpi.toFloat()
        totalSeconds = prefs.getLong(Prefs.KEY_TOTAL_SECONDS, 0)
        scrollEventsToday = prefs.getLong(Prefs.KEY_SCROLL_EVENTS, 0)
        scrollMetresToday = prefs.getFloat(Prefs.KEY_SCROLL_METRES, 0f).toDouble()
        lifetimeSeconds = prefs.getLong(Prefs.KEY_LIFETIME_SECONDS, 0)
        checkAndResetIfNeeded()
    }

    fun checkAndResetIfNeeded() {
        if (prefs.getString(Prefs.KEY_RESET_DATE, "") != LocalDate.now().toString()) {
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
            .putLong(Prefs.KEY_TOTAL_SECONDS, totalSeconds)
            .putLong(Prefs.KEY_SCROLL_EVENTS, scrollEventsToday)
            .putFloat(Prefs.KEY_SCROLL_METRES, scrollMetresToday.toFloat())
            .apply()
    }

    fun reset() {
        stopTicking()
        totalSeconds = 0
        scrollEventsToday = 0
        scrollMetresToday = 0.0
        prefs.edit()
            .putLong(Prefs.KEY_TOTAL_SECONDS, 0)
            .putLong(Prefs.KEY_SCROLL_EVENTS, 0)
            .putFloat(Prefs.KEY_SCROLL_METRES, 0f)
            .putString(Prefs.KEY_RESET_DATE, LocalDate.now().toString())
            .putInt(Prefs.KEY_LAST_DAILY_LEVEL, 0)
            .apply()
    }

    // ── Scroll events ─────────────────────────────────────────────────────────

    fun incrementScrollEvent(deltaPixels: Int = 0) {
        scrollEventsToday++
        val metres = if (deltaPixels > 0 && screenDpi > 0) {
            deltaPixels * 25.4 / (screenDpi * 1000.0)
        } else {
            FALLBACK_METRES_PER_EVENT
        }
        scrollMetresToday += metres
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

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

    // ── Display mode ──────────────────────────────────────────────────────────

    fun getDisplayMode(): String =
        prefs.getString(Prefs.KEY_DISPLAY_MODE, Prefs.DISPLAY_MODE_TIME) ?: Prefs.DISPLAY_MODE_TIME

    fun setDisplayMode(mode: String) =
        prefs.edit().putString(Prefs.KEY_DISPLAY_MODE, mode).apply()

    // ── Per-app toggles ───────────────────────────────────────────────────────

    fun isAppEnabled(key: String): Boolean {
        val disabled = prefs.getStringSet(Prefs.KEY_DISABLED_APPS, emptySet()) ?: emptySet()
        return key !in disabled
    }

    fun setAppEnabled(key: String, enabled: Boolean) {
        val current = prefs.getStringSet(Prefs.KEY_DISABLED_APPS, mutableSetOf())
            ?.toMutableSet() ?: mutableSetOf()
        if (enabled) current.remove(key) else current.add(key)
        prefs.edit().putStringSet(Prefs.KEY_DISABLED_APPS, current).apply()
    }

    // ── History ───────────────────────────────────────────────────────────────

    fun getHistory(): List<DayStats> {
        val today = LocalDate.now()
        val obj = parseJsonObject(prefs.getString(Prefs.KEY_DAY_HISTORY, "{}"))
        return (6 downTo 1).map { i ->
            val date = today.minusDays(i.toLong()).toString()
            val day = if (obj.has(date)) obj.getJSONObject(date) else null
            DayStats(date, day?.optLong("s", 0) ?: 0, day?.optLong("e", 0) ?: 0)
        } + DayStats(today.toString(), totalSeconds, scrollEventsToday)
    }

    fun getAchievementHistory(): List<Achievement> {
        val arr = parseJsonArray(prefs.getString(Prefs.KEY_ACHIEVEMENT_HISTORY, "[]"))
        return (arr.length() - 1 downTo 0).mapNotNull { i ->
            val obj = arr.optJSONObject(i) ?: return@mapNotNull null
            Achievement(
                date = obj.optString("date", ""),
                type = obj.optString("type", "daily"),
                emoji = obj.optString("emoji", ""),
                title = obj.optString("title", ""),
                tagline = obj.optString("tagline", "")
            )
        }
    }

    // ── Achievement detection ─────────────────────────────────────────────────

    private fun checkAndFireAchievements() {
        checkLevel(
            FunFacts.getDailyLevel(totalSeconds),
            FunFacts.dailyLevels,
            Prefs.KEY_LAST_DAILY_LEVEL,
            type = "daily"
        )
        checkLevel(
            FunFacts.getLifetimeLevel(lifetimeSeconds + totalSeconds),
            FunFacts.lifetimeLevels,
            Prefs.KEY_LAST_LIFETIME_LEVEL,
            type = "lifetime"
        )
    }

    private fun checkLevel(
        current: FunFacts.Level,
        allLevels: List<FunFacts.Level>,
        prefKey: String,
        type: String
    ) {
        val currentIdx = allLevels.indexOf(current)
        val lastIdx = prefs.getInt(prefKey, 0)
        if (currentIdx > lastIdx) {
            prefs.edit().putInt(prefKey, currentIdx).apply()
            if (currentIdx > 0) {
                recordAchievement(current, type)
                achievementListener?.invoke(current)
            }
        }
    }

    private fun recordAchievement(level: FunFacts.Level, type: String) {
        val arr = parseJsonArray(prefs.getString(Prefs.KEY_ACHIEVEMENT_HISTORY, "[]"))
        arr.put(
            JSONObject()
                .put("date", LocalDate.now().toString())
                .put("type", type)
                .put("emoji", level.emoji)
                .put("title", level.title)
                .put("tagline", level.tagline)
        )
        val trimmed = if (arr.length() > Prefs.MAX_ACHIEVEMENT_HISTORY) {
            JSONArray().also { fresh ->
                for (i in (arr.length() - Prefs.MAX_ACHIEVEMENT_HISTORY) until arr.length()) {
                    fresh.put(arr.get(i))
                }
            }
        } else arr
        prefs.edit().putString(Prefs.KEY_ACHIEVEMENT_HISTORY, trimmed.toString()).apply()
    }

    private fun archiveTodayToHistory() {
        if (totalSeconds == 0L) return
        val lastDate = prefs.getString(Prefs.KEY_RESET_DATE, "") ?: ""
        if (lastDate.isEmpty()) return
        val obj = parseJsonObject(prefs.getString(Prefs.KEY_DAY_HISTORY, "{}"))
        obj.put(lastDate, JSONObject().put("s", totalSeconds).put("e", scrollEventsToday).put("m", scrollMetresToday))
        val keys = obj.keys().asSequence().sorted().toList()
        if (keys.size > Prefs.MAX_DAY_HISTORY) keys.take(keys.size - Prefs.MAX_DAY_HISTORY).forEach { obj.remove(it) }
        val newLifetime = lifetimeSeconds + totalSeconds
        prefs.edit()
            .putString(Prefs.KEY_DAY_HISTORY, obj.toString())
            .putLong(Prefs.KEY_LIFETIME_SECONDS, newLifetime)
            .apply()
        lifetimeSeconds = newLifetime
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun parseJsonObject(json: String?): JSONObject =
        try { JSONObject(json ?: "{}") } catch (e: Exception) { JSONObject() }

    private fun parseJsonArray(json: String?): JSONArray =
        try { JSONArray(json ?: "[]") } catch (e: Exception) { JSONArray() }
}
