package com.ruslanataev.razbudilnik.domain.alarm.usecases

import com.ruslanataev.razbudilnik.domain.alarm.api.AlarmScheduler
import javax.inject.Inject

class ScheduleAlarmUseCase @Inject constructor(
    private val alarmScheduler: AlarmScheduler,
) {
    suspend operator fun invoke(hour: Int, minute: Int): Boolean {
        return alarmScheduler.schedule(hour, minute)
    }
}