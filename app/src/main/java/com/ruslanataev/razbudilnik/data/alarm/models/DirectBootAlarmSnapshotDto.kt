package com.ruslanataev.razbudilnik.data.alarm.models

data class DirectBootAlarmSnapshotDto(
    val hour: Int,
    val minute: Int,
    val enabled: Boolean,
)
