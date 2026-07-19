package com.ruslanataev.razbudilnik.data.alarm.repository

import android.content.Context
import com.ruslanataev.razbudilnik.data.alarm.mappers.DirectBootAlarmSnapshotDtoToDirectBootAlarmSnapshotMapper
import com.ruslanataev.razbudilnik.data.alarm.mappers.DirectBootAlarmSnapshotToDirectBootAlarmSnapshotDtoMapper
import com.ruslanataev.razbudilnik.data.alarm.models.DirectBootAlarmSnapshotDto
import com.ruslanataev.razbudilnik.domain.alarm.api.DirectBootAlarmSnapshotRepository
import com.ruslanataev.razbudilnik.domain.alarm.models.DirectBootAlarmSnapshot
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

class DirectBootAlarmSnapshotRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context,
) : DirectBootAlarmSnapshotRepository {

    private val preferences = context
        .createDeviceProtectedStorageContext()
        .getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    override suspend fun save(snapshot: DirectBootAlarmSnapshot) {
        withContext(Dispatchers.IO) {
            val snapshotDto =
                DirectBootAlarmSnapshotToDirectBootAlarmSnapshotDtoMapper.map(snapshot)

            val wasSaved = preferences.edit()
                .putInt(HOUR_KEY, snapshotDto.hour)
                .putInt(MINUTE_KEY, snapshotDto.minute)
                .putBoolean(ENABLED_KEY, snapshotDto.enabled)
                .commit()

            if (!wasSaved) {
                throw IOException("Failed to save the direct boot alarm snapshot")
            }
        }
    }

    override suspend fun get(): DirectBootAlarmSnapshot? {
        return withContext(Dispatchers.IO) {
            if (
                !preferences.contains(HOUR_KEY) ||
                !preferences.contains(MINUTE_KEY) ||
                !preferences.contains(ENABLED_KEY)
            ) {
                return@withContext null
            }

            val snapshotDto = DirectBootAlarmSnapshotDto(
                hour = preferences.getInt(HOUR_KEY, 0),
                minute = preferences.getInt(MINUTE_KEY, 0),
                enabled = preferences.getBoolean(ENABLED_KEY, false)
            )

            DirectBootAlarmSnapshotDtoToDirectBootAlarmSnapshotMapper.map(snapshotDto)
        }
    }

    private companion object {
        const val PREFERENCES_NAME = "direct_boot_alarm_snapshot"
        const val HOUR_KEY = "hour"
        const val MINUTE_KEY = "minute"
        const val ENABLED_KEY = "enabled"
    }
}
