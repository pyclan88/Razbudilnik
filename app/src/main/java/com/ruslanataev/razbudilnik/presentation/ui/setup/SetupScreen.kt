package com.ruslanataev.razbudilnik.presentation.ui.setup

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun SetupScreen(
    state: SetupUiState,
    onEnabledChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
    ) {
        Text(state.wakeUpTime)
        Switch(
            checked = state.enabled,
            onCheckedChange = onEnabledChange
        )
        Text("Page counts: ${state.requiredPageCount}")
        Button(
            onClick = {}
        ) {
            Text("Alarm Button")
        }
    }
}