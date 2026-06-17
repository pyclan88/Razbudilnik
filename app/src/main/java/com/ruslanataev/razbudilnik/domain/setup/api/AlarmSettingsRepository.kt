package com.ruslanataev.razbudilnik.domain.setup.api

import com.ruslanataev.razbudilnik.domain.setup.models.AlarmSettings
import kotlinx.coroutines.flow.Flow

interface AlarmSettingsRepository {
    val settings: Flow<AlarmSettings>

    suspend fun saveTime(hour: Int, minute: Int)

    suspend fun saveEnabled(enabled: Boolean)
}