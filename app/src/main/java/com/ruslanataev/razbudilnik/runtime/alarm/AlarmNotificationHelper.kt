package com.ruslanataev.razbudilnik.runtime.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.content.ContextCompat

class AlarmNotificationHelper(
    private val context: Context,
) {

    fun createAlarmChannel() {
        val notificationManager = ContextCompat.getSystemService(
            context,
            NotificationManager::class.java,
        ) ?: return

        val channel = NotificationChannel(
            ALARM_CHANNEL_ID,
            "Alarm",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Alarm notification"
            lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            enableVibration(true)
        }

        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        const val ALARM_CHANNEL_ID = "alarm"
    }
}