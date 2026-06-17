package com.ruslanataev.razbudilnik.domain.setup.usecases

import com.ruslanataev.razbudilnik.domain.setup.api.AlarmSettingsRepository
import javax.inject.Inject

class SaveAlarmEnabledUseCase @Inject constructor(
    private val repository: AlarmSettingsRepository,
) {
    suspend operator fun invoke(enable: Boolean) {
        repository.saveEnabled(enable)
    }
}