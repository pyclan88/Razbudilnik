package com.ruslanataev.razbudilnik.presentation.ui.reader

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ruslanataev.razbudilnik.presentation.ui.reader.viewmodel.RegularReaderViewModel

@Composable
fun RegularReaderRoute(
    modifier: Modifier = Modifier,
    viewModel: RegularReaderViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val currentState = state

    if (currentState == null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }

        return
    }

    RegularReaderScreen(
        state = currentState,
        modifier = modifier,
    )
}
