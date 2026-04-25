package com.doomscrollclock

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.doomscrollclock.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val notifPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { updatePermissionUI() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            binding.cardNotification.visibility = View.VISIBLE
            binding.btnNotification.setOnClickListener {
                notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        val prefs = requireContext().getSharedPreferences("doom_scroll_prefs", Context.MODE_PRIVATE)
        val currentMode = prefs.getString("pill_display_mode", "time") ?: "time"
        binding.togglePillMode.check(if (currentMode == "time") R.id.btn_mode_time else R.id.btn_mode_distance)

        binding.togglePillMode.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                val mode = if (checkedId == R.id.btn_mode_time) "time" else "distance"
                prefs.edit().putString("pill_display_mode", mode).apply()
            }
        }

        setupAppToggles(prefs)
    }

    private fun setupAppToggles(prefs: android.content.SharedPreferences) {
        val disabled = prefs.getStringSet("disabled_apps", emptySet()) ?: emptySet()

        val switches = listOf(
            "instagram" to binding.switchInstagram,
            "youtube" to binding.switchYoutube,
            "reddit" to binding.switchReddit,
            "snapchat" to binding.switchSnapchat,
        )

        switches.forEach { (key, switch) ->
            switch.isChecked = key !in disabled
            switch.setOnCheckedChangeListener { _, isChecked ->
                val current = prefs.getStringSet("disabled_apps", mutableSetOf())
                    ?.toMutableSet() ?: mutableSetOf()
                if (isChecked) current.remove(key) else current.add(key)
                prefs.edit().putStringSet("disabled_apps", current).apply()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updatePermissionUI()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updatePermissionUI() {
        val overlayGranted = Settings.canDrawOverlays(requireContext())
        val accessibilityGranted = isAccessibilityServiceEnabled()

        updateDot(binding.dotOverlay, overlayGranted)
        updateDot(binding.dotAccessibility, accessibilityGranted)
        binding.btnOverlay.isEnabled = !overlayGranted
        binding.btnAccessibility.isEnabled = !accessibilityGranted

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val notifGranted = ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            updateDot(binding.dotNotification, notifGranted)
            binding.btnNotification.isEnabled = !notifGranted
        }

        binding.statusText.text = if (overlayGranted && accessibilityGranted) {
            getString(R.string.status_all_set)
        } else {
            getString(R.string.status_missing_permissions)
        }
    }

    private fun updateDot(dot: View, granted: Boolean) {
        val color = if (granted) R.color.colorGranted else R.color.colorNotGranted
        dot.backgroundTintList = ContextCompat.getColorStateList(requireContext(), color)
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val am = requireContext().getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        return am.getEnabledAccessibilityServiceList(
            android.accessibilityservice.AccessibilityServiceInfo.FEEDBACK_ALL_MASK
        ).any { it.resolveInfo.serviceInfo.packageName == requireContext().packageName }
    }
}
