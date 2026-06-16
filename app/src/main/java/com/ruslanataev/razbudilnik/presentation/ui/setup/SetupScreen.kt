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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(
    state: SetupUiState,
    onTimeSelected: (hour: Int, minute: Int) -> Unit,
    onEnabledChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isTimePickerVisible by remember { mutableStateOf(false) }

    val timmePickerState = rememberTimePickerState(
        initialHour = state.hour,
        initialMinute = state.minute,
        is24Hour = true,
    )

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
                        onTimeSelected(timmePickerState.hour, timmePickerState.minute)
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
            TimeInput(state = timmePickerState)
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
    }
}

@Preview(showBackground = true)
@Composable
private fun SetupScreenPreview() {
    SetupScreen(
        state = SetupUiState(),
        onTimeSelected = { _, _ -> },
        onEnabledChange = {},
    )
}
