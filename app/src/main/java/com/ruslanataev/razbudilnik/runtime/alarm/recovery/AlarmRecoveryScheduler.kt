package com.ruslanataev.razbudilnik.runtime.alarm.recovery

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock

class AlarmRecoveryScheduler(
    context: Context
) {

    private val applicationContext = context.applicationContext

    private val alarmManager: AlarmManager =
        applicationContext.getSystemService(AlarmManager::class.java)


    @SuppressLint("MissingPermission")
    fun postponeRecovery(hour: Int, minute: Int) {
        val triggerAtMillis = SystemClock.elapsedRealtime() + RECOVERY_TIMEOUT_MILLIS

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            triggerAtMillis,
            createRecoveryPendingIntent(
                hour = hour,
                minute = minute,
            ),
        )
    }

    fun cancelRecovery() {
        alarmManager.cancel(
            createRecoveryPendingIntent(
                hour = 0,
                minute = 0,
            ),
        )
    }

    private fun createRecoveryPendingIntent(
        hour: Int,
        minute: Int,
    ): PendingIntent {
        val intent = Intent(
            applicationContext,
            AlarmRecoveryReceiver::class.java,
        ).apply {
            action = AlarmRecoveryReceiver.ACTION_RECOVER_ALARM

            putExtra(AlarmRecoveryReceiver.EXTRA_HOUR, hour)
            putExtra(AlarmRecoveryReceiver.EXTRA_MINUTE, minute)
        }

        return PendingIntent.getBroadcast(
            applicationContext,
            REQUEST_CODE_ALARM_RECOVERY,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private companion object {
        const val RECOVERY_TIMEOUT_MILLIS = 15_000L

        const val REQUEST_CODE_ALARM_RECOVERY = 3001
    }
}
