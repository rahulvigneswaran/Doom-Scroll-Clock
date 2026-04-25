---
name: Doom Scroll Clock

colors:
  # ── App UI ───────────────────────────────────────────────────────────────────
  background: "#F5FAFF"
  surface: "#FFFFFF"
  surface-variant: "#EBF3F9"
  divider: "#DDE8EF"

  primary: "#306B3F"
  primary-variant: "#235E34"
  primary-container: "#C8E6C9"
  on-primary: "#FFFFFF"

  on-surface: "#1B3543"
  on-surface-variant: "#5A7A8A"

  # ── Data visualization ────────────────────────────────────────────────────────
  chart-bar-today: "#306B3F"
  chart-bar-past: "#B0C9D6"

  # ── Status indicators ─────────────────────────────────────────────────────────
  status-granted: "#306B3F"
  status-not-granted: "#E53935"

  # ── Overlay pill (system-level, drawn above all other apps) ───────────────────
  pill-background: "#E6000000"
  pill-text: "#FFFFFF"
  pill-glow-outer: "#FF1744"
  pill-glow-inner: "#FFFFFF"

typography:
  hero:
    fontFamily: monospace
    fontSize: 56sp
    fontWeight: bold

  page-title:
    fontSize: 28sp
    fontWeight: bold

  level-title:
    fontSize: 18sp
    fontWeight: bold

  body:
    fontSize: 15sp
    fontWeight: regular

  body-secondary:
    fontSize: 13sp
    fontWeight: regular

  section-label:
    fontSize: 11sp
    fontWeight: bold
    textTransform: uppercase
    letterSpacing: "0.08em"

  card-row-title:
    fontSize: 15sp
    fontWeight: bold

  small:
    fontSize: 12sp
    fontWeight: regular

  achievement-title:
    fontSize: 14sp
    fontWeight: bold

  achievement-tagline:
    fontSize: 12sp
    fontWeight: regular

  achievement-meta:
    fontSize: 11sp
    fontWeight: regular

  pill:
    fontFamily: monospace
    fontSize: 13sp
    fontWeight: regular

radii:
  hero-card: 20dp
  card: 16dp
  pill: 24dp
  status-dot: 5dp

spacing:
  screen-horizontal: 20dp
  screen-top: 32dp
  screen-bottom: 16dp
  nav-height: 80dp
  card-padding: 20dp
  card-margin-hero: 16dp
  card-margin-content: 10dp
  card-margin-tight: 8dp
  section-label-bottom: 10dp
  section-gap: 24dp
  row-height: 56dp
  list-row-horizontal: 16dp

elevation:
  tinted-card: 0dp
  content-card: 1dp
  bottom-nav: 0dp

motion:
  pill-glow-pulse:
    property: alpha
    from: 0.45
    to: 1.0
    duration: 1500ms
    easing: linear
    repeat: reverse-infinite
  pill-hide-delay: 1500ms
  achievement-display: 4000ms
  achievement-revert: 1500ms

components:
  hero-card:
    backgroundColor: primary-container
    cornerRadius: hero-card
    elevation: tinted-card
    padding: card-padding

  content-card:
    backgroundColor: surface
    cornerRadius: card
    elevation: content-card
    padding: card-padding

  fact-card:
    backgroundColor: primary-container
    cornerRadius: card
    elevation: tinted-card
    padding: card-padding

  section-label:
    typography: section-label
    color: on-surface-variant
    marginBottom: section-label-bottom

  outlined-button:
    style: Widget.Material3.Button.OutlinedButton
    strokeColor: primary-variant
    textColor: on-surface

  toggle-group:
    style: Widget.Material3.Button.OutlinedButton
    singleSelection: true
    selectionRequired: true

  permission-row:
    backgroundColor: surface
    cornerRadius: card
    elevation: content-card
    padding: 16dp
    dot-size: 10dp
    dot-granted: status-granted
    dot-not-granted: status-not-granted

  app-toggle-row:
    height: row-height
    paddingHorizontal: list-row-horizontal
    dividerColor: surface-variant
    dividerHeight: 1dp

  chart-bar:
    colorToday: chart-bar-today
    colorPast: chart-bar-past
    trackColor: surface-variant

  achievement-row:
    paddingVertical: 12dp
    minHeight: row-height
    emojiSize: 26sp
    emojiWidth: 40dp
    dividerColor: surface-variant

  pill-overlay:
    backgroundColor: pill-background
    cornerRadius: pill
    textColor: pill-text
    typography: pill
    gravity: "TOP | CENTER_HORIZONTAL"
    offsetY: 56dp
    glow-outer:
      color: pill-glow-outer
      blurRadius: 36dp
      animation: pill-glow-pulse
      peakAlpha: 0.85
    glow-inner:
      color: pill-glow-inner
      blurRadius: 16dp
      animation: pill-glow-pulse
      peakAlpha: 0.70
---

## Brand & Style

Doom Scroll Clock has a deliberate split personality. The in-app UI is calm, clinical, and data-forward — a neutral mirror for your habits. The overlay pill is the opposite: a pulsing red specter that hovers over every feed you open. These two faces are in constant tension, and that tension is the brand.

The app never lectures. It just shows you the number. The discomfort comes from the pill — it is, by design, the thing you cannot ignore.

---

## Colors

The palette divides into two registers.

