package com.ruslanataev.razbudilnik.presentation.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ruslanataev.razbudilnik.presentation.ui.setup.SetupScreen
import com.ruslanataev.razbudilnik.presentation.ui.setup.SetupViewModel

@Composable
fun RootScreen(
    modifier: Modifier = Modifier,
    viewModel: SetupViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    SetupScreen(
        state = state,
        onEnabledChange = viewModel::onEnabledChange,
        modifier = modifier,
    )
}