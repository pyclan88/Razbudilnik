package com.ruslanataev.razbudilnik.data.setup.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ruslanataev.razbudilnik.domain.setup.api.AlarmSettingsRepository
import com.ruslanataev.razbudilnik.domain.setup.models.AlarmSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.dataStore by preferencesDataStore(name = "alarm_preferences")

class AlarmSettingsRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : AlarmSettingsRepository {

    override val settings: Flow<AlarmSettings> =
        context.dataStore.data.map { prefs ->
            AlarmSettings(
                hour = prefs[HOUR_KEY] ?: 7,
                minute = prefs[MINUTE_KEY] ?: 0,
                enabled = prefs[ENABLED_KEY] ?: true,
            )
        }

    override suspend fun saveTime(hour: Int, minute: Int) {
        context.dataStore.edit { prefs ->
            prefs[HOUR_KEY] = hour
            prefs[MINUTE_KEY] = minute
        }
    }

    override suspend fun saveEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[ENABLED_KEY] = enabled
        }
    }

    private companion object {
        val HOUR_KEY = intPreferencesKey("hour")
        val MINUTE_KEY = intPreferencesKey("minute")
        val ENABLED_KEY = booleanPreferencesKey("enabled")
    }
}