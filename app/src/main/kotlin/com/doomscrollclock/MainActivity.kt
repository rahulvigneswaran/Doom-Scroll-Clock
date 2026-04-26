package com.doomscrollclock

import android.content.Context
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.accessibility.AccessibilityManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.doomscrollclock.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            routeInitialScreen()
        }

        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_stats -> { showFragment(StatsFragment()); true }
                R.id.nav_achievements -> { showFragment(AchievementsFragment()); true }
                R.id.nav_history -> { showFragment(HistoryFragment()); true }
                R.id.nav_settings -> { showFragment(SettingsFragment()); true }
                else -> false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Re-check in case the user navigated away to grant a permission and came back
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (currentFragment is SetupFragment && permissionsGranted()) {
            onSetupComplete()
        }
    }

    fun onSetupComplete() {
        binding.bottomNav.visibility = View.VISIBLE
        binding.bottomNav.selectedItemId = R.id.nav_stats
        showFragment(StatsFragment())
    }

    private fun routeInitialScreen() {
        if (permissionsGranted()) {
            binding.bottomNav.visibility = View.VISIBLE
            showFragment(StatsFragment())
        } else {
            binding.bottomNav.visibility = View.GONE
            showFragment(SetupFragment())
        }
    }

    private fun permissionsGranted(): Boolean {
        val overlayGranted = Settings.canDrawOverlays(this)
        val accessibilityGranted = isAccessibilityEnabled()
        return overlayGranted && accessibilityGranted
    }

    private fun isAccessibilityEnabled(): Boolean {
        val am = getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        return am.getEnabledAccessibilityServiceList(
            android.accessibilityservice.AccessibilityServiceInfo.FEEDBACK_ALL_MASK
        ).any { it.resolveInfo.serviceInfo.packageName == packageName }
    }

    private fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
