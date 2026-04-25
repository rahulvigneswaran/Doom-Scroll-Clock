package com.doomscrollclock

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.doomscrollclock.databinding.FragmentDistanceBinding

class DistanceFragment : Fragment() {

    private var _binding: FragmentDistanceBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDistanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        updateDistance()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateDistance() {
        val metres = TimerManager.getScrollMetres()
        val scrollEvents = TimerManager.getScrollEvents()
        binding.heroDistance.text = FunFacts.formatDistance(metres)
        binding.scrollEventsSubtitle.text = "$scrollEvents scroll events today"

        val distanceFact = FunFacts.getDistanceFact(metres)
        if (distanceFact != null) {
            binding.distanceFactEmoji.text = distanceFact.emoji
            binding.distanceFactText.text = distanceFact.text
            binding.distanceFactSub.text = "${FunFacts.formatDistance(metres)} of content"
            binding.cardDistanceFact.visibility = View.VISIBLE
            binding.cardEmpty.visibility = View.GONE
        } else {
            binding.cardDistanceFact.visibility = View.GONE
            binding.cardEmpty.visibility = View.VISIBLE
        }
    }
}
