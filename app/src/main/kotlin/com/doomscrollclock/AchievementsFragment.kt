package com.doomscrollclock

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.doomscrollclock.databinding.FragmentAchievementsBinding
import com.doomscrollclock.databinding.ItemAchievementEntryBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class AchievementsFragment : Fragment() {

    private var _binding: FragmentAchievementsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAchievementsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loadData() {
        val achievements = TimerManager.getAchievementHistory()
        val totalSecs = TimerManager.getTotalSeconds()
        val metres = TimerManager.getScrollMetres()
        val currentLevel = FunFacts.getDailyLevel(totalSecs)
        val currentLevelIdx = FunFacts.dailyLevels.indexOf(currentLevel)
        val roastIntensity = TimerManager.getRoastIntensity()

        setupHeroCard(currentLevel, currentLevelIdx, totalSecs, metres, roastIntensity)
        setupQuickStats(achievements, currentLevelIdx)
        populateAchievementList(achievements)
    }

    private fun setupHeroCard(
        level: FunFacts.Level,
        levelIdx: Int,
        totalSecs: Long,
        metres: Double,
        roastIntensity: Int
    ) {
        // Gradient background: lavender → pink
        val gradient = GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            intArrayOf(Color.parseColor("#AEAFF7"), Color.parseColor("#EF5DA8"))
        ).apply {
            cornerRadius = 24 * resources.displayMetrics.density
        }
        binding.cardHeroAchievement.background = gradient

        binding.tvLevelNumberBadge.text = "${levelIdx + 1}"
        binding.tvHeroLevelName.text = "${level.emoji} ${level.title}"
        binding.tvHeroEarnedDate.text = "Reached on ${LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy"))}"

        val mins = totalSecs / 60
        binding.chipTime.text = if (mins >= 60) "⏱ ${mins / 60}h ${mins % 60}m" else "⏱ ${mins}m"
        binding.chipDistance.text = "↕ ${FunFacts.formatDistance(metres)}"

        binding.tvHeroRoast.text = "\"${FunFacts.getRoastQuote(level, roastIntensity)}\""

        binding.btnShare.setOnClickListener {
            val quote = FunFacts.getRoastQuote(level, roastIntensity)
            val shareText = getString(R.string.share_text, level.title, (totalSecs / 60).toInt(), quote)
            startActivity(Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
            }.let { Intent.createChooser(it, null) })
        }
    }

    private fun setupQuickStats(achievements: List<TimerManager.Achievement>, currentLevelIdx: Int) {
        binding.tvStatLevels.text = achievements.size.toString()

        val bestLevelTitle = achievements.maxByOrNull {
            FunFacts.dailyLevels.indexOfFirst { l -> l.title == it.title }
        }?.title ?: FunFacts.dailyLevels[currentLevelIdx].emoji
        binding.tvStatBest.text = bestLevelTitle.take(6)

        val weekAgo = LocalDate.now().minusDays(7).toString()
        val thisWeekCount = achievements.count { it.date >= weekAgo }
        binding.tvStatThisWeek.text = thisWeekCount.toString()
    }

    private fun populateAchievementList(achievements: List<TimerManager.Achievement>) {
        val container = binding.containerAchievements
        container.removeAllViews()

        if (achievements.isEmpty()) {
            val empty = TextView(requireContext()).apply {
                text = "No achievements yet — keep scrolling (or don't)."
                textSize = 13f
                setTextColor(Color.parseColor("#9590A0"))
                setPadding(0, 8, 0, 8)
            }
            container.addView(empty)
            return
        }

        achievements.forEachIndexed { index, achievement ->
            val entryBinding = ItemAchievementEntryBinding.inflate(
                layoutInflater, container, false
            )

            val levelIdx = FunFacts.dailyLevels.indexOfFirst { it.title == achievement.title }
                .takeIf { it >= 0 } ?: 0
            val levelColor = try {
                Color.parseColor(FunFacts.dailyLevels.getOrNull(levelIdx)?.color ?: "#AEAFF7")
            } catch (_: Exception) { Color.parseColor("#AEAFF7") }

            entryBinding.tvEntryLevelNumber.text = "${achievement.emoji}"
            entryBinding.tvEntryLevelNumber.setTextColor(levelColor)

            // Tint the circle background
            (entryBinding.tvEntryLevelNumber.background as? GradientDrawable)?.apply {
                setColor(Color.argb(26, Color.red(levelColor), Color.green(levelColor), Color.blue(levelColor)))
                setStroke(
                    (2 * resources.displayMetrics.density).toInt(),
                    Color.argb(140, Color.red(levelColor), Color.green(levelColor), Color.blue(levelColor))
                )
            }

            entryBinding.tvEntryLevelName.text = achievement.title
            entryBinding.tvEntryLevelName.setTextColor(Color.parseColor("#2D1B2E"))

            val dateFmt = try {
                LocalDate.parse(achievement.date)
                    .format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
            } catch (_: Exception) { achievement.date }
            entryBinding.tvEntryMeta.text = "${achievement.type.replaceFirstChar { it.uppercase() }} · $dateFmt"

            entryBinding.tvEntryTagline.text = "\"${achievement.tagline}\""

            if (index == 0) {
                entryBinding.tvEntryLatestBadge.visibility = View.VISIBLE
                entryBinding.tvEntryLatestBadge.setTextColor(levelColor)
            }

            container.addView(entryBinding.root)
        }
    }
}
