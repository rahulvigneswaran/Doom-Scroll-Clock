package com.doomscrollclock

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        val today = LocalDate.now().toString()
        val bars = history.map { day ->
            val label = if (day.date == today) "Today"
            else LocalDate.parse(day.date).dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
            HistoryChartView.Bar(label, day.seconds, day.date == today)
        }
        binding.historyChart.setData(bars)
    }
}
