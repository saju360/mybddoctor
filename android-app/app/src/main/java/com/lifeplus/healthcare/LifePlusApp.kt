package com.lifeplus.healthcare

import android.app.Application
import com.lifeplus.healthcare.ads.AdsManager
import com.lifeplus.healthcare.util.AppSettings
import com.lifeplus.healthcare.util.NotificationScheduler
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class LifePlusApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Sync admin settings from backend
        AppSettings.get(this).syncFromBackend(this)
        AdsManager.initialize(this)
        NotificationScheduler.ensureReminderChannel(this)
    }
}
