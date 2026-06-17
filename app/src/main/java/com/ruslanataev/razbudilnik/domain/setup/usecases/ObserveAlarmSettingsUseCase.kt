package com.ruslanataev.razbudilnik.domain.setup.usecases

import com.ruslanataev.razbudilnik.domain.setup.api.AlarmSettingsRepository
import com.ruslanataev.razbudilnik.domain.setup.models.AlarmSettings
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveAlarmSettingsUseCase @Inject constructor(
    private val repository: AlarmSettingsRepository,
) {
    operator fun invoke(): Flow<AlarmSettings> {
        return repository.settings
    }
}