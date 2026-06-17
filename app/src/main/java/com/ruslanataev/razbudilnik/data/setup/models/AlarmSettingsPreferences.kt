package com.ruslanataev.razbudilnik.data.setup.models

data class AlarmSettingsPreferences(
    val hour: Int = 7,
    val minute: Int = 0,
    val enabled: Boolean = true,
)