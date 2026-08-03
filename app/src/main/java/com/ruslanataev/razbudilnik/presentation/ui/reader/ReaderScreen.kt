package com.ruslanataev.razbudilnik.presentation.ui.reader

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ruslanataev.razbudilnik.presentation.ui.reader.states.ReaderUiState
import kotlin.math.abs
import kotlin.time.Duration.Companion.seconds

@Composable
fun ReaderScreen(
    state: ReaderUiState,
    onReaderInteractionChanged: (isFingerDown: Boolean, movementDistancePx: Float) -> Unit,
    onPreviousPageClick: () -> Unit,
    onNextPageClick: () -> Unit,
    onFinishChallengeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown()

                    onReaderInteractionChanged(true, 0f)

                    drag(down.id) { change ->
                        val movementDistancePx =
                            abs(change.positionChange().x) + abs(change.positionChange().y)

                        onReaderInteractionChanged(true, movementDistancePx)

                        change.consume()
                    }

                    onReaderInteractionChanged(false, 0f)
                }
            },
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(text = state.pageTextLabel)

        Text(text = state.progressText)

        Text(text = state.pageText)

        Button(
            enabled = state.canGoToPreviousPage,
            onClick = onPreviousPageClick,
        ) {
            Text("Back")
        }

        Button(
            enabled = state.canGoToNextPage,
            onClick = onNextPageClick,
        ) {
            Text("Next")
        }

        Button(
            enabled = state.canFinishChallenge,
            onClick = onFinishChallengeClick,
        ) {
            Text("Finish")
        }

        BuildVariantChallengeControls(
            onFinishChallenge = onFinishChallengeClick,
        )
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
            canGoToPreviousPage = false,
            canGoToNextPage = false,
            canFinishChallenge = false,
            shouldMuteAlarm = true,
        ),
        onReaderInteractionChanged = { _, _ -> },
        onPreviousPageClick = {},
        onNextPageClick = {},
        onFinishChallengeClick = {},
    )
}
