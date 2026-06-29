package com.ruslanataev.razbudilnik.runtime.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_TRIGGER_ALARM) {
            return
        }

        val hour = intent.getIntExtra(EXTRA_HOUR, 7)
        val minute = intent.getIntExtra(EXTRA_MINUTE, 0)

        AlarmNotificationHelper(context).showAlarmNotification(hour, minute)
    }

    companion object {
        const val ACTION_TRIGGER_ALARM = "com.ruslanataev.razbudilnik.action.TRIGGER_ALARM"
        const val EXTRA_HOUR = "extra_hour"
        const val EXTRA_MINUTE = "extra_minute"
    }
}