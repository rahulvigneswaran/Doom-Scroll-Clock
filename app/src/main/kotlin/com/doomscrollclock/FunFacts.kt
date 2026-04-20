package com.doomscrollclock

object FunFacts {

    data class Level(val emoji: String, val title: String, val tagline: String, val minSeconds: Long)
    data class TimeFact(val emoji: String, val text: String, val minSeconds: Long)
    data class DistanceFact(val emoji: String, val text: String, val minEvents: Long)

    val dailyLevels = listOf(
        Level("🌱", "Fresh Start", "The day is young.", 0),
        Level("👀", "Just Checking", "A quick peek. Sure.", 300),
        Level("📱", "Warming Up", "Algorithm: 'Welcome back.'", 900),
        Level("🌀", "Getting Comfy", "Half an hour in. Cozy.", 1800),
        Level("🎯", "Full Commit", "One hour. You meant to stop earlier.", 3600),
        Level("🌊", "Deep In The Feed", "Two hours. Respect.", 7200),
        Level("🔥", "Doom Scroll Pro", "Three hours. This app is working as intended.", 10800),
        Level("💀", "Today's MVP", "Four+ hours. Unmatched dedication.", 14400),
    )

    val lifetimeLevels = listOf(
        Level("🥚", "Freshly Hatched", "You've barely begun.", 0),
        Level("🐣", "Peeping Scroller", "The algorithm smells potential.", 5 * 3600L),
        Level("🐥", "Habitual Visitor", "You keep coming back.", 24 * 3600L),
        Level("🌍", "One Day Gone", "A literal 24-hour day, scrolled away.", 168 * 3600L),
        Level("🌙", "Week of Doom", "Seven full days of your finite life.", 720 * 3600L),
        Level("🗓️", "Monthly Subscriber", "A month of evenings — gone.", 2160 * 3600L),
        Level("🌌", "Scroll Immortal", "100 days. You're part of the internet now.", 8760 * 3600L),
        Level("🏆", "Legend of the Feed", "A full year, scrolled away. Legendary.", 87600 * 3600L),
    )

    val timeFacts = listOf(
        TimeFact("⚡", "Usain Bolt's 100m world record (9.58s)", 9),
        TimeFact("🎵", "The Beatles' 'Her Majesty' — their shortest song ever", 23),
        TimeFact("☕", "The world's most anticlimactic 'instant' coffee", 120),
        TimeFact("☕", "A proper pour-over coffee, done right", 300),
        TimeFact("☀️", "Sunlight traveling from the Sun to Earth", 480),
        TimeFact("🥚", "A perfect hard-boiled egg", 600),
        TimeFact("🍕", "A Neapolitan pizza in a wood-fired oven", 900),
        TimeFact("🚶", "The average person walking a mile", 1260),
        TimeFact("📺", "One episode of The Office (Dunder Mifflin approved)", 1320),
        TimeFact("🌑", "Dark Side of the Moon by Pink Floyd, front to back", 2580),
        TimeFact("🧘", "A yoga class you keep meaning to actually attend", 2700),
        TimeFact("🛸", "The ISS completing one full orbit of Earth", 5400),
        TimeFact("🎬", "Watching Titanic (give or take 14 minutes)", 7200),
        TimeFact("🌌", "Watching Interstellar — time dilation not included", 10020),
        TimeFact("⚔️", "Lord of the Rings: Fellowship Extended Cut", 13680),
        TimeFact("✈️", "A nonstop flight from London to New York", 18000),
        TimeFact("💼", "A full 8-hour workday — productivity mode: scrolling", 28800),
        TimeFact("🌍", "One full rotation of planet Earth", 86400),
    )

    const val METRES_PER_EVENT = 0.05 // 5cm per TYPE_VIEW_SCROLLED event

    val distanceFacts = listOf(
        DistanceFact("🧍", "Taller than an NBA player", 44),
        DistanceFact("🚌", "A London double-decker bus", 88),
        DistanceFact("🗽", "The Statue of Liberty (pedestal to torch)", 1860),
        DistanceFact("🗼", "Past the Eiffel Tower", 6600),
        DistanceFact("🏙️", "One World Trade Center", 10840),
        DistanceFact("🌆", "Over the Burj Khalifa — world's tallest building", 16560),
        DistanceFact("🏔️", "The summit of Mount Everest", 177000),
        DistanceFact("🏃", "A full marathon, one post at a time", 844000),
        DistanceFact("🚀", "Into space (the Kármán line)", 2000000),
        DistanceFact("🛸", "To the altitude of the ISS", 8000000),
    )

    fun getDailyLevel(seconds: Long): Level =
        dailyLevels.lastOrNull { seconds >= it.minSeconds } ?: dailyLevels.first()

    fun getLifetimeLevel(totalSeconds: Long): Level =
        lifetimeLevels.lastOrNull { totalSeconds >= it.minSeconds } ?: lifetimeLevels.first()

    fun getTimeFact(seconds: Long): TimeFact? =
        timeFacts.lastOrNull { seconds >= it.minSeconds }

    fun getDistanceFact(scrollEvents: Long): DistanceFact? =
        distanceFacts.lastOrNull { scrollEvents >= it.minEvents }

    fun formatDistance(scrollEvents: Long): String {
        val metres = scrollEvents * METRES_PER_EVENT
        return if (metres < 1000) "~${metres.toInt()}m" else "~${"%.1f".format(metres / 1000)}km"
    }
}
