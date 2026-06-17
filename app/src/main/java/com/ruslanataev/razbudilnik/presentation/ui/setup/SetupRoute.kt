package com.ruslanataev.razbudilnik.presentation.ui.setup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun SetupRoute(
    modifier: Modifier = Modifier,
    viewModel: SetupViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    SetupScreen(
        state = state,
        onTimeSelected = viewModel::onTimeSelected,
        onEnabledChange = viewModel::onEnabledChange,
        modifier = modifier,
    )
}