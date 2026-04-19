package com.doomscrollclock

import android.accessibilityservice.AccessibilityService
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
        try {
            TimerManager.init(applicationContext)
            OverlayManager.init(applicationContext)

            // Restrict OS-level event delivery to target packages only
            serviceInfo?.let { info ->
                info.packageNames = (TARGET_PACKAGES + BROWSER_PACKAGES).toTypedArray()
                serviceInfo = info
            }

            alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            scheduleMidnightAlarm()

            // Android 14 (API 34) requires RECEIVER_NOT_EXPORTED even for system broadcasts
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(
                    midnightReceiver,
                    IntentFilter(Intent.ACTION_DATE_CHANGED),
                    RECEIVER_NOT_EXPORTED
                )
            } else {
                @Suppress("UnspecifiedRegisterReceiverFlag")
                registerReceiver(midnightReceiver, IntentFilter(Intent.ACTION_DATE_CHANGED))
            }
        } catch (e: Exception) {
            // Prevent crash-loop from killing the service
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        try {
            val pkg = event.packageName?.toString() ?: return

            when (event.eventType) {
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                    if (pkg in BROWSER_PACKAGES) {
                        val root = rootInActiveWindow ?: return
                        val urlNodes = root.findAccessibilityNodeInfosByViewId("$pkg:id/url_bar")
                        cachedBrowserUrl = urlNodes.firstOrNull()?.text?.toString()
                            ?: cachedBrowserUrl
                    }
                }
                AccessibilityEvent.TYPE_VIEW_SCROLLED -> {
                    val isTargetApp = pkg in TARGET_PACKAGES
                    val isTargetBrowserPage = pkg in BROWSER_PACKAGES &&
                            BROWSER_TARGET_DOMAINS.any {
                                cachedBrowserUrl.contains(it, ignoreCase = true)
                            }
                    if (isTargetApp || isTargetBrowserPage) {
                        OverlayManager.show()
                        OverlayManager.scheduleHide()
                    }
                }
            }
        } catch (e: Exception) {
            // Swallow to prevent crash-looping the service
        }
    }

    override fun onInterrupt() = Unit

    override fun onUnbind(intent: Intent): Boolean {
        try {
            unregisterReceiver(midnightReceiver)
        } catch (e: Exception) {
            // Not registered or already unregistered
        }
        alarmManager?.cancel(buildMidnightPendingIntent())
        OverlayManager.cleanup()
        TimerManager.stopTicking()
        return super.onUnbind(intent)
    }

    private fun scheduleMidnightAlarm() {
        val am = alarmManager ?: return
        val pendingIntent = buildMidnightPendingIntent()
        val midnight = nextMidnightMillis()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) {
            am.setWindow(AlarmManager.RTC_WAKEUP, midnight, 10 * 60 * 1000L, pendingIntent)
        } else {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, midnight, pendingIntent)
        }
    }

    private fun buildMidnightPendingIntent(): PendingIntent {
        return PendingIntent.getBroadcast(
            this,
            0,
            Intent(this, MidnightResetReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun nextMidnightMillis(): Long =
        LocalDate.now().plusDays(1)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
}
