package com.ruslanataev.razbudilnik.runtime.alarm.recovery

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ruslanataev.razbudilnik.runtime.alarm.AlarmRingingService

class AlarmRecoveryReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_RECOVER_ALARM) {
            return
        }

        val hour = intent.getIntExtra(EXTRA_HOUR, DEFAULT_HOUR)
        val minute = intent.getIntExtra(EXTRA_MINUTE, DEFAULT_MINUTE)

        context.startForegroundService(
            AlarmRingingService.createStartIntent(
                context = context,
                hour = hour,
                minute = minute,
            ),
        )
    }

    companion object {
        const val ACTION_RECOVER_ALARM = "com.ruslanataev.razbudilnik.action.RECOVER_ALARM"

        const val EXTRA_HOUR = "extra_recovery_hour"
        const val EXTRA_MINUTE = "extra_recovery_minute"

        private const val DEFAULT_HOUR = 7
        private const val DEFAULT_MINUTE = 0
    }
}
