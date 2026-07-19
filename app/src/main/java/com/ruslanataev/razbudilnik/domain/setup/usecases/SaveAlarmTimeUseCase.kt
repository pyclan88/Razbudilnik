package com.ruslanataev.razbudilnik.domain.setup.usecases

import com.ruslanataev.razbudilnik.domain.alarm.usecases.SynchronizeDirectBootAlarmSnapshotUseCase
import com.ruslanataev.razbudilnik.domain.setup.api.AlarmSettingsRepository
import javax.inject.Inject

class SaveAlarmTimeUseCase @Inject constructor(
    private val repository: AlarmSettingsRepository,
    private val synchronizeDirectBootAlarmSnapshotUseCase: SynchronizeDirectBootAlarmSnapshotUseCase,
) {
    suspend operator fun invoke(hour: Int, minute: Int) {
        repository.saveTime(hour, minute)

        synchronizeDirectBootAlarmSnapshotUseCase()
    }
}
