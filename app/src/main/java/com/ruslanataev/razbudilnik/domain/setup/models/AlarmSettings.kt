package com.ruslanataev.razbudilnik.domain.setup.models

data class AlarmSettings(
    val hour: Int = 7,
    val minute: Int = 0,
    val enabled: Boolean = true,
)