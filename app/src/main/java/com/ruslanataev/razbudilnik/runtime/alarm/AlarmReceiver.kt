package com.ruslanataev.razbudilnik.runtime.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.UserManager
import com.ruslanataev.razbudilnik.domain.alarm.usecases.RescheduleDirectBootAlarmUseCase
import com.ruslanataev.razbudilnik.domain.alarm.usecases.RescheduleEnabledAlarmUseCase
import com.ruslanataev.razbudilnik.runtime.alarm.events.AlarmRuntimeEvents
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var rescheduleEnabledAlarmUseCase: RescheduleEnabledAlarmUseCase

    @Inject
    lateinit var rescheduleDirectBootAlarmUseCase: RescheduleDirectBootAlarmUseCase

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_TRIGGER_ALARM) {
            return
        }

        val hour = intent.getIntExtra(EXTRA_HOUR, 7)
        val minute = intent.getIntExtra(EXTRA_MINUTE, 0)

        context.startForegroundService(
            AlarmRingingService.createStartIntent(
                context = context,
                hour = hour,
                minute = minute,
            ),
        )

        AlarmRuntimeEvents.notifyAlarmStarted()

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userManager = context.getSystemService(UserManager::class.java)

                if (userManager?.isUserUnlocked == true) {
                    rescheduleEnabledAlarmUseCase()
                } else {
                    rescheduleDirectBootAlarmUseCase()
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_TRIGGER_ALARM = "com.ruslanataev.razbudilnik.action.TRIGGER_ALARM"
        const val EXTRA_HOUR = "extra_hour"
        const val EXTRA_MINUTE = "extra_minute"

        fun createTriggerIntent(
            context: Context,
            hour: Int,
            minute: Int,
        ): Intent {
            return createTriggerIntent(context).apply {
                putExtra(EXTRA_HOUR, hour)
                putExtra(EXTRA_MINUTE, minute)
            }
        }

        fun createTriggerIntent(context: Context): Intent {
            return Intent(context, AlarmReceiver::class.java).apply {
                action = ACTION_TRIGGER_ALARM
            }
        }
    }
}
