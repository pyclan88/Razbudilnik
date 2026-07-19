package com.ruslanataev.razbudilnik.domain.setup.usecases

import com.ruslanataev.razbudilnik.domain.alarm.usecases.SynchronizeDirectBootAlarmSnapshotUseCase
import com.ruslanataev.razbudilnik.domain.setup.api.AlarmSettingsRepository
import javax.inject.Inject

class SaveAlarmEnabledUseCase @Inject constructor(
    private val repository: AlarmSettingsRepository,
    private val synchronizeDirectBootAlarmSnapshotUseCase: SynchronizeDirectBootAlarmSnapshotUseCase,
) {
    suspend operator fun invoke(enable: Boolean) {
        repository.saveEnabled(enable)

        synchronizeDirectBootAlarmSnapshotUseCase()
    }
}
