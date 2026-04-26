# Design Reference

## Theme
Material3 (`Theme.Material3.DayNight.NoActionBar`). All widget styles must use `Widget.Material3.*` — never `Widget.MaterialComponents.*` (causes aapt2 link errors).

## Color palette

| Token | Hex | Usage |
|-------|-----|-------|
| `colorBackground` | `#F5FAFF` | Screen backgrounds |
| `colorSurface` | `#FFFFFF` | Cards |
| `colorSurfaceVariant` | `#EBF3F9` | Dividers, chart tracks |
| `colorPrimary` | `#306B3F` | Green — brand, active states |
| `colorPrimaryContainer` | `#D4EDDA` | Hero cards |
| `colorOnSurface` | `#1A1C1E` | Primary text |
| `colorOnSurfaceVariant` | `#5C6A72` | Secondary text, labels |
| `colorBarToday` | `#306B3F` | Chart bar — today |
| `colorBarPast` | `#B0C9D6` | Chart bar — past days |
| `colorGranted` | `#306B3F` | Permission dot — granted |
| `colorNotGranted` | `#E53935` | Permission dot — denied |

## Overlay pill
- Background: `#E6000000` (semi-transparent black)
- Text: white, 13sp, Typeface.MONOSPACE
- Outer glow: `#FF1744` red, 36dp blur radius, pulsing 0.45→1.0 alpha
- Inner glow: white, 16dp blur radius
- Position: `Gravity.TOP | Gravity.CENTER_HORIZONTAL`, y=56dp from top
- Achievement/summary mode: multi-line text, pill expands naturally via WRAP_CONTENT

## Section labels
- 11sp, bold, `textAllCaps="true"`, letterSpacing=0.08–0.1, `colorOnSurfaceVariant`

## Cards
- `cardCornerRadius`: 16dp standard, 20dp for hero cards
- `cardElevation`: 1dp for content cards, 0dp for hero/tinted cards
