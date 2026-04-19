package com.doomscrollclock

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

object NotificationHelper {

    private const val CHANNEL_ID = "doom_scroll_summary"
    private const val NOTIF_ID = 1

    private lateinit var appContext: Context
    private lateinit var notifManager: NotificationManager
    private var initialized = false

    fun init(context: Context) {
        if (initialized) return
        appContext = context.applicationContext
        notifManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notifManager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "Scroll Summary", NotificationManager.IMPORTANCE_LOW).apply {
                description = "Fun facts about your daily scrolling"
                setShowBadge(true)
            }
        )
        initialized = true
    }

    fun showSummary() {
        if (!initialized) return
        val totalSecs = TimerManager.getTotalSeconds()
        if (totalSecs < 9) return

        val level = FunFacts.getDailyLevel(totalSecs)
        val timeFact = FunFacts.getTimeFact(totalSecs)

        val title = "${level.emoji} ${level.title}"
        val body = if (timeFact != null) {
            "${timeFact.emoji} ${TimerManager.getFormattedTime()} today — like ${timeFact.text}"
        } else {
            "${TimerManager.getFormattedTime()} of doom-scrolling today"
        }

        val tapIntent = Intent(appContext, StatsActivity::class.java)
        val pi = PendingIntent.getActivity(
            appContext, 0, tapIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notif = androidx.core.app.NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(pi)
            .setAutoCancel(true)
            .build()

        notifManager.notify(NOTIF_ID, notif)
    }

    fun cleanup() {
        if (::notifManager.isInitialized) notifManager.cancel(NOTIF_ID)
        initialized = false
    }
}
