package com.ruslanataev.razbudilnik.presentation.ui.setup

data class SetupUiState(
    val hour: Int = 7,
    val minute: Int = 0,
    val enabled: Boolean = true,
) {
    val wakeUpTime: String
        get() = "%02d:%02d".format(hour, minute)
}