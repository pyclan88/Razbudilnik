package com.ruslanataev.razbudilnik.domain.setup.models

data class AlarmSettings(
    val hour: Int,
    val minute: Int,
    val enabled: Boolean,
)