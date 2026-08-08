package com.ruslanataev.razbudilnik.presentation.ui.reader.pagination

import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import com.ruslanataev.razbudilnik.presentation.ui.reader.models.ReaderPageRange

private const val MEASUREMENT_CHUNK_CHARACTER_COUNT = 8_192

internal class ReaderTextPaginator(
    private val textMeasurer: TextMeasurer,
) {

    fun calculatePageRange(
        bookText: String,
        startOffset: Int,
        textStyle: TextStyle,
        constraints: Constraints,
    ): ReaderPageRange? {
        require(startOffset in 0..bookText.length) {
            "Reader page start offset must be inside the book text"
        }
        require(constraints.hasBoundedWidth && constraints.maxWidth > 0) {
            "Reader page width must be finite and positive"
        }
        require(constraints.hasBoundedHeight && constraints.maxHeight > 0) {
            "Reader page height must be finite and positive"
        }

        if (startOffset == bookText.length) {
            return null
        }

        var measurementEndOffset =
            (startOffset + MEASUREMENT_CHUNK_CHARACTER_COUNT).coerceAtMost(bookText.length)

        while (true) {
            val measuredText = bookText.substring(
                startIndex = startOffset,
                endIndex = measurementEndOffset,
            )

            val layoutResult = textMeasurer.measure(
                text = measuredText,
                style = textStyle,
                overflow = TextOverflow.Clip,
                softWrap = true,
                constraints = constraints,
            )

            check(layoutResult.lineCount > 0) {
                "Reader page layout must contain at least one line"
            }

            val lastVisibleLineIndex = (0 until layoutResult.lineCount).lastOrNull { lineIndex ->
                layoutResult.getLineBottom(lineIndex) <= constraints.maxHeight
            } ?: 0

            val relativeEndOffset = layoutResult.getLineEnd(
                lineIndex = lastVisibleLineIndex,
                visibleEnd = false,
            )

            check(relativeEndOffset > 0) {
                "Reader page must contain at least one character"
            }

            val endOffsetExclusive = startOffset + relativeEndOffset

            if (endOffsetExclusive < measurementEndOffset || measurementEndOffset == bookText.length) {
                return ReaderPageRange(
                    startOffset = startOffset,
                    endOffsetExclusive = endOffsetExclusive,
                )
            }

            measurementEndOffset =
                (measurementEndOffset + MEASUREMENT_CHUNK_CHARACTER_COUNT).coerceAtMost(bookText.length)
        }
    }
}
