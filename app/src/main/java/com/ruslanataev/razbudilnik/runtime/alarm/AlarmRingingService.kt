package com.ruslanataev.razbudilnik.runtime.alarm

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.IBinder

class AlarmRingingService : Service() {

    private var ringtone: Ringtone? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startAlarm(
                hour = intent.getIntExtra(EXTRA_HOUR, 7),
                minute = intent.getIntExtra(EXTRA_MINUTE, 0)
            )

            ACTION_STOP -> stopAlarm()
            else -> stopSelf()
        }

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        ringtone?.stop()
        ringtone = null
        super.onDestroy()
    }

    private fun startAlarm(hour: Int, minute: Int) {
        val notification = AlarmNotificationHelper(this).createAlarmNotification(hour, minute)

        startForeground(
            AlarmNotificationHelper.NOTIFICATION_ID_ALARM,
            notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
        )

        if (ringtone?.isPlaying == true) {
            return
        }

        val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

        ringtone = RingtoneManager.getRingtone(this, ringtoneUri)?.apply {
            isLooping = true
            play()
        }
    }

    private fun stopAlarm() {
        ringtone?.stop()
        ringtone = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    companion object {
        private const val ACTION_START = "com.ruslanataev.razbudilnik.action.START_ALARM"
        private const val ACTION_STOP = "com.ruslanataev.razbudilnik.action.STOP_ALARM"
        private const val EXTRA_HOUR = "extra_hour"
        private const val EXTRA_MINUTE = "extra_minute"

        fun createStartIntent(context: Context, hour: Int, minute: Int) =
            Intent(context, AlarmRingingService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_HOUR, hour)
                putExtra(EXTRA_MINUTE, minute)
            }

        fun createStopIntent(context: Context) =
            Intent(context, AlarmRingingService::class.java).apply {
                action = ACTION_STOP
            }
    }
}