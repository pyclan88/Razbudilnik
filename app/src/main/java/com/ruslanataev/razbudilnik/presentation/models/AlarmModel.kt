package com.ruslanataev.razbudilnik.presentation.models

data class AlarmModel(
    val wakeUpTime: String,
    val enabled: Boolean,
    val requiredPageCount: Int,
)
