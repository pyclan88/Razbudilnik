package com.ruslanataev.razbudilnik.runtime.alarm

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.ruslanataev.razbudilnik.presentation.ui.alarm.AlarmActivity

class AlarmNotificationHelper(
    private val context: Context,
) {

    fun createAlarmChannel() {
        val notificationManager = getNotificationManager() ?: return

        val channel = NotificationChannel(
            ALARM_CHANNEL_ID,
            "Alarm",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Alarm notification"
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            enableVibration(true)

            setSound(null, null)
        }

        notificationManager.createNotificationChannel(channel)
    }

    @SuppressLint("FullScreenIntentPolicy")
    fun createAlarmNotification(hour: Int, minute: Int): Notification {
        createAlarmChannel()

        val alarmActivityPendingIntent = createAlarmActivityPendingIntent(hour, minute)

        return NotificationCompat.Builder(context, ALARM_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
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
    }

    fun createActiveAlarmNotification(hour: Int, minute: Int): Notification {
        createActiveAlarmChannel()

        return NotificationCompat.Builder(context, ACTIVE_ALARM_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Alarm")
            .setContentText("Wake up time: %02d:%02d".format(hour, minute))
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(createAlarmActivityPendingIntent(hour, minute))
            .setOnlyAlertOnce(true)
            .setAutoCancel(false)
            .setOngoing(true)
            .build()
    }

    private fun createActiveAlarmChannel() {
        val notificationManager = getNotificationManager() ?: return

        val channel = NotificationChannel(
            ACTIVE_ALARM_CHANNEL_ID,
            "Active alarm",
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "Alarm currently in progress"
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            enableVibration(false)
            setSound(null, null)
        }

        notificationManager.createNotificationChannel(channel)
    }

    private fun getNotificationManager(): NotificationManager? {
        return ContextCompat.getSystemService(
            context,
            NotificationManager::class.java,
        )
    }

    private fun createAlarmActivityPendingIntent(hour: Int, minute: Int): PendingIntent {
        val alarmActivityIntent = Intent(context, AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(AlarmReceiver.EXTRA_HOUR, hour)
            putExtra(AlarmReceiver.EXTRA_MINUTE, minute)
        }

        return PendingIntent.getActivity(
            context,
            REQUEST_CODE_ALARM_ACTIVITY,
            alarmActivityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    companion object {
        const val ALARM_CHANNEL_ID = "alarm"
        const val NOTIFICATION_ID_ALARM = 2001

        private const val ACTIVE_ALARM_CHANNEL_ID = "active_alarm"
        private const val REQUEST_CODE_ALARM_ACTIVITY = 2002
    }
}
