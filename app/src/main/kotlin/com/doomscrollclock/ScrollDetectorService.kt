package com.doomscrollclock

import android.accessibilityservice.AccessibilityService
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.abs

class ScrollDetectorService : AccessibilityService() {

    companion object {
        private const val TAG = "DoomScrollClock"
        private const val URL_REFRESH_DELAY_MS = 500L
        private const val YOUTUBE_DEBOUNCE_MS = 400L
        private const val BROWSER_URL_REFRESH_INTERVAL = 15

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
        private val CHROME_URL_BAR_IDS = listOf(
            "url_bar",
            "location_bar_edit_text",
            "search_box_text",
            "omnibox_text",
            "url_field"
        )

        // Maps domain → disable key stored in prefs
        private val DOMAIN_TO_KEY = mapOf(
            "instagram.com" to "instagram",
            "youtube.com" to "youtube",
            "reddit.com" to "reddit",
            "snapchat.com" to "snapchat"
        )
        private val PACKAGE_TO_KEY = mapOf(
            "com.instagram.android" to "instagram",
            "com.google.android.youtube" to "youtube",
            "com.reddit.frontpage" to "reddit",
            "com.snapchat.android" to "snapchat"
        )
    }

    private var cachedBrowserUrl = ""
    private var browserScrollCount = 0
    private val midnightReceiver = MidnightResetReceiver()
    private var alarmManager: AlarmManager? = null
    private var lastYoutubeContentChangeMs = 0L
    private var lastBrowserUrlRefreshMs = 0L

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
            NotificationHelper.init(applicationContext)
        } catch (e: Exception) {
            Log.e(TAG, "NotificationHelper init failed", e)
        }
        try {
            alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            scheduleMidnightAlarm()
        } catch (e: Exception) {
            Log.e(TAG, "Midnight alarm setup failed", e)
        }
        try {
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

        TimerManager.achievementListener = { level ->
            OverlayManager.showAchievement(level)
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val pkg = event.packageName?.toString() ?: return
        try {
            when (event.eventType) {
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                    if (pkg in BROWSER_PACKAGES) {
                        refreshBrowserUrl(pkg)
                        handler.removeCallbacks(urlRefreshRunnable)
                        pendingUrlRefreshPkg = pkg
                        handler.postDelayed(urlRefreshRunnable, URL_REFRESH_DELAY_MS)
                        browserScrollCount = 0
                    }
                }
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                    // YouTube Shorts swipe-navigation doesn't fire TYPE_VIEW_SCROLLED,
                    // so we use content changes debounced at 400ms as a proxy.
                    if (pkg == "com.google.android.youtube" && isPackageEnabled(pkg)) {
                        val now = SystemClock.elapsedRealtime()
                        if (now - lastYoutubeContentChangeMs > YOUTUBE_DEBOUNCE_MS) {
                            lastYoutubeContentChangeMs = now
                            TimerManager.incrementScrollEvent(0)
                            OverlayManager.show()
                            OverlayManager.scheduleHide()
                        }
                    }
                    // Also use content changes to keep browser URL fresh
                    if (pkg in BROWSER_PACKAGES) {
                        val now = SystemClock.elapsedRealtime()
                        if (now - lastBrowserUrlRefreshMs > 1000L) {
                            lastBrowserUrlRefreshMs = now
                            refreshBrowserUrl(pkg)
                        }
                    }
                }
                AccessibilityEvent.TYPE_VIEW_SCROLLED -> {
                    val deltaY = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        abs(event.scrollDeltaY)
                    } else 0

                    if (pkg in TARGET_PACKAGES && isPackageEnabled(pkg)) {
                        TimerManager.incrementScrollEvent(deltaY)
                        OverlayManager.show()
                        OverlayManager.scheduleHide()
                    } else if (pkg in BROWSER_PACKAGES) {
                        browserScrollCount++
                        if (cachedBrowserUrl.isEmpty() || browserScrollCount % BROWSER_URL_REFRESH_INTERVAL == 0) {
                            refreshBrowserUrl(pkg)
                        }
                        if (isDomainTracked(cachedBrowserUrl)) {
                            TimerManager.incrementScrollEvent(deltaY)
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
        TimerManager.achievementListener = null
        try {
            unregisterReceiver(midnightReceiver)
        } catch (e: Exception) {
            // Not registered or already unregistered
        }
        alarmManager?.cancel(buildMidnightPendingIntent())
        OverlayManager.cleanup()
        NotificationHelper.cleanup()
        TimerManager.stopTicking()
        return super.onUnbind(intent)
    }

    private fun isPackageEnabled(pkg: String): Boolean {
        val key = PACKAGE_TO_KEY[pkg] ?: return true
        return TimerManager.isAppEnabled(key)
    }

    private fun isDomainTracked(url: String): Boolean {
        if (url.isEmpty()) return false
        return BROWSER_TARGET_DOMAINS.any { domain ->
            url.contains(domain, ignoreCase = true) &&
                TimerManager.isAppEnabled(DOMAIN_TO_KEY[domain] ?: return@any false)
        }
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
