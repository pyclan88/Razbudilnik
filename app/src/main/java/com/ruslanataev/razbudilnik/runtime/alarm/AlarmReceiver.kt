package com.ruslanataev.razbudilnik.runtime.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ruslanataev.razbudilnik.presentation.ui.alarm.AlarmActivity

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_TRIGGER_ALARM) {
            return
        }

        val activityIntent = Intent(context, AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP

            putExtra(EXTRA_HOUR, intent.getIntExtra(EXTRA_HOUR, 7))
            putExtra(EXTRA_MINUTE, intent.getIntExtra(EXTRA_MINUTE, 0))
        }

        context.startActivity(activityIntent)
    }

    companion object {
        const val ACTION_TRIGGER_ALARM = "com.ruslanataev.razbudilnik.action.TRIGGER_ALARM"
        const val EXTRA_HOUR = "extra_hour"
        const val EXTRA_MINUTE = "extra_minute"
    }
}