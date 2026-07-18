package com.ruslanataev.razbudilnik.domain.alarm.models

data class DirectBootAlarmSnapshot(
    val hour: Int,
    val minute: Int,
    val enabled: Boolean,
)
