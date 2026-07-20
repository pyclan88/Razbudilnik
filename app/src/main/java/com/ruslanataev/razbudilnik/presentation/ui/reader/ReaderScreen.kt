package com.ruslanataev.razbudilnik.presentation.ui.reader

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ruslanataev.razbudilnik.presentation.ui.reader.states.ReaderUiState
import kotlin.time.Duration.Companion.seconds

@Composable
fun ReaderScreen(
    state: ReaderUiState,
    onNextPageClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(text = state.pageTextLabel)

        Text(text = state.progressText)

        Text(text = state.pageText)

        Button(
            enabled = state.canGoToNextPage,
            onClick = onNextPageClick,
        ) {
            Text("Next")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ReaderScreenPreview() {
    ReaderScreen(
        state = ReaderUiState(
            pageText = "The morning was still dark when the first bell rang.",
            pageNumber = 1,
            pageCount = 3,
            activeReadingTime = 42.seconds,
            requiredReadingTime = 180.seconds,
            canGoToNextPage = false,
            shouldMuteAlarm = true,
        ),
        onNextPageClick = {},
    )
}
