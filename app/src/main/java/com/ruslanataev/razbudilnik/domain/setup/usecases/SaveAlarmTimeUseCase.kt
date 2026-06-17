package com.ruslanataev.razbudilnik.domain.setup.usecases

import com.ruslanataev.razbudilnik.domain.setup.api.AlarmSettingsRepository
import javax.inject.Inject

class SaveAlarmTimeUseCase @Inject constructor(
    private val repository: AlarmSettingsRepository,
) {
    suspend operator fun invoke(hour: Int, minute: Int) {
        repository.saveTime(hour, minute)
    }
}