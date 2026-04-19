package com.doomscrollclock

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import java.time.LocalDate
import java.time.ZoneId

class ScrollDetectorService : AccessibilityService() {

    companion object {
        private val TARGET_PACKAGES = setOf(
            "com.instagram.android",
            "com.snapchat.android",
            "com.google.android.youtube",
            "com.reddit.frontpage"
        )
        private val BROWSER_PACKAGES = setOf(
            "com.android.chrome",
            "com.brave.browser"
        )
        private val BROWSER_TARGET_DOMAINS = setOf(
            "instagram.com",
            "reddit.com",
            "youtube.com",
            "snapchat.com"
        )
    }

    private var cachedBrowserUrl = ""
    private val midnightReceiver = MidnightResetReceiver()
    private var alarmManager: AlarmManager? = null

    override fun onServiceConnected() {
        TimerManager.init(applicationContext)
        OverlayManager.init(applicationContext)
        // Scope OS-level event delivery to only our target packages for battery efficiency
        serviceInfo = serviceInfo.apply {
            packageNames = (TARGET_PACKAGES + BROWSER_PACKAGES).toTypedArray()
        }
        alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        scheduleMidnightAlarm()
        registerReceiver(midnightReceiver, IntentFilter(Intent.ACTION_DATE_CHANGED))
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val pkg = event.packageName?.toString() ?: return

        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                if (pkg in BROWSER_PACKAGES) {
                    val root = rootInActiveWindow ?: return
                    val urlNodes = root.findAccessibilityNodeInfosByViewId("$pkg:id/url_bar")
                    cachedBrowserUrl = urlNodes.firstOrNull()?.text?.toString() ?: cachedBrowserUrl
                }
            }
            AccessibilityEvent.TYPE_VIEW_SCROLLED -> {
                val isTargetApp = pkg in TARGET_PACKAGES
                val isTargetBrowserPage = pkg in BROWSER_PACKAGES &&
                        BROWSER_TARGET_DOMAINS.any { cachedBrowserUrl.contains(it, ignoreCase = true) }

                if (isTargetApp || isTargetBrowserPage) {
                    OverlayManager.show()
                    OverlayManager.scheduleHide()
                }
            }
        }
    }

    override fun onInterrupt() = Unit

    override fun onUnbind(intent: Intent): Boolean {
        try {
            unregisterReceiver(midnightReceiver)
        } catch (e: IllegalArgumentException) {
            // Not registered
        }
        alarmManager?.cancel(buildMidnightPendingIntent())
        OverlayManager.cleanup()
        TimerManager.stopTicking()
        return super.onUnbind(intent)
    }

    private fun scheduleMidnightAlarm() {
        val am = alarmManager ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) {
            // Fall back to inexact alarm — still resets within ~10 minutes of midnight
            am.setWindow(
                AlarmManager.RTC_WAKEUP,
                nextMidnightMillis(),
                10 * 60 * 1000L,
                buildMidnightPendingIntent()
            )
            return
        }
        am.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            nextMidnightMillis(),
            buildMidnightPendingIntent()
        )
    }

    private fun buildMidnightPendingIntent(): PendingIntent {
        val intent = Intent(this, MidnightResetReceiver::class.java)
        return PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun nextMidnightMillis(): Long {
        return LocalDate.now()
            .plusDays(1)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }
}
