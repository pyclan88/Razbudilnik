package com.ruslanataev.razbudilnik.data.alarm.scheduler

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.ruslanataev.razbudilnik.domain.alarm.api.AlarmScheduler
import com.ruslanataev.razbudilnik.domain.alarm.usecases.CalculateNextAlarmTriggerAtMillisUseCase
import com.ruslanataev.razbudilnik.runtime.alarm.AlarmReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AlarmSchedulerImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val calculateNextAlarmTriggerAtMillisUseCase: CalculateNextAlarmTriggerAtMillisUseCase,
) : AlarmScheduler {

    private val alarmManager: AlarmManager =
        context.getSystemService(AlarmManager::class.java)

    @SuppressLint("MissingPermission")
    override suspend fun schedule(hour: Int, minute: Int): Boolean {
        if (!alarmManager.canScheduleExactAlarms()) {
            return false
        }

        val triggerAtMillis = calculateNextAlarmTriggerAtMillisUseCase(
            hour = hour,
            minute = minute
        )

        val alarmClockInfo = AlarmManager.AlarmClockInfo(
            triggerAtMillis,
            createShowAlarmPendingIntent(),
        )

        alarmManager.setAlarmClock(
            alarmClockInfo,
            createTriggerAlarmPendingIntent(hour, minute)
        )

        return true
    }

    override suspend fun cancel() {
        alarmManager.cancel(createTriggerAlarmPendingIntent())
    }

    private fun createTriggerAlarmPendingIntent(
        hour: Int? = null,
        minute: Int? = null,
    ): PendingIntent {
        val intent = if (hour != null && minute != null) {
            AlarmReceiver.createTriggerIntent(
                context = context,
                hour = hour,
                minute = minute,
            )
        } else {
            AlarmReceiver.createTriggerIntent(context)
        }

        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_TRIGGER_ALARM,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun createShowAlarmPendingIntent(): PendingIntent {
        val intent = context.packageManager
            .getLaunchIntentForPackage(context.packageName)
            ?: Intent()

        return PendingIntent.getActivity(
            context,
            REQUEST_CODE_SHOW_ALARM,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private companion object {
        const val REQUEST_CODE_TRIGGER_ALARM = 1001
        const val REQUEST_CODE_SHOW_ALARM = 1002
    }
}
