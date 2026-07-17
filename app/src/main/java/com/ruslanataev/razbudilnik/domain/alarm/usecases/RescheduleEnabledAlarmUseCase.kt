package com.ruslanataev.razbudilnik.domain.alarm.usecases

import com.ruslanataev.razbudilnik.domain.alarm.api.AlarmScheduler
import com.ruslanataev.razbudilnik.domain.setup.api.AlarmSettingsRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class RescheduleEnabledAlarmUseCase @Inject constructor(
    private val alarmSettingsRepository: AlarmSettingsRepository,
    private val alarmScheduler: AlarmScheduler,
) {

    suspend operator fun invoke(): Boolean {
        val alarmSettings = alarmSettingsRepository.settings.first()

        if (!alarmSettings.enabled) {
            return false
        }

        return alarmScheduler.schedule(
            hour = alarmSettings.hour,
            minute = alarmSettings.minute,
        )
    }
}
