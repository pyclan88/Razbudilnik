package com.ruslanataev.razbudilnik.presentation.ui.setup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ruslanataev.razbudilnik.presentation.ui.setup.states.SetupUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(
    state: SetupUiState,
    onTimeSelected: (hour: Int, minute: Int) -> Unit,
    onEnabledChange: (Boolean) -> Unit,
    onOpenExactAlarmSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isTimePickerVisible by remember { mutableStateOf(false) }

    val timePickerState = rememberTimePickerState(
        is24Hour = true,
    )

    LaunchedEffect(isTimePickerVisible, state.hour, state.minute) {
        if (isTimePickerVisible) {
            timePickerState.hour = state.hour
            timePickerState.minute = state.minute
        }
    }

    if (isTimePickerVisible) {
        TimePickerDialog(
            title = {
                Text("Select wake-up time")
            },
            onDismissRequest = {
                isTimePickerVisible = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onTimeSelected(timePickerState.hour, timePickerState.minute)
                        isTimePickerVisible = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        isTimePickerVisible = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        ) {
            TimeInput(state = timePickerState)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Wake up time")

        Button(onClick = {
            isTimePickerVisible = true
        }) {
            Text(state.wakeUpTime)
        }

        Switch(
            checked = state.enabled,
            onCheckedChange = onEnabledChange,
        )

        state.statusMessage?.let { statusMessage ->
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(statusMessage)

                TextButton(
                    onClick = onOpenExactAlarmSettingsClick,
                ) {
                    Text("Grant exact alarm access")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SetupScreenPreview() {
    SetupScreen(
        state = SetupUiState.initial(),
        onTimeSelected = { _, _ -> },
        onEnabledChange = {},
        onOpenExactAlarmSettingsClick = {}
    )
}
