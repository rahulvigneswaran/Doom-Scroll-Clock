package com.doomscrollclock

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.doomscrollclock.databinding.FragmentHistoryBinding
import com.doomscrollclock.databinding.ItemHistoryEntryBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private var isTimeMode = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toggleMode.check(R.id.btn_mode_time)
        binding.toggleMode.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                isTimeMode = checkedId == R.id.btn_mode_time
                populateList()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        populateList()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun populateList() {
        val container = binding.containerHistory
        container.removeAllViews()

        val history = TimerManager.getHistory().reversed()
        val achievements = TimerManager.getAchievementHistory()
        val roastIntensity = TimerManager.getRoastIntensity()

        if (history.all { it.seconds == 0L }) {
            val empty = TextView(requireContext()).apply {
                text = "No history yet. Start scrolling to fill this in."
                textSize = 13f
                setTextColor(Color.parseColor("#9590A0"))
                setPadding(0, 16, 0, 16)
            }
            container.addView(empty)
            return
        }

        history.forEachIndexed { index, day ->
            if (day.seconds == 0L) return@forEachIndexed
            val entryBinding = ItemHistoryEntryBinding.inflate(layoutInflater, container, false)

            val level = FunFacts.getDailyLevel(day.seconds)
            val levelColor = try { Color.parseColor(level.color) } catch (_: Exception) { Color.parseColor("#AEAFF7") }

            // Timeline dot colour
            entryBinding.timelineDot.backgroundTintList =
                android.content.res.ColorStateList.valueOf(levelColor)

            // Hide connector line on last visible item
            if (index == history.indexOfLast { it.seconds > 0L }) {
                entryBinding.timelineLine.visibility = View.INVISIBLE
            }

            val dateFmt = try {
                LocalDate.parse(day.date).format(DateTimeFormatter.ofPattern("EEE, MMM d")).uppercase()
            } catch (_: Exception) { day.date }
            entryBinding.tvEntryDate.text = dateFmt

            entryBinding.tvEntryLevelBadge.text = "${level.emoji} ${level.title}"
            try {
                entryBinding.tvEntryLevelBadge.backgroundTintList =
                    android.content.res.ColorStateList.valueOf(levelColor)
            } catch (_: Exception) {}

            if (isTimeMode) {
                entryBinding.tvEntryValue.text = TimerManager.formatSeconds(day.seconds)
            } else {
                // Metres not stored per-day historically — show scroll events as proxy
                entryBinding.tvEntryValue.text = "${day.scrollEvents} scrolls"
            }

            // Roast quote from matching level
            val roastQuote = FunFacts.getRoastQuote(level, roastIntensity)

            // Expandable section
            var expanded = false
            val tapAction = View.OnClickListener {
                expanded = !expanded
                entryBinding.detailSection.visibility = if (expanded) View.VISIBLE else View.GONE
                entryBinding.tvTapHint.text = if (expanded) "Tap to collapse" else "Tap to expand"
                if (expanded) {
                    populateMiniAppBars(entryBinding.containerAppMiniBars, day)
                    entryBinding.tvRoastQuote.text = "\"$roastQuote\""
                }
            }
            entryBinding.root.setOnClickListener(tapAction)

            container.addView(entryBinding.root)
        }
    }

    private fun populateMiniAppBars(container: LinearLayout, day: TimerManager.DayStats) {
        container.removeAllViews()
        // Simplified per-app display using today's data as a proxy for past days
        val appEvents = TimerManager.getAppEvents()
        if (appEvents.isEmpty() || day.date != LocalDate.now().toString()) {
            val note = TextView(requireContext()).apply {
                text = "Per-app breakdown available for today"
                textSize = 11f
                setTextColor(Color.parseColor("#9590A0"))
            }
            container.addView(note)
            return
        }
        val d = resources.displayMetrics.density
        val maxVal = appEvents.values.maxOrNull()?.coerceAtLeast(1L) ?: 1L
        appEvents.entries.sortedByDescending { it.value }.forEach { (key, count) ->
            val row = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
                setPadding(0, (3 * d).toInt(), 0, (3 * d).toInt())
            }
            val label = TextView(requireContext()).apply {
                text = key.replaceFirstChar { it.uppercase() }
                textSize = 11f
                setTextColor(Color.parseColor("#9590A0"))
                layoutParams = LinearLayout.LayoutParams((72 * d).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
            }
            row.addView(label)
            val track = LinearLayout(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(0, (6 * d).toInt(), 1f)
                background = android.graphics.drawable.GradientDrawable().apply {
                    cornerRadius = 3 * d
                    setColor(Color.parseColor("#EAE8F2"))
                }
            }
            val fill = View(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ((count.toFloat() / maxVal) * 200 * d).toInt().coerceAtLeast(1),
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                background = android.graphics.drawable.GradientDrawable().apply {
                    cornerRadius = 3 * d
                    setColor(Color.parseColor("#AEAFF7"))
                }
            }
            track.addView(fill)
            row.addView(track)
            container.addView(row)
        }
    }
}
