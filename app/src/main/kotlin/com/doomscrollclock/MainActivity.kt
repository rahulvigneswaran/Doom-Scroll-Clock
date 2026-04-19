package com.doomscrollclock

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.doomscrollclock.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnOverlay.setOnClickListener {
            startActivity(
                Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
            )
        }

        binding.btnAccessibility.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
    }

    override fun onResume() {
        super.onResume()
        updatePermissionUI()
    }

    private fun updatePermissionUI() {
        val overlayGranted = Settings.canDrawOverlays(this)
        val accessibilityGranted = isAccessibilityServiceEnabled()

        updateDot(binding.dotOverlay, overlayGranted)
        updateDot(binding.dotAccessibility, accessibilityGranted)
        binding.btnOverlay.isEnabled = !overlayGranted
        binding.btnAccessibility.isEnabled = !accessibilityGranted

        binding.statusText.text = if (overlayGranted && accessibilityGranted) {
            getString(R.string.status_all_set)
        } else {
            getString(R.string.status_missing_permissions)
        }
    }

    private fun updateDot(dot: android.view.View, granted: Boolean) {
        val color = if (granted) R.color.colorGranted else R.color.colorNotGranted
        dot.backgroundTintList = ContextCompat.getColorStateList(this, color)
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val am = getSystemService(ACCESSIBILITY_SERVICE) as AccessibilityManager
        return am.getEnabledAccessibilityServiceList(android.accessibilityservice.AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
            .any { it.resolveInfo.serviceInfo.packageName == packageName }
    }
}
