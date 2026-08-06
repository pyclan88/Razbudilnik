package com.ruslanataev.razbudilnik.presentation.ui.reader

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.ruslanataev.razbudilnik.presentation.ui.reader.pagination.ReaderTextPaginator
import com.ruslanataev.razbudilnik.presentation.ui.reader.states.RegularReaderUiState
import kotlin.math.abs

@Composable
fun RegularReaderScreen(
    state: RegularReaderUiState,
    onNextPage: (nextPageStartOffset: Int) -> Unit,
    onPreviousPage: (previousPageStartOffset: Int) -> Unit,
    modifier: Modifier = Modifier,
) {

    val textStyle = MaterialTheme.typography.bodyLarge

    val textMeasurer = rememberTextMeasurer()

    val paginator = remember(textMeasurer) {
        ReaderTextPaginator(textMeasurer)
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        val density = LocalDensity.current

        val pageConstraints = with(density) {
            Constraints(
                maxWidth = maxWidth.roundToPx(),
                maxHeight = maxHeight.roundToPx(),
            )
        }

        val pageRange = remember(
            state.bookText,
            state.currentViewOffset,
            textStyle,
            pageConstraints,
            paginator,
        ) {
            requireNotNull(
                paginator.calculatePageRange(
                    bookText = state.bookText,
                    startOffset = state.currentViewOffset,
                    textStyle = textStyle,
                    constraints = pageConstraints,
                ),
            )
        }

        val previousPageStartOffsets = remember(
            state.bookId,
            textStyle,
            pageConstraints,
        ) {
            mutableListOf<Int>()
        }

        val useHorizontalPageTurn = pageConstraints.maxWidth > pageConstraints.maxHeight

        val minimumSwipeDistancePx = if (useHorizontalPageTurn) {
            pageConstraints.maxWidth / 2f
        } else {
            pageConstraints.maxHeight / 2f
        }

        Text(
            text = state.bookText.substring(
                startIndex = pageRange.startOffset,
                endIndex = pageRange.endOffsetExclusive,
            ),
            style = textStyle,
            modifier = Modifier
                .fillMaxSize()
                .readerPageGesture(
                    useHorizontalMovement = useHorizontalPageTurn,
                    minimumSwipeDistancePx = minimumSwipeDistancePx,
                    onPageForward = {
                        if (pageRange.endOffsetExclusive < state.bookText.length) {
                            previousPageStartOffsets.add(pageRange.startOffset)
                            onNextPage(pageRange.endOffsetExclusive)
                        }
                    },
                    onPageBackward = {
                        val previousPageStartOffset = previousPageStartOffsets.removeLastOrNull()

                        if (previousPageStartOffset != null) {
                            onPreviousPage(previousPageStartOffset)
                        }
                    },
                ),
        )
    }
}

private fun Modifier.readerPageGesture(
    useHorizontalMovement: Boolean,
    minimumSwipeDistancePx: Float,
    onPageForward: () -> Unit,
    onPageBackward: () -> Unit,
): Modifier {
    return pointerInput(
        useHorizontalMovement,
        minimumSwipeDistancePx,
        onPageForward,
        onPageBackward,
    ) {
        awaitEachGesture {
            awaitFirstDown(requireUnconsumed = false)

            var pageTurnMovementPx = 0f
            var hadTwoPressedFingers = false

            do {
                val event = awaitPointerEvent()
                val pressedChanges = event.changes.filter { change -> change.pressed }

                if (pressedChanges.size == 2) {
                    hadTwoPressedFingers = true

                    val averagePageTurnChange = pressedChanges.fold(0f) { total, change ->
                        val positionChange = change.positionChange()

                        total + if (useHorizontalMovement) {
                            positionChange.x
                        } else {
                            positionChange.y
                        }
                    } / pressedChanges.size.toFloat()

                    pageTurnMovementPx += averagePageTurnChange
                    pressedChanges.forEach { change -> change.consume() }
                }
            } while (event.changes.any { change -> change.pressed })

            if (hadTwoPressedFingers && abs(pageTurnMovementPx) > minimumSwipeDistancePx) {
                if (pageTurnMovementPx < 0f) {
                    onPageForward()
                } else {
                    onPageBackward()
                }
            }
        }
    }
}
