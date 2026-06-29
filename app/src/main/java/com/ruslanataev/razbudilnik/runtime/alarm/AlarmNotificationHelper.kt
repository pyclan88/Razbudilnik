package com.ruslanataev.razbudilnik.runtime.alarm

import android.Manifest
import android.R
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.ruslanataev.razbudilnik.presentation.ui.alarm.AlarmActivity

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

    @SuppressLint(
        "MissingPermission",
        "FullScreenIntentPolicy"
    )
    fun showAlarmNotification(hour: Int, minute: Int) {
        createAlarmChannel()

        val alarmActivityIntent = Intent(context, AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(AlarmReceiver.EXTRA_HOUR, hour)
            putExtra(AlarmReceiver.EXTRA_MINUTE, minute)
        }

        val alarmActivityPendingIntent = PendingIntent.getActivity(
            context,
            REQUEST_CODE_ALARM_ACTIVITY,
            alarmActivityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, ALARM_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Alarm")
            .setContentText("Wake up time: %02d:%02d".format(hour, minute))
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(alarmActivityPendingIntent)
            .setFullScreenIntent(alarmActivityPendingIntent, true)
            .setAutoCancel(false)
            .setOngoing(true)
            .build()

        if (!canPostNotifications()) {
            return
        }

        NotificationManagerCompat.from(context).notify(
            NOTIFICATION_ID_ALARM,
            notification,
        )
    }

    private fun canPostNotifications(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return true
        }

        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        const val ALARM_CHANNEL_ID = "alarm"

        private const val NOTIFICATION_ID_ALARM = 2001
        private const val REQUEST_CODE_ALARM_ACTIVITY = 2002
    }
}