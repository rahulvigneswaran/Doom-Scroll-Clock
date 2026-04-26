package com.doomscrollclock

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.doomscrollclock.databinding.FragmentStatsBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class StatsFragment : Fragment() {

    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!

    private val handler = Handler(Looper.getMainLooper())
    private var factRotateIndex = 0
    private val factRotateRunnable = object : Runnable {
        override fun run() {
            rotateFact()
            handler.postDelayed(this, 3400)
        }
    }

    private var isTimeMode = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toggleMode.check(R.id.btn_mode_time)
        binding.toggleMode.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                isTimeMode = checkedId == R.id.btn_mode_time
                updateDisplay()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadData()
        handler.post(factRotateRunnable)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(factRotateRunnable)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loadData() {
        binding.tvDate.text = LocalDate.now()
            .format(DateTimeFormatter.ofPattern("EEE, MMM d")).uppercase()

        updateDisplay()
        updateWeeklyChart()
        updatePerAppBars()
        updateFactDots()
        rotateFact()
    }

    private fun updateDisplay() {
        val totalSecs = TimerManager.getTotalSeconds()
        val metres = TimerManager.getScrollMetres()
        val history = TimerManager.getHistory()
        val weeklyMax = history.maxOfOrNull { it.seconds }?.coerceAtLeast(1L) ?: 1L
        val level = FunFacts.getDailyLevel(totalSecs)
        val levelProgress = FunFacts.getDailyLevelProgress(totalSecs)
        val minsToNext = FunFacts.getMinutesToNextLevel(totalSecs)

        // Level badge
        binding.tvLevelBadge.text = "${level.emoji} ${level.title}"
        try {
            binding.tvLevelBadge.backgroundTintList =
                android.content.res.ColorStateList.valueOf(Color.parseColor(level.color))
        } catch (_: Exception) {}

        // Dual ring
        binding.dualRing.setData(
            todaySeconds = totalSecs,
            weeklyMaxSeconds = weeklyMax,
            levelProgress = levelProgress,
            mode = if (isTimeMode) "time" else "distance"
        )

        // Level progress bar
        binding.progressLevel.progress = (levelProgress * 100).toInt()
        if (minsToNext > 0) {
            binding.tvMinToNext.text = getString(R.string.min_to_next_level, minsToNext)
            binding.tvMinToNext.visibility = View.VISIBLE
        } else {
            binding.tvMinToNext.visibility = View.GONE
        }

        // Mini stat pills
        binding.tvScrollEvents.text = TimerManager.getScrollEvents().toString()
        if (isTimeMode) {
            binding.tvHeroSecondary.text = FunFacts.formatDistance(metres)
            binding.tvHeroSecondaryLabel.text = "distance"
        } else {
            val mins = totalSecs / 60
            binding.tvHeroSecondary.text = if (mins >= 60) "${mins / 60}h ${mins % 60}m" else "${mins}m"
            binding.tvHeroSecondaryLabel.text = "time"
        }

        // Weekly total label
        val weekTotal = history.sumOf { it.seconds }
        binding.tvWeeklyTotal.text = TimerManager.formatSeconds(weekTotal)
    }

    private fun updateWeeklyChart() {
        val history = TimerManager.getHistory()
        val bars = history.map { day ->
            val label = try {
                LocalDate.parse(day.date).format(DateTimeFormatter.ofPattern("EEE")).take(2)
            } catch (_: Exception) { "?" }
            HistoryChartView.Bar(label, day.seconds, day.date == LocalDate.now().toString())
        }
        binding.historyChart.setData(bars)
    }

    private fun updatePerAppBars() {
        val container = binding.containerAppBars
        container.removeAllViews()
        val appEvents = TimerManager.getAppEvents()
        val appMetres = TimerManager.getAppMetres()

        val appNames = mapOf(
            "instagram" to "Instagram",
            "youtube" to "YouTube",
            "reddit" to "Reddit",
            "snapchat" to "Snapchat",
            "tiktok" to "TikTok",
            "twitter" to "Twitter"
        )
        val appColors = mapOf(
            "instagram" to "#EF5DA8",
            "youtube" to "#FF6B6B",
            "reddit" to "#F09E54",
            "snapchat" to "#A0E3E2",
            "tiktok" to "#AEAFF7",
            "twitter" to "#AEAFF7"
        )

        // Build a unified Long map for display regardless of mode
        val data: List<Pair<String, Long>> = if (isTimeMode) {
            appEvents.entries.sortedByDescending { it.value }.map { it.key to it.value }
        } else {
            appMetres.entries.sortedByDescending { it.value }.map { it.key to it.value.toLong() }
        }

        if (data.isEmpty()) {
            val empty = TextView(requireContext())
            empty.text = "No data yet — start scrolling!"
            empty.textSize = 13f
            empty.setTextColor(Color.parseColor("#9590A0"))
            container.addView(empty)
            return
        }

        val maxVal = data.maxOfOrNull { it.second }?.coerceAtLeast(1L) ?: 1L
        val d = resources.displayMetrics.density

        data.forEach { (key, value) ->
            val name = appNames[key] ?: key
            val color = try { Color.parseColor(appColors[key] ?: "#AEAFF7") } catch (_: Exception) { Color.parseColor("#AEAFF7") }

            val row = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, (6 * d).toInt(), 0, (6 * d).toInt())
            }

            // Colour chip
            val chip = View(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams((10 * d).toInt(), (10 * d).toInt()).apply {
                    marginEnd = (10 * d).toInt()
                }
                background = android.graphics.drawable.GradientDrawable().apply {
                    shape = android.graphics.drawable.GradientDrawable.OVAL
                    setColor(color)
                }
            }
            row.addView(chip)

            // Name
            val tvName = TextView(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams((90 * d).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
                text = name
                textSize = 13f
                setTextColor(Color.parseColor("#2D1B2E"))
            }
            row.addView(tvName)

            // ProgressBar (handles fraction layout without needing measured width)
            val bar = ProgressBar(requireContext(), null, android.R.attr.progressBarStyleHorizontal).apply {
                layoutParams = LinearLayout.LayoutParams(0, (8 * d).toInt(), 1f).apply {
                    marginEnd = (10 * d).toInt()
                }
                max = 100
                progress = ((value.toFloat() / maxVal) * 100).toInt()
                progressTintList = android.content.res.ColorStateList.valueOf(color)
                progressBackgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#EAE8F2"))
            }
            row.addView(bar)

            // Value label
            val tvValue = TextView(requireContext()).apply {
                text = if (isTimeMode) "$value" else FunFacts.formatDistance(value.toDouble())
                textSize = 12f
                setTextColor(Color.parseColor("#9590A0"))
            }
            row.addView(tvValue)

            container.addView(row)
        }
    }

    private fun updateFactDots() {
        val dotsContainer = binding.factDots
        dotsContainer.removeAllViews()
        val d = resources.displayMetrics.density
        repeat(4) { i ->
            val dot = View(requireContext()).apply {
                val size = (6 * d).toInt()
                layoutParams = LinearLayout.LayoutParams(size, size).apply {
                    marginStart = (3 * d).toInt()
                }
                background = android.graphics.drawable.GradientDrawable().apply {
                    shape = android.graphics.drawable.GradientDrawable.OVAL
                    setColor(if (i == factRotateIndex % 4) Color.WHITE else Color.argb(77, 255, 255, 255))
                }
            }
            dotsContainer.addView(dot)
        }
    }

    private fun rotateFact() {
        val totalSecs = TimerManager.getTotalSeconds()
        val metres = TimerManager.getScrollMetres()
        val fact = if (isTimeMode) {
            FunFacts.getTimeFact(totalSecs)?.let { Pair(it.emoji, it.text) }
        } else {
            FunFacts.getDistanceFact(metres)?.let { Pair(it.emoji, it.text) }
        }
        if (fact != null) {
            binding.tvFactEmoji.text = fact.first
            binding.tvFactTitle.text = fact.second
            binding.tvFactSubtitle.text = getString(R.string.time_fact_label)
        }
        factRotateIndex++
        updateFactDots()
    }
}
