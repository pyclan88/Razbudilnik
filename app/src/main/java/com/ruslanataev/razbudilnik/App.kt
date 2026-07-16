package com.ruslanataev.razbudilnik

import android.app.Application
import com.ruslanataev.razbudilnik.runtime.alarm.AlarmNotificationHelper
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {

    override fun onCreate() {
        super.onCreate()

        AlarmNotificationHelper(this).createAlarmChannel()
    }
}