package com.ruslanataev.razbudilnik.runtime.alarm.volume

import android.content.Context
import android.media.AudioManager
import kotlin.math.ceil

class AlarmVolumeController(
    context: Context,
) {

    private val audioManager: AudioManager =
        context.applicationContext.getSystemService(AudioManager::class.java)

    private var originalAlarmVolume: Int? = null
    private var protectedAlarmVolume: Int? = null

    fun startProtection() {
        if (audioManager.isVolumeFixed) {
            return
        }

        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)
        val maximumVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)

        if (originalAlarmVolume == null) {
            originalAlarmVolume = currentVolume
        }

        val minimumProtectedVolume = ceil(
            maximumVolume * MINIMUM_ALARM_VOLUME_RATIO,
        ).toInt()

        protectedAlarmVolume = maxOf(
            currentVolume,
            minimumProtectedVolume
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
        val volumeToRestore = originalAlarmVolume ?: return

        if (!audioManager.isVolumeFixed) {
            audioManager.setStreamVolume(
                AudioManager.STREAM_ALARM,
                volumeToRestore,
                0,
            )
        }

        originalAlarmVolume = null
        protectedAlarmVolume = null
    }

    private companion object {
        private const val MINIMUM_ALARM_VOLUME_RATIO = 0.7
    }
}