**The dashboard** runs on cool blue-whites and forest green. `background` (#F5FAFF) and `surface` (#FFFFFF) read as a health or productivity app — serene, trustworthy. `primary` (#306B3F) is a deep forest green used for brand moments: active nav items, section labels in the hero card, the chart's today bar, and the "granted" permission dots. The green reads "natural" and "growth" — a quiet visual pun on the app's nature-to-doom progression system.

**The overlay pill** is an entirely different palette: near-opaque black (`#E6000000`, 90% opacity) with a `#FF1744` red outer glow and a white inner glow. Both glows pulse on a continuous 1500ms animation. The pill looks like a threat indicator, which is the point — it is a physiological warning light you've opted into.

The only other deliberate chromatic choice is `chart-bar-past` (#B0C9D6): a desaturated blue-grey for the six preceding days. Calm and forgettable next to the green today bar — yesterday's doom doesn't need your attention.

---

## Typography

The typeface system is sparse: system fonts only, no custom families loaded.

**Hero numbers** (the daily time or distance) are rendered in `monospace` at 56sp bold. Monospace is non-negotiable here — it gives the timer a clock-like precision and prevents the layout from shifting as digits change. Numbers are the primary content; they deserve the weight.

**The overlay pill** also uses monospace at 13sp. It should feel like a readout from an instrument panel: terse, technical, slightly inhuman.

**Section labels** are 11sp bold uppercase with 0.08em letter spacing. Used throughout to introduce groups of cards — permissions, pill display mode, tracked apps, last 7 days, achievement history. They are always `on-surface-variant` (#5A7A8A), intentionally low-contrast to avoid competing with card content.

**Card content** follows a two-level hierarchy: bold 18sp for level titles and fact headlines, regular 13–15sp for taglines and supporting text. Achievement rows use 14sp bold title + 12sp muted tagline — a well-worn two-line list item pattern, kept tight.

---

## The Overlay Pill

The pill is the product's primary surface. It appears above every other app while scrolling and is the only thing most users will see during normal use. It must earn its presence.

**Structure:** A `GlowPillView` (`FrameLayout`) wraps a single `TextView`. The wrapper carries a software-rendered layer that draws two `BlurMaskFilter` rounded rects beneath the pill: an outer glow (36dp blur, `#FF1744`) and an inner glow (16dp blur, white). The pill itself is a `GradientDrawable` with 24dp corner radius and 90% opaque black fill. The text inside has 14dp horizontal and 5dp vertical padding.

**Animation:** A `ValueAnimator` oscillates glow alpha between 0.45 and 1.0 over 1500ms on a reverse-infinite cycle. Both glows follow the same alpha curve, scaled to their respective peak values (0.85 for outer, 0.70 for inner). The result is a slow, biological pulse — more heartbeat than strobe.

**Behavior:** The pill appears within ~100ms of a scroll event. After 1500ms of inactivity it disappears cleanly — no summary, no parting message. When a new achievement level unlocks, the pill expands to two lines (emoji + title, tagline in quotes), holds for 4000ms, reverts to the time/distance readout, then hides after another 1500ms. Touch events pass through (`FLAG_NOT_TOUCHABLE`) — the pill never interrupts the user's interaction with the underlying app.

---

## Layout & Spacing

All screens use a single-column `ScrollView` with 20dp horizontal padding and 32dp top padding. Cards stack vertically with 10–16dp gaps. The bottom navigation bar is 80dp tall; the fragment container accounts for this with `paddingBottom="80dp"`.

**Hero cards** (green-tinted, 20dp radius, 0dp elevation, 20dp padding) always appear at the top of a tab and hold the primary metric — today's time or distance. They create an immediate anchor: you know what you're looking at the moment the tab loads.

**Content cards** (white, 16dp radius, 1dp elevation, 20dp padding) hold secondary information: level cards, fact cards, the 7-day chart, achievement history. The 1dp elevation is barely perceptible — it separates cards from the background without adding visual weight.

**Section labels** float between card groups: 11sp bold uppercase, always `on-surface-variant`. They introduce context without demanding attention.

**List rows** (permission items, app toggles) are 56dp tall — a standard Android touch target — with 16dp horizontal padding. Dividers are 1dp `surface-variant` lines, not full-bleed: they sit within the card's horizontal padding.

---

## Motion & Animation

Animations are intentionally minimal. The only active animation is the pill's glow pulse; everything else is instantaneous state changes managed by `Handler.postDelayed`.

The glow pulse runs continuously while the pill is on screen. It is not triggered by user interaction — it just breathes. The effect should feel ambient and slightly unsettling, not celebratory.

Achievement expansion is the only other animated moment, and even it is not a true animation — the pill text changes immediately and the view wraps to its new height via `WRAP_CONTENT`. The drama comes from content change, not from motion.

---

## Achievement & Level System

Two parallel progressions use the same visual language:

**Daily levels** (8 tiers, 🌱→💀, 0–4h+) reset each midnight. They track how deep today's session has gone. The emoji sequence tells a story: a seed becoming a doomed skull. Labels escalate from warm ("Fresh Start") to resigned ("Today's MVP").

**Lifetime levels** (8 tiers, 🥚→🏆, 0–1yr cumulative) never reset. They mark cumulative drift. The emoji sequence tells a different story: an egg hatching, eventually becoming a legend.

When a new tier is reached, the pill expands to show the level emoji, title, and a dry tagline in quotation marks. No notification, no confetti — just the pill, suddenly bigger, with something to say.

---

## Bottom Navigation

The bottom nav is styled to disappear: white background, 0dp elevation, no shadow, no divider line. Active items use `primary` green for icon and label tint; inactive items are muted `on-surface-variant`. The three tabs are labeled (Time, Distance, Settings) with Material Icons (clock, ruler, gear). The nav becomes invisible against the white `surface` of the card stack — the content is the focus, not the chrome.
