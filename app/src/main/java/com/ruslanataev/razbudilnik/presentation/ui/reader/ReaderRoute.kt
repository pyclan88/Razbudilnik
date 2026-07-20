package com.ruslanataev.razbudilnik.presentation.ui.reader

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ruslanataev.razbudilnik.presentation.ui.reader.viewmodel.ReaderViewModel

@Composable
fun ReaderRoute(
    modifier: Modifier = Modifier,
    viewModel: ReaderViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ReaderScreen(
        state = state,
        onNextPageClick = {},
        modifier = modifier,
    )
}
