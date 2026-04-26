# Doom Scroll Clock — Claude Code Reference

## What this app does
An Android accessibility service that tracks time spent scrolling in social media apps (Instagram, YouTube, Reddit, Snapchat) and their browser equivalents in Chrome/Brave. A floating pill overlay shows live time or distance. Achievements unlock as you hit daily/lifetime milestones. The app resets daily at midnight.

## Architecture overview

```
ScrollDetectorService (AccessibilityService)
  ├── Detects TYPE_VIEW_SCROLLED  → TimerManager.incrementScrollEvent(deltaPixels)
  ├── Detects TYPE_WINDOW_CONTENT_CHANGED → YouTube Shorts debounce
  ├── Browser URL parsing via findAccessibilityNodeInfosByViewId
  └── Sets TimerManager.achievementListener → OverlayManager.showAchievement()

TimerManager (singleton object)
  ├── Ticks totalSeconds every 1s via Handler
  ├── Accumulates scrollMetresToday (pixel→metres via screen DPI)
  ├── Checks daily/lifetime level changes → fires achievementListener
  ├── Archives to 30-day JSON history on midnight reset
  └── Stores achievement history (capped at 100 entries)

OverlayManager (singleton object)
  ├── WindowManager TYPE_APPLICATION_OVERLAY pill
  ├── GlowPillView: pulsing red glow via ValueAnimator + BlurMaskFilter
  ├── showAchievement(level): expands pill text for 4s then reverts
  └── showEndOfSessionSummary(): shows fun fact for 3.5s then removes pill

FunFacts (object)
  ├── 8 daily levels (0→4h+) + 8 lifetime levels (0→1yr cumulative)
  ├── timeFacts: 36 entries, 2 per tier, rotates every 2 min via seconds/120
  └── distanceFacts: 24 entries, 2 per tier, threshold in physical metres
```

## Key files

| File | Purpose |
|------|---------|
| `ScrollDetectorService.kt` | Core accessibility service — detects scrolls, fires events |
| `TimerManager.kt` | State machine for time/distance/achievements — singleton |
| `FunFacts.kt` | All levels, facts, distance milestones — pure data + logic |
| `OverlayManager.kt` | Floating pill UI — WindowManager + custom canvas |
| `HistoryChartView.kt` | Custom canvas bar chart for 7-day history |
| `NotificationHelper.kt` | Legacy — kept for permission UI, no longer calls showSummary() |
| `MidnightResetReceiver.kt` | AlarmManager BroadcastReceiver for daily reset |
| `MainActivity.kt` | Single activity hosting 3-tab bottom nav |
| `TimeFragment.kt` | Time tab: hero time, levels, fact card, 7-day chart, achievement history |
| `DistanceFragment.kt` | Distance tab: hero distance, distance fact |
| `SettingsFragment.kt` | Settings: permissions, pill mode toggle, per-app toggles |

## SharedPreferences keys (`"doom_scroll_prefs"`)

| Key | Type | Purpose |
|-----|------|---------|
| `total_seconds_today` | Long | Daily scroll seconds |
| `scroll_events_today` | Long | Daily scroll event count |
| `scroll_metres_today` | Float | Daily physical distance in metres |
| `last_reset_date` | String | ISO date of last midnight reset |
| `lifetime_seconds` | Long | Cumulative lifetime scroll seconds |
| `day_history` | String (JSON) | 30-day rolling history `{date: {s, e, m}}` |
| `last_daily_level_idx` | Int | Index into `FunFacts.dailyLevels` — resets to 0 nightly |
| `last_lifetime_level_idx` | Int | Index into `FunFacts.lifetimeLevels` — never resets |
| `achievement_history` | String (JSON array) | Last 100 achievements `[{date,type,emoji,title,tagline}]` |
| `pill_display_mode` | String | `"time"` or `"distance"` |
| `disabled_apps` | Set\<String\> | App keys opted out: `instagram`, `youtube`, `reddit`, `snapchat` |

## Distance calculation

`event.scrollDeltaY` (API 28+) gives pixel displacement per scroll event.  
`metres = abs(deltaPixels) * 25.4 / (densityDpi * 1000.0)`  
Fallback for API < 28: `0.002` m per event (2mm).

## YouTube detection

- Native app (`com.google.android.youtube`): `TYPE_VIEW_SCROLLED` covers the home feed; `TYPE_WINDOW_CONTENT_CHANGED` (debounced 400ms) covers Shorts swipe-navigation.
- Browser: `cachedBrowserUrl` is read via `findAccessibilityNodeInfosByViewId` using IDs `url_bar`, `location_bar_edit_text`, `search_box_text`, `omnibox_text`, `url_field`. URL is refreshed on `TYPE_WINDOW_STATE_CHANGED` and content changes.

## Build / CI

- **Build**: `./gradlew assembleDebug --no-daemon --stacktrace`
- **CI**: GitHub Actions `.github/workflows/build.yml` — triggers on push to `main` and on PRs
- **minSdk**: 26, **targetSdk**: 35, **compileSdk**: 35
- **AGP**: 8.4.2, **Kotlin**: 1.9.24
- Known warning: AGP 8.4.2 was tested up to compileSdk 34 — safe to suppress with `android.suppressUnsupportedCompileSdk=35` in `gradle.properties`

## Active branch

Development happens on `claude/fix-build-actions-7qVNI`, kept in sync with PR branch `claude/scrolling-time-tracker-JFQPu` via fast-forward pushes:
```
git push origin claude/fix-build-actions-7qVNI:claude/scrolling-time-tracker-JFQPu
```
