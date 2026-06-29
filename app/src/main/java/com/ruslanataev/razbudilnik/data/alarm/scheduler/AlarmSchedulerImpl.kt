package com.ruslanataev.razbudilnik.data.alarm.scheduler

import android.app.AlarmManager
import android.app.AlarmManager.RTC_WAKEUP
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.ruslanataev.razbudilnik.runtime.alarm.AlarmReceiver
import com.ruslanataev.razbudilnik.domain.alarm.api.AlarmScheduler
import com.ruslanataev.razbudilnik.presentation.ui.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject

class AlarmSchedulerImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : AlarmScheduler {

    private val alarmManager: AlarmManager =
        context.getSystemService(AlarmManager::class.java)

    override suspend fun schedule(hour: Int, minute: Int): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            return false
        }

        val triggerAtMillis = calculateTriggerAtMillis(hour, minute)

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

    private fun calculateTriggerAtMillis(hour: Int, minute: Int): Long {
        val zoneId = ZoneId.systemDefault()
        val now = LocalDateTime.now(zoneId)

        var scheduledTime = now
            .withHour(hour)
            .withMinute(minute)
            .withSecond(0)
            .withNano(0)

        if (!scheduledTime.isAfter(now)) {
            scheduledTime = scheduledTime.plusDays(1)
        }

        return scheduledTime
            .atZone(zoneId)
            .toInstant()
            .toEpochMilli()
    }

    private fun createTriggerAlarmPendingIntent(
        hour: Int? = null,
        minute: Int? = null,
    ): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_TRIGGER_ALARM

            if (hour != null) {
                putExtra(AlarmReceiver.EXTRA_HOUR, hour)
            }
            if (minute != null) {
                putExtra(AlarmReceiver.EXTRA_MINUTE, minute)
            }
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