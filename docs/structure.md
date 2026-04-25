# Codebase Structure

## Kotlin source files
`app/src/main/kotlin/com/doomscrollclock/`

```
DoomScrollClockApp.kt         Application class — calls TimerManager.init() on app start
MainActivity.kt               Single activity — hosts BottomNavigationView + fragment container
TimeFragment.kt               "Time" tab — hero time, daily/lifetime levels, fact, 7-day chart, achievements
DistanceFragment.kt           "Distance" tab — hero distance, distance fact
SettingsFragment.kt           "Settings" tab — permissions, pill mode, per-app toggles
ScrollDetectorService.kt      AccessibilityService — core scroll detection engine
TimerManager.kt               Singleton — all state: time, distance, achievements, history
FunFacts.kt                   Pure data — levels, facts, distance milestones, formatting
OverlayManager.kt             Singleton — WindowManager floating pill UI
HistoryChartView.kt           Custom View — canvas bar chart for 7-day history
NotificationHelper.kt         Notification channel setup (permission flow only)
MidnightResetReceiver.kt      BroadcastReceiver — fires at midnight, also reschedules next alarm
```

## Resource files
```
res/layout/
  activity_main.xml           CoordinatorLayout + BottomNavigationView
  fragment_time.xml           Time tab layout
  fragment_distance.xml       Distance tab layout
  fragment_settings.xml       Settings tab layout
  item_achievement.xml        Achievement history row

res/menu/
  bottom_nav_menu.xml         3-item bottom nav (nav_time, nav_distance, nav_settings)

res/values/
  strings.xml                 All user-facing strings
  colors.xml                  Color resources
  themes.xml                  Material3 theme declaration

res/xml/
  accessibility_service_config.xml   Event types + flags for the accessibility service
```

## Navigation
`MainActivity` uses a `FrameLayout` fragment container. `BottomNavigationView.setOnItemSelectedListener` replaces fragments directly (no Navigation component). Fragment instances are created fresh on each tab tap — `onResume` drives all data refresh.

## Data flow
```
Scroll event
  → ScrollDetectorService.onAccessibilityEvent()
  → TimerManager.incrementScrollEvent(deltaPixels)
  → OverlayManager.show() + scheduleHide()

Every 1 second (while overlay is visible)
  → TimerManager.tickRunnable
  → OverlayManager.updateDisplay(totalSeconds)
  → TimerManager.checkAndFireAchievements()
    → achievementListener?.invoke(level)
      → OverlayManager.showAchievement(level)

1.5s after last scroll
  → OverlayManager.hideRunnable
  → TimerManager.stopTicking()
  → OverlayManager.showEndOfSessionSummary() [shows fun fact for 3.5s]

Midnight
  → MidnightResetReceiver.onReceive()
  → TimerManager.checkAndResetIfNeeded()
    → archiveTodayToHistory() + reset()
```
