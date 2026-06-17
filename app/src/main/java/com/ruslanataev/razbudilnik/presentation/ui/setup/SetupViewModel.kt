package com.ruslanataev.razbudilnik.presentation.ui.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ruslanataev.razbudilnik.domain.setup.api.AlarmSettingsRepository
import com.ruslanataev.razbudilnik.domain.setup.usecases.ObserveAlarmSettingsUseCase
import com.ruslanataev.razbudilnik.domain.setup.usecases.SaveAlarmEnabledUseCase
import com.ruslanataev.razbudilnik.domain.setup.usecases.SaveAlarmTimeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SetupViewModel @Inject constructor(
    private val observeAlarmSettingsUseCase: ObserveAlarmSettingsUseCase,
    private val saveAlarmEnabledUseCase: SaveAlarmEnabledUseCase,
    private val saveAlarmTimeUseCase: SaveAlarmTimeUseCase,
) : ViewModel() {

    private val _state: MutableStateFlow<SetupUiState> = MutableStateFlow(SetupUiState())
    val state: StateFlow<SetupUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            observeAlarmSettingsUseCase().collect { preferences ->
                _state.update { currentState ->
                    currentState.copy(
                        hour = preferences.hour,
                        minute = preferences.minute,
                        enabled = preferences.enabled
                    )
                }
            }
        }
    }

    fun onEnabledChange(enabled: Boolean) {
        _state.update { currentState ->
            currentState.copy(enabled = enabled)
        }

        viewModelScope.launch {
            saveAlarmEnabledUseCase(enabled)
        }
    }

    fun onTimeSelected(hour: Int, minute: Int) {
        _state.update { currentState ->
            currentState.copy(
                hour = hour,
                minute = minute,
            )
        }

        viewModelScope.launch {
            saveAlarmTimeUseCase(hour, minute)
        }
    }
}