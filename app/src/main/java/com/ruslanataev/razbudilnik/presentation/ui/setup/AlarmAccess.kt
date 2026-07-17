package com.ruslanataev.razbudilnik.presentation.ui.setup

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.ruslanataev.razbudilnik.runtime.alarm.AlarmNotificationHelper

internal fun hasRequiredAlarmAccess(context: Context): Boolean {
    return hasNotificationPermission(context) &&
            hasAlarmChannelAccess(context) &&
            hasFullScreenIntentAccess(context)
}

internal fun hasNotificationPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.POST_NOTIFICATIONS,
    ) == PackageManager.PERMISSION_GRANTED
}

internal fun hasAlarmChannelAccess(context: Context): Boolean {
    val notificationManager = context.getSystemService(NotificationManager::class.java)

    val alarmChannel =
        notificationManager.getNotificationChannel(AlarmNotificationHelper.ALARM_CHANNEL_ID)

    return alarmChannel != null && alarmChannel.importance >= NotificationManager.IMPORTANCE_HIGH
}

internal fun createAlarmChannelSettingsIntent(context: Context): Intent {
    return Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {
        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        putExtra(Settings.EXTRA_CHANNEL_ID, AlarmNotificationHelper.ALARM_CHANNEL_ID)
    }
}

internal fun hasFullScreenIntentAccess(context: Context): Boolean {
    val notificationManager =
        context.getSystemService(NotificationManager::class.java)

    return notificationManager.canUseFullScreenIntent()
}

internal fun createFullScreenIntentSettingsIntent(context: Context): Intent {
    return Intent(
        Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT,
        "package:${context.packageName}".toUri(),
    )
}
