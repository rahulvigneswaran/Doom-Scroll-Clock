package com.doomscrollclock

import android.app.Application

class DoomScrollClockApp : Application() {
    override fun onCreate() {
        super.onCreate()
        TimerManager.init(this)
    }
}
