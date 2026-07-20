package com.ruslanataev.razbudilnik.domain.alarm.usecases

import com.ruslanataev.razbudilnik.domain.alarm.api.DirectBootAlarmSnapshotRepository
import com.ruslanataev.razbudilnik.domain.alarm.models.DirectBootAlarmSnapshot
import com.ruslanataev.razbudilnik.domain.setup.api.AlarmSettingsRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class SynchronizeDirectBootAlarmSnapshotUseCase @Inject constructor(
    private val alarmSettingsRepository: AlarmSettingsRepository,
    private val directBootAlarmSnapshotRepository: DirectBootAlarmSnapshotRepository,
) {

    suspend operator fun invoke() {
        val alarmSettings = alarmSettingsRepository.settings.first()

        directBootAlarmSnapshotRepository.save(
            snapshot = DirectBootAlarmSnapshot(
                hour = alarmSettings.hour,
                minute = alarmSettings.minute,
                enabled = alarmSettings.enabled,
            ),
        )
    }
}
