package com.doomscrollclock

object FunFacts {

    data class Level(val emoji: String, val title: String, val tagline: String, val minSeconds: Long)
    data class TimeFact(val emoji: String, val text: String, val minSeconds: Long)
    data class DistanceFact(val emoji: String, val text: String, val minMetres: Double)

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

    // Two facts per tier so the rotation shows variety each session
    val timeFacts = listOf(
        TimeFact("⚡", "Usain Bolt's 100m world record (9.58s)", 9),
        TimeFact("🤧", "The average human sneeze. Bless you.", 9),
        TimeFact("🎵", "The Beatles' 'Her Majesty' — their shortest song ever", 23),
        TimeFact("🐦", "How long a hummingbird can hover before giving up and moving on", 23),
        TimeFact("☕", "The world's most anticlimactic 'instant' coffee", 120),
        TimeFact("🦷", "Brushing your teeth, twice, like the dentist asked", 120),
        TimeFact("☕", "A proper pour-over coffee, brewed with actual intention", 300),
        TimeFact("🎬", "The average time spent deciding what to watch on Netflix", 300),
        TimeFact("☀️", "Sunlight traveling 150 million km from the Sun to Earth", 480),
        TimeFact("🚇", "The average London Underground journey between stops", 480),
        TimeFact("🥚", "A perfect hard-boiled egg, timed to the second", 600),
        TimeFact("📱", "How long before boredom triggers another phone check, statistically", 600),
        TimeFact("🍕", "A Neapolitan pizza firing in a wood-fired oven", 900),
        TimeFact("🚿", "The average human shower — water you'll never get back", 900),
        TimeFact("🚶", "The average person walking a mile at a comfortable pace", 1260),
        TimeFact("🛰️", "The ISS crossing an entire continent overhead", 1260),
        TimeFact("📺", "One episode of The Office (Dunder Mifflin approved)", 1320),
        TimeFact("🎸", "Bohemian Rhapsody by Queen — played twice, back to back", 1320),
        TimeFact("🌑", "Dark Side of the Moon by Pink Floyd, front to back", 2580),
        TimeFact("🐢", "How long it takes a sloth to travel 100 metres. No rush.", 2580),
        TimeFact("🧘", "A yoga class you keep meaning to actually attend", 2700),
        TimeFact("📧", "The average gap between 'quick email checks' for remote workers", 2700),
        TimeFact("🛸", "The ISS completing one full orbit of planet Earth", 5400),
        TimeFact("🎞️", "The average movie length, not counting trailers you sat through anyway", 5400),
        TimeFact("🎬", "Watching Titanic — give or take 14 minutes", 7200),
        TimeFact("🧠", "Two full episodes of a prestige drama you keep meaning to watch", 7200),
        TimeFact("🌌", "Watching Interstellar — time dilation not included", 10020),
        TimeFact("💤", "Four complete REM sleep cycles. You could have slept.", 10020),
        TimeFact("⚔️", "Lord of the Rings: Fellowship of the Ring, Extended Cut", 13680),
        TimeFact("📺", "Every episode of a limited series you swore you'd only watch one of", 13680),
        TimeFact("✈️", "A nonstop flight from London to New York", 18000),
        TimeFact("🦥", "The time it takes a sloth to fully digest a single leaf", 18000),
        TimeFact("💼", "A full 8-hour workday — productivity mode: scroll", 28800),
        TimeFact("😴", "The amount of sleep doctors say you need. But here we are.", 28800),
        TimeFact("🌍", "One full rotation of planet Earth on its axis", 86400),
        TimeFact("⏳", "The exact amount of time you had today. All of it.", 86400),
    )

    // Two facts per distance tier — minMetres in physical metres
    val distanceFacts = listOf(
        DistanceFact("📏", "roughly your own height, scrolled upright", 0.5),
        DistanceFact("🧍", "one standing human — feet to crown, in content", 0.5),
        DistanceFact("🏀", "taller than an NBA player. You're in the zone.", 2.13),
        DistanceFact("🚪", "four standard doors stacked floor to ceiling", 2.13),
        DistanceFact("🦒", "as tall as a giraffe — the world's tallest land animal", 5.5),
        DistanceFact("🏗️", "a two-storey building. You've scrolled to the roof.", 5.5),
        DistanceFact("🌊", "the height of Niagara Falls, top to mist", 57.0),
        DistanceFact("🏛️", "the Leaning Tower of Pisa. Still standing, unlike your screen time.", 57.0),
        DistanceFact("🗽", "the Statue of Liberty, pedestal to torch", 93.0),
        DistanceFact("🏙️", "25 floors of a typical office building", 93.0),
        DistanceFact("🗼", "the full height of the Eiffel Tower", 330.0),
        DistanceFact("📡", "Tokyo Tower — France's more neon-lit cousin", 333.0),
        DistanceFact("🏙️", "One World Trade Center — tallest in the Western Hemisphere", 541.0),
        DistanceFact("🍁", "the CN Tower, where Canada keeps its ambitions", 553.0),
        DistanceFact("🌆", "the Burj Khalifa — world's tallest building", 828.0),
        DistanceFact("✈️", "high enough to see the curvature of the Earth… almost", 828.0),
        DistanceFact("🏔️", "the summit of Mount Everest above sea level", 8849.0),
        DistanceFact("🛫", "higher than commercial aircraft cruise altitude", 8849.0),
        DistanceFact("🏃", "a full marathon — 42.2km, one scroll at a time", 42195.0),
        DistanceFact("🌍", "the width of Switzerland, scrolled sideways", 42195.0),
        DistanceFact("🚀", "past the Kármán line — you're technically in space", 100000.0),
        DistanceFact("🌙", "high enough to see the entire UK as a blurry blob below", 100000.0),
        DistanceFact("🛸", "the altitude of the International Space Station", 408000.0),
        DistanceFact("🌐", "far enough that the atmosphere is just a polite suggestion", 408000.0),
    )

    fun getDailyLevel(seconds: Long): Level =
        dailyLevels.lastOrNull { seconds >= it.minSeconds } ?: dailyLevels.first()

    fun getLifetimeLevel(totalSeconds: Long): Level =
        lifetimeLevels.lastOrNull { totalSeconds >= it.minSeconds } ?: lifetimeLevels.first()

    fun getTimeFact(seconds: Long): TimeFact? {
        val eligible = timeFacts.filter { seconds >= it.minSeconds }
        if (eligible.isEmpty()) return null
        val top = eligible.maxOf { it.minSeconds }
        val tier = eligible.filter { it.minSeconds == top }
        // Rotate every 2 minutes so returning users see a different fact
        return tier[((seconds / 120) % tier.size).toInt()]
    }

    fun getDistanceFact(metres: Double): DistanceFact? {
        if (metres <= 0.0) return null
        val eligible = distanceFacts.filter { metres >= it.minMetres }
        if (eligible.isEmpty()) return null
        val top = eligible.maxOf { it.minMetres }
        val tier = eligible.filter { it.minMetres == top }
        return tier[(metres.toLong() % tier.size).toInt()]
    }

    fun formatDistance(metres: Double): String =
        if (metres < 1000) "~${metres.toInt()}m" else "~${"%.1f".format(metres / 1000)}km"
}
