package com.doomscrollclock

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.core.app.NotificationCompat

object NotificationHelper {

    private const val CHANNEL_ID = "doom_scroll_active"
    const val NOTIF_ID = 1
    private const val PREFS_NAME = "doom_scroll_prefs"
    private const val KEY_DISPLAY_MODE = "notif_display_mode"

    private lateinit var appContext: Context
    private lateinit var notifManager: NotificationManager
    private val handler = Handler(Looper.getMainLooper())
    private var initialized = false
    private var isActive = false

    private val stopRunnable = Runnable {
        TimerManager.stopTicking()
        isActive = false
        if (::notifManager.isInitialized) {
            notifManager.notify(NOTIF_ID, buildNotification(ongoing = false))
        }
    }

    fun init(context: Context) {
        if (initialized) return
        appContext = context.applicationContext
        notifManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notifManager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "Scroll Timer", NotificationManager.IMPORTANCE_LOW).apply {
                description = "Shows your live scroll time"
                setShowBadge(false)
            }
        )
        initialized = true
    }

    fun show() {
        if (!initialized) return
        if (!isActive) {
            TimerManager.startTicking()
            isActive = true
        }
        notifManager.notify(NOTIF_ID, buildNotification(ongoing = true))
    }

    fun scheduleHide() {
        handler.removeCallbacks(stopRunnable)
        handler.postDelayed(stopRunnable, 1500)
    }

    fun updateDisplay() {
        if (!initialized) return
        notifManager.notify(NOTIF_ID, buildNotification(ongoing = isActive))
    }

    fun cleanup() {
        handler.removeCallbacksAndMessages(null)
        TimerManager.stopTicking()
        if (::notifManager.isInitialized) notifManager.cancel(NOTIF_ID)
        isActive = false
        initialized = false
    }

    fun buildNotification(ongoing: Boolean = true): Notification {
        val mode = if (::appContext.isInitialized) {
            appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_DISPLAY_MODE, "time") ?: "time"
        } else "time"

        val contentText = if (mode == "distance") {
            val metres = TimerManager.getScrollEvents() * 0.5
            if (metres < 1000) "📏 ~${metres.toInt()}m scrolled today"
            else "📏 ~${"%.1f".format(metres / 1000)}km scrolled today"
        } else {
            "⏱ ${TimerManager.getFormattedTime()} today"
        }

        val tapIntent = Intent(appContext, StatsActivity::class.java)
        val pi = PendingIntent.getActivity(
            appContext, 0, tapIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Doom Scroll Clock")
            .setContentText(contentText)
            .setOngoing(ongoing)
            .setContentIntent(pi)
            .setShowWhen(false)
            .build()
    }
}
