package com.doomscrollclock

object Prefs {
    const val NAME = "doom_scroll_prefs"

    // Daily counters (reset at midnight)
    const val KEY_TOTAL_SECONDS = "total_seconds_today"
    const val KEY_SCROLL_EVENTS = "scroll_events_today"
    const val KEY_SCROLL_METRES = "scroll_metres_today"

    // Dates and lifetime totals
    const val KEY_RESET_DATE = "last_reset_date"
    const val KEY_LIFETIME_SECONDS = "lifetime_seconds"

    // History
    const val KEY_DAY_HISTORY = "day_history"
    const val KEY_ACHIEVEMENT_HISTORY = "achievement_history"

    // Achievement level tracking
    const val KEY_LAST_DAILY_LEVEL = "last_daily_level_idx"
    const val KEY_LAST_LIFETIME_LEVEL = "last_lifetime_level_idx"

    // UI preferences
    const val KEY_DISPLAY_MODE = "pill_display_mode"
    const val KEY_DISABLED_APPS = "disabled_apps"

    const val DISPLAY_MODE_TIME = "time"
    const val DISPLAY_MODE_DISTANCE = "distance"

    // History limits
    const val MAX_DAY_HISTORY = 30
    const val MAX_ACHIEVEMENT_HISTORY = 100
}
