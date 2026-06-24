package com.ruslanataev.razbudilnik.presentation.ui.setup

import java.time.LocalTime

data class SetupUiState(
    val hour: Int,
    val minute: Int,
    val enabled: Boolean,
    val statusMessage: String? = null,
    val shouldEnableAfterPermissionGrant: Boolean = false,
) {
    val wakeUpTime: String
        get() = "%02d:%02d".format(hour, minute)

    companion object {
        fun initial(): SetupUiState {
            val now = LocalTime.now()

            return SetupUiState(
                hour = now.hour,
                minute = now.minute,
                enabled = false
            )
        }
    }
}