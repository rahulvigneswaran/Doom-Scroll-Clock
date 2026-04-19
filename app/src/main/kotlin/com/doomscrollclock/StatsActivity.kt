package com.doomscrollclock

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.doomscrollclock.databinding.ActivityStatsBinding
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

class StatsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStatsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnBack.setOnClickListener { finish() }
    }

    override fun onResume() {
        super.onResume()
        updateStats()
    }

    private fun updateStats() {
        val totalSecs = TimerManager.getTotalSeconds()
        val scrollEvents = TimerManager.getScrollEvents()
        val lifetimeSecs = TimerManager.getLifetimeSeconds()

        binding.heroTime.text = TimerManager.formatSeconds(totalSecs)

        val dailyLevel = FunFacts.getDailyLevel(totalSecs)
        binding.dailyLevelEmoji.text = dailyLevel.emoji
        binding.dailyLevelTitle.text = dailyLevel.title
        binding.dailyLevelTagline.text = "\"${dailyLevel.tagline}\""

        val lifetimeLevel = FunFacts.getLifetimeLevel(lifetimeSecs)
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

        val distanceFact = FunFacts.getDistanceFact(scrollEvents)
        if (distanceFact != null) {
            binding.distanceFactEmoji.text = distanceFact.emoji
            binding.distanceFactText.text = distanceFact.text
            val metres = scrollEvents * 0.02
            binding.distanceFactSub.text = if (metres < 1000) {
                "~${metres.toInt()}m of content"
            } else {
                "~${"%.1f".format(metres / 1000)}km of content"
            }
            binding.cardDistanceFact.visibility = View.VISIBLE
        } else {
            binding.cardDistanceFact.visibility = View.GONE
        }

        populateHistory()
    }

    private fun populateHistory() {
        val history = TimerManager.getHistory()
        val maxSecs = history.maxOfOrNull { it.seconds }?.takeIf { it > 0 } ?: 1L
        val container = binding.historyContainer
        container.removeAllViews()
        val today = LocalDate.now().toString()
        val inflater = LayoutInflater.from(this)

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
                if (day.date == today) 0xFF7B61FF.toInt() else 0xFF555555.toInt()
            )

            timeView.text = if (day.seconds > 0) TimerManager.formatSeconds(day.seconds) else "—"
            container.addView(row)
        }
    }
}
