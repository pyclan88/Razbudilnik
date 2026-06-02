package com.ruslanataev.razbudilnik.presentation.ui.setup

data class SetupUiState(
    val wakeUpTime: String = "07:00",
    val enabled: Boolean = true,
    val requiredPageCount: Int = 5,
)