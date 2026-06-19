package com.ruslanataev.razbudilnik.domain.alarm.usecases

import com.ruslanataev.razbudilnik.domain.alarm.api.AlarmScheduler
import javax.inject.Inject

class CancelAlarmUseCase @Inject constructor(
    private val alarmScheduler: AlarmScheduler,
) {
    suspend operator fun invoke() {
        alarmScheduler.cancel()
    }
}