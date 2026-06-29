package com.ruslanataev.razbudilnik.presentation.ui.setup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
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
    onExactAlarmAccessDialogDismissed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isTimePickerVisible by remember { mutableStateOf(false) }

    if (isTimePickerVisible) {
        WakeUpTimePickerDialog(
            initialHour = state.hour,
            initialMinute = state.minute,
            onConfirm = { hour, minute ->
                onTimeSelected(hour, minute)
                isTimePickerVisible = false
            },
            onDismiss = {
                isTimePickerVisible = false
            },
        )
    }

    if (state.isExactAlarmAccessDialogVisible) {
        ExactAlarmAccessDialog(
            onOpenSettingsClick = onOpenExactAlarmSettingsClick,
            onDismiss = onExactAlarmAccessDialogDismissed,
        )
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
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WakeUpTimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onConfirm: (hour: Int, minute: Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true,
    )

    TimePickerDialog(
        title = {
            Text("Select wake-up time")
        },
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(timePickerState.hour, timePickerState.minute)
                },
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
            ) {
                Text("Cancel")
            }
        },
    ) {
        TimeInput(state = timePickerState)
    }
}

@Composable
private fun ExactAlarmAccessDialog(
    onOpenSettingsClick: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Allow exact alarms?")
        },
        text = {
            Text(
                "The Razbudilnik needs exact-alarm access so your alarm can ring at the selected time."
            )
        },
        confirmButton = {
            TextButton(
                onClick = onOpenSettingsClick,
            ) {
                Text("Open settings")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
            ) {
                Text("Not now")
            }
        },
    )

}

@Preview(showBackground = true)
@Composable
private fun SetupScreenPreview() {
    SetupScreen(
        state = SetupUiState.initial(),
        onTimeSelected = { _, _ -> },
        onEnabledChange = {},
        onOpenExactAlarmSettingsClick = {},
        onExactAlarmAccessDialogDismissed = {}
    )
}
