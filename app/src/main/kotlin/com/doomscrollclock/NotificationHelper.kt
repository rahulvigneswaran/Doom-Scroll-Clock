package com.doomscrollclock

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

object NotificationHelper {

    private const val CHANNEL_ID = "doom_scroll_summary"
    private const val NOTIF_ID = 1

    private lateinit var notifManager: NotificationManager
    private var initialized = false

    fun init(context: Context) {
        if (initialized) return
        notifManager = context.applicationContext
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notifManager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "Scroll Summary", NotificationManager.IMPORTANCE_LOW).apply {
                description = "Fun facts about your daily scrolling"
                setShowBadge(true)
            }
        )
        initialized = true
    }

    fun cleanup() {
        if (::notifManager.isInitialized) notifManager.cancel(NOTIF_ID)
        initialized = false
    }
}
