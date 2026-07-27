package com.ruslanataev.razbudilnik.runtime.alarm.volume

import android.content.Context
import android.media.AudioManager
import androidx.core.content.edit
import kotlin.math.ceil

class AlarmVolumeController(
    context: Context,
) {

    private val applicationContext = context.applicationContext

    private val audioManager: AudioManager =
        applicationContext.getSystemService(AudioManager::class.java)

    private val preferences = applicationContext
        .createDeviceProtectedStorageContext()
        .getSharedPreferences(
            PREFERENCES_NAME,
            Context.MODE_PRIVATE,
        )

    private var protectedAlarmVolume: Int? = null

    fun startProtection() {
        if (audioManager.isVolumeFixed) {
            return
        }

        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)
        val maximumVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)

        if (!preferences.contains(KEY_ORIGINAL_ALARM_VOLUME)) {
            preferences.edit(commit = true) {
                putInt(KEY_ORIGINAL_ALARM_VOLUME, currentVolume)
            }
        }

        val minimumProtectedVolume = ceil(
            maximumVolume * MINIMUM_ALARM_VOLUME_RATIO,
        ).toInt()

        protectedAlarmVolume = maxOf(
            currentVolume,
            minimumProtectedVolume,
        )

        enforceProtectedVolume()
    }

    fun enforceProtectedVolume() {
        val targetVolume = protectedAlarmVolume ?: return

        if (audioManager.isVolumeFixed) {
            return
        }

        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)

        if (currentVolume < targetVolume) {
            audioManager.setStreamVolume(
                AudioManager.STREAM_ALARM,
                targetVolume,
                0,
            )
        }
    }

    fun stopProtection() {
        restoreOriginalVolumeIfNeeded()
        protectedAlarmVolume = null
    }

    fun restoreOriginalVolumeIfNeeded() {
        if (!preferences.contains(KEY_ORIGINAL_ALARM_VOLUME)) {
            return
        }

        val originalVolume = preferences.getInt(
            KEY_ORIGINAL_ALARM_VOLUME,
            VOLUME_NOT_STORED,
        )

        if (!audioManager.isVolumeFixed && originalVolume != VOLUME_NOT_STORED) {
            audioManager.setStreamVolume(
                AudioManager.STREAM_ALARM,
                originalVolume,
                0,
            )
        }

        preferences.edit(commit = true) {
            remove(KEY_ORIGINAL_ALARM_VOLUME)
        }
    }

    private companion object {
        private const val PREFERENCES_NAME = "alarm_volume_protection"
        private const val KEY_ORIGINAL_ALARM_VOLUME = "original_alarm_volume"
        private const val VOLUME_NOT_STORED = -1
        private const val MINIMUM_ALARM_VOLUME_RATIO = 0.7
    }
}
