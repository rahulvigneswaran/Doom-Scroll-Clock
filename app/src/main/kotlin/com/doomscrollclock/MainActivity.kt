package com.doomscrollclock

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.accessibility.AccessibilityManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.doomscrollclock.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val notifPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { updatePermissionUI() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            binding.cardNotification.visibility = View.VISIBLE
            binding.btnNotification.setOnClickListener {
                notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        binding.btnAccessibility.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        binding.btnStats.setOnClickListener {
            startActivity(Intent(this, StatsActivity::class.java))
        }

        val prefs = getSharedPreferences("doom_scroll_prefs", MODE_PRIVATE)
        binding.btnModeTime.setOnClickListener {
            prefs.edit().putString("notif_display_mode", "time").apply()
            updateModeButtons("time")
        }
        binding.btnModeDistance.setOnClickListener {
            prefs.edit().putString("notif_display_mode", "distance").apply()
            updateModeButtons("distance")
        }
        updateModeButtons(prefs.getString("notif_display_mode", "time") ?: "time")
    }

    override fun onResume() {
        super.onResume()
        updatePermissionUI()
    }

    private fun updatePermissionUI() {
        val notifGranted = isNotifPermissionGranted()
        val accessibilityGranted = isAccessibilityServiceEnabled()
        val allGranted = notifGranted && accessibilityGranted

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            updateDot(binding.dotNotification, notifGranted)
            binding.btnNotification.isEnabled = !notifGranted
        }
        updateDot(binding.dotAccessibility, accessibilityGranted)
        binding.btnAccessibility.isEnabled = !accessibilityGranted

        binding.statusText.text = if (allGranted) {
            getString(R.string.status_all_set)
        } else {
            getString(R.string.status_missing_permissions)
        }

        val visibility = if (allGranted) View.VISIBLE else View.GONE
        binding.btnStats.visibility = visibility
        binding.layoutDisplayToggle.visibility = visibility
    }

    private fun updateDot(dot: View, granted: Boolean) {
        val color = if (granted) R.color.colorGranted else R.color.colorNotGranted
        dot.backgroundTintList = ContextCompat.getColorStateList(this, color)
    }

    private fun updateModeButtons(mode: String) {
        val accentColor = 0xFF7B61FF.toInt()
        val defaultColor = getColor(R.color.colorOnSurface)
        binding.btnModeTime.setTextColor(if (mode == "time") accentColor else defaultColor)
        binding.btnModeDistance.setTextColor(if (mode == "distance") accentColor else defaultColor)
    }

    private fun isNotifPermissionGranted(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val am = getSystemService(ACCESSIBILITY_SERVICE) as AccessibilityManager
        return am.getEnabledAccessibilityServiceList(
            android.accessibilityservice.AccessibilityServiceInfo.FEEDBACK_ALL_MASK
        ).any { it.resolveInfo.serviceInfo.packageName == packageName }
    }
}
