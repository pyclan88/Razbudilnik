package com.ruslanataev.razbudilnik.presentation.ui.reader.pagination

import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import com.ruslanataev.razbudilnik.presentation.ui.reader.models.ReaderPageRange

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

        val remainingText = bookText.substring(startOffset)

        val layoutResult = textMeasurer.measure(
            text = remainingText,
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

        return ReaderPageRange(
            startOffset = startOffset,
            endOffsetExclusive = (startOffset + relativeEndOffset)
                .coerceAtMost(bookText.length),
        )
    }
}
