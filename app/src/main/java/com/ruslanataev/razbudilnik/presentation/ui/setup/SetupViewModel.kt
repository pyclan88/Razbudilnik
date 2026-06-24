package com.ruslanataev.razbudilnik.presentation.ui.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ruslanataev.razbudilnik.domain.alarm.usecases.CancelAlarmUseCase
import com.ruslanataev.razbudilnik.domain.alarm.usecases.ScheduleAlarmUseCase
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
    private val scheduleAlarmUseCase: ScheduleAlarmUseCase,
    private val cancelAlarmUseCase: CancelAlarmUseCase,
) : ViewModel() {

    private val _state: MutableStateFlow<SetupUiState> = MutableStateFlow(SetupUiState.initial())
    val state: StateFlow<SetupUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            observeAlarmSettingsUseCase().collect { preferences ->
                _state.update { currentState ->
                    currentState.copy(
                        hour = preferences.hour,
                        minute = preferences.minute,
                        enabled = preferences.enabled,
                        statusMessage = null
                    )
                }
            }
        }
    }

    fun onExactAlarmAccessChecked(canScheduleExactAlarm: Boolean) {
        if (!canScheduleExactAlarm) {
            return
        }

        _state.update { currentState ->
            currentState.copy(statusMessage = null)
        }
    }

    fun onEnabledChange(enabled: Boolean) {
        val currentState = _state.value

        viewModelScope.launch {
            if (enabled) {
                val wasScheduled = scheduleAlarmUseCase(currentState.hour, currentState.minute)
                if (!wasScheduled) {
                    saveAlarmEnabledUseCase(false)

                    _state.update { latestState ->
                        latestState.copy(
                            enabled = false,
                            statusMessage = "Exact alarms are unavailable on this device right now.",
                        )
                    }
                    return@launch
                }
            } else {
                cancelAlarmUseCase()
            }

            saveAlarmEnabledUseCase(enabled)

            _state.update { latestState ->
                latestState.copy(
                    enabled = enabled,
                    statusMessage = null,
                )
            }
        }
    }

    fun onTimeSelected(hour: Int, minute: Int) {
        viewModelScope.launch {
            val wasEnabled = _state.value.enabled

            saveAlarmTimeUseCase(hour, minute)

            _state.update { currentState ->
                currentState.copy(
                    hour = hour,
                    minute = minute,
                    statusMessage = null,
                )
            }

            if (!wasEnabled) {
                return@launch
            }
            cancelAlarmUseCase()

            val wasScheduled = scheduleAlarmUseCase(hour, minute)
            if (!wasScheduled) {
                saveAlarmEnabledUseCase(false)

                _state.update { currentState ->
                    currentState.copy(
                        hour = hour,
                        minute = minute,
                        enabled = false,
                        statusMessage = "Exact alarms are unavailable on this device right now.",
                    )
                }
            }
        }
    }
}