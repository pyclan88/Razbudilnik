package com.ruslanataev.razbudilnik.presentation.ui.reader

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.ruslanataev.razbudilnik.presentation.ui.reader.pagination.ReaderTextPaginator
import com.ruslanataev.razbudilnik.presentation.ui.reader.states.RegularReaderUiState

@Composable
fun RegularReaderScreen(
    state: RegularReaderUiState,
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

        Text(
            text = state.bookText.substring(
                startIndex = pageRange.startOffset,
                endIndex = pageRange.endOffsetExclusive,
            ),
            style = textStyle,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
