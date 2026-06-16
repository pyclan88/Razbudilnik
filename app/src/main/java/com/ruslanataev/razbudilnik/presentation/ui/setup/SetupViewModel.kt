package com.ruslanataev.razbudilnik.presentation.ui.setup

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SetupViewModel : ViewModel() {

    private val _state: MutableStateFlow<SetupUiState> = MutableStateFlow(SetupUiState())
    val state: StateFlow<SetupUiState> = _state.asStateFlow()

    fun onEnabledChange(enabled: Boolean) {
        _state.update { currentState ->
            currentState.copy(enabled = enabled)
        }
    }

    fun onTimeClick() {
        // Temporary stub for MVP wiring.
        // Next step: open a time picker or emit a one-time event.
    }
}