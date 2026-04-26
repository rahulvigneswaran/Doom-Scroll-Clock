package com.doomscrollclock

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.doomscrollclock.databinding.FragmentSetupBinding

class SetupFragment : Fragment() {

    private var _binding: FragmentSetupBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSetupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnOverlay.setOnClickListener {
            startActivity(
                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${requireContext().packageName}"))
            )
        }

        binding.btnAccessibility.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        binding.btnContinue.setOnClickListener {
            (activity as? MainActivity)?.onSetupComplete()
        }
    }

    override fun onResume() {
        super.onResume()
        refreshPermissionState()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun refreshPermissionState() {
        val overlayGranted = Settings.canDrawOverlays(requireContext())
        val accessibilityGranted = isAccessibilityEnabled()

        updateDot(binding.dotOverlay, overlayGranted)
        updateDot(binding.dotAccessibility, accessibilityGranted)

        binding.btnOverlay.isEnabled = !overlayGranted
        binding.btnAccessibility.isEnabled = !accessibilityGranted

        val bothGranted = overlayGranted && accessibilityGranted
        binding.btnContinue.visibility = if (bothGranted) View.VISIBLE else View.GONE
        binding.tvStatus.text = when {
            bothGranted -> "You\'re all set. Tap to continue."
            !overlayGranted && !accessibilityGranted -> "Grant both permissions to continue"
            !overlayGranted -> "One more — allow overlay display"
            else -> "One more — enable the accessibility service"
        }

        if (bothGranted) {
            (activity as? MainActivity)?.onSetupComplete()
        }
    }

    private fun updateDot(dot: View, granted: Boolean) {
        dot.backgroundTintList = ContextCompat.getColorStateList(
            requireContext(),
            if (granted) R.color.colorGranted else R.color.colorNotGranted
        )
    }

    private fun isAccessibilityEnabled(): Boolean {
        val am = requireContext().getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        return am.getEnabledAccessibilityServiceList(
            android.accessibilityservice.AccessibilityServiceInfo.FEEDBACK_ALL_MASK
        ).any { it.resolveInfo.serviceInfo.packageName == requireContext().packageName }
    }
}
