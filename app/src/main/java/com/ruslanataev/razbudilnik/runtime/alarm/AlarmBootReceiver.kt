package com.ruslanataev.razbudilnik.runtime.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ruslanataev.razbudilnik.domain.alarm.usecases.RescheduleEnabledAlarmUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AlarmBootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var rescheduleEnabledAlarmUseCase: RescheduleEnabledAlarmUseCase

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) {
            return
        }

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                rescheduleEnabledAlarmUseCase()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
