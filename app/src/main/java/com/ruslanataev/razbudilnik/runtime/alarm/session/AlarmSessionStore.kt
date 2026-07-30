package com.ruslanataev.razbudilnik.runtime.alarm.session

import android.content.Context
import androidx.core.content.edit

class AlarmSessionStore(
    context: Context,
) {

    private val applicationContext = context.applicationContext

    private val preferences = applicationContext
        .createDeviceProtectedStorageContext()
        .getSharedPreferences(
            PREFERENCES_NAME,
            Context.MODE_PRIVATE,
        )

    fun saveActiveSession(hour: Int, minute: Int) {
        preferences.edit(commit = true) {
            putBoolean(KEY_IS_ACTIVE, true)
            putInt(KEY_HOUR, hour)
            putInt(KEY_MINUTE, minute)
        }
    }

    fun getActiveSession(): ActiveAlarmSession? {
        val isActive = preferences.getBoolean(KEY_IS_ACTIVE, false)

        if (
            !isActive ||
            !preferences.contains(KEY_HOUR) ||
            !preferences.contains(KEY_MINUTE)
        ) {
            return null
        }

        return ActiveAlarmSession(
            hour = preferences.getInt(KEY_HOUR, 0),
            minute = preferences.getInt(KEY_MINUTE, 0),
        )
    }

    fun clearActiveSession() {
        preferences.edit(commit = true) {
            clear()
        }
    }

    private companion object {
        private const val PREFERENCES_NAME = "active_alarm_session"

        private const val KEY_IS_ACTIVE = "is_active"
        private const val KEY_HOUR = "hour"
        private const val KEY_MINUTE = "minute"
    }
}
