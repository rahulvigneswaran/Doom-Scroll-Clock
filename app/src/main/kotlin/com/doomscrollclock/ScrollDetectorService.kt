package com.doomscrollclock

import android.accessibilityservice.AccessibilityService
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import java.time.LocalDate
import java.time.ZoneId

class ScrollDetectorService : AccessibilityService() {

    companion object {
        private const val TAG = "DoomScrollClock"
        private const val URL_REFRESH_DELAY_MS = 500L

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
        // Try multiple IDs — Chrome's URL bar resource name varies across versions
        private val CHROME_URL_BAR_IDS = listOf(
            "url_bar",
            "location_bar_edit_text",
            "search_box_text"
        )
    }

    private var cachedBrowserUrl = ""
    private val midnightReceiver = MidnightResetReceiver()
    private var alarmManager: AlarmManager? = null

    private val handler = Handler(Looper.getMainLooper())
    private var pendingUrlRefreshPkg = ""
    private val urlRefreshRunnable = Runnable { refreshBrowserUrl(pendingUrlRefreshPkg) }

    override fun onServiceConnected() {
        try {
            TimerManager.init(applicationContext)
        } catch (e: Exception) {
            Log.e(TAG, "TimerManager init failed", e)
        }
        try {
            OverlayManager.init(applicationContext)
        } catch (e: Exception) {
            Log.e(TAG, "OverlayManager init failed", e)
        }
        try {
            alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            scheduleMidnightAlarm()
        } catch (e: Exception) {
            Log.e(TAG, "Midnight alarm setup failed", e)
        }
        try {
            // API 33+ (TIRAMISU) requires RECEIVER_NOT_EXPORTED for dynamically registered
            // receivers; enforcement depends on targetSdk/runtime behavior.
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
            Log.e(TAG, "Failed to register midnight receiver", e)
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val pkg = event.packageName?.toString() ?: return
        try {
            when (event.eventType) {
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                    if (pkg in BROWSER_PACKAGES) {
                        // Delay the read — URL bar text isn't populated at the moment this event fires
                        handler.removeCallbacks(urlRefreshRunnable)
                        pendingUrlRefreshPkg = pkg
                        handler.postDelayed(urlRefreshRunnable, URL_REFRESH_DELAY_MS)
                    }
                }
                AccessibilityEvent.TYPE_VIEW_SCROLLED -> {
                    if (pkg in TARGET_PACKAGES) {
                        OverlayManager.show()
                        OverlayManager.scheduleHide()
                    } else if (pkg in BROWSER_PACKAGES) {
                        // If cache is empty (first scroll before state-change delay fires), try now
                        if (cachedBrowserUrl.isEmpty()) refreshBrowserUrl(pkg)
                        if (BROWSER_TARGET_DOMAINS.any {
                                cachedBrowserUrl.contains(it, ignoreCase = true)
                            }) {
                            OverlayManager.show()
                            OverlayManager.scheduleHide()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error handling accessibility event from $pkg", e)
        }
    }

    override fun onInterrupt() = Unit

    override fun onUnbind(intent: Intent): Boolean {
        handler.removeCallbacksAndMessages(null)
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

    private fun refreshBrowserUrl(pkg: String) {
        if (pkg.isEmpty()) return
        try {
            val root = rootInActiveWindow ?: return
            for (id in CHROME_URL_BAR_IDS) {
                val text = root.findAccessibilityNodeInfosByViewId("$pkg:id/$id")
                    .firstOrNull()?.text?.toString()
                if (!text.isNullOrEmpty()) {
                    cachedBrowserUrl = text
                    return
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not read browser URL from $pkg", e)
        }
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

    private fun buildMidnightPendingIntent(): PendingIntent =
        PendingIntent.getBroadcast(
            this, 0,
            Intent(this, MidnightResetReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    private fun nextMidnightMillis(): Long =
        LocalDate.now().plusDays(1)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
}
