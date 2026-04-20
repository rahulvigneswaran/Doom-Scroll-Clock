package com.doomscrollclock

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.doomscrollclock.databinding.FragmentTimeBinding
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

class TimeFragment : Fragment() {

    private var _binding: FragmentTimeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTimeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        updateStats()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateStats() {
        val totalSecs = TimerManager.getTotalSeconds()
        binding.heroTime.text = TimerManager.formatSeconds(totalSecs)

        val dailyLevel = FunFacts.getDailyLevel(totalSecs)
        binding.dailyLevelEmoji.text = dailyLevel.emoji
        binding.dailyLevelTitle.text = dailyLevel.title
        binding.dailyLevelTagline.text = "\"${dailyLevel.tagline}\""

        val lifetimeLevel = FunFacts.getLifetimeLevel(TimerManager.getLifetimeSeconds())
        binding.lifetimeLevelEmoji.text = lifetimeLevel.emoji
        binding.lifetimeLevelTitle.text = lifetimeLevel.title
        binding.lifetimeLevelTagline.text = "\"${lifetimeLevel.tagline}\""

        val timeFact = FunFacts.getTimeFact(totalSecs)
        if (timeFact != null) {
            binding.timeFactEmoji.text = timeFact.emoji
            binding.timeFactText.text = timeFact.text
            binding.cardTimeFact.visibility = View.VISIBLE
        } else {
            binding.cardTimeFact.visibility = View.GONE
        }

        populateHistory()
    }

    private fun populateHistory() {
        val history = TimerManager.getHistory()
        val maxSecs = history.maxOfOrNull { it.seconds }?.takeIf { it > 0 } ?: 1L
        val container = binding.historyContainer
        container.removeAllViews()
        val today = LocalDate.now().toString()
        val inflater = LayoutInflater.from(requireContext())

        history.forEach { day ->
            val row = inflater.inflate(R.layout.item_history_row, container, false)
            val label = row.findViewById<TextView>(R.id.row_label)
            val bar = row.findViewById<View>(R.id.row_bar)
            val timeView = row.findViewById<TextView>(R.id.row_time)

            label.text = if (day.date == today) "Today" else {
                LocalDate.parse(day.date).dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
            }

            val frac = day.seconds.toFloat() / maxSecs
            val params = bar.layoutParams as LinearLayout.LayoutParams
            params.weight = frac
            bar.layoutParams = params
            bar.setBackgroundColor(
                if (day.date == today) ContextCompat.getColor(requireContext(), R.color.colorBarToday)
                else ContextCompat.getColor(requireContext(), R.color.colorBarPast)
            )

            timeView.text = if (day.seconds > 0) TimerManager.formatSeconds(day.seconds) else "—"
            container.addView(row)
        }
    }
}
