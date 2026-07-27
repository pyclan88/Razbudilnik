package com.ruslanataev.razbudilnik

import android.app.Application
import com.ruslanataev.razbudilnik.runtime.alarm.AlarmNotificationHelper
import com.ruslanataev.razbudilnik.runtime.alarm.volume.AlarmVolumeController
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {

    override fun onCreate() {
        super.onCreate()

        AlarmVolumeController(this).restoreOriginalVolumeIfNeeded()

        AlarmNotificationHelper(this).createAlarmChannel()
    }
}
