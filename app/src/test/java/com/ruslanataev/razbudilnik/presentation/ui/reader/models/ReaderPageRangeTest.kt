package com.ruslanataev.razbudilnik.presentation.ui.reader.models

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ReaderPageRangeTest {

    @Test
    fun `calculate length from offsets`() {
        val readerPageRange = ReaderPageRange(
            startOffset = 10,
            endOffsetExclusive = 25,
        )

        assertEquals(15, readerPageRange.length)
    }

    @Test
    fun `rejects negative start offset`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            ReaderPageRange(
                startOffset = -1,
                endOffsetExclusive = 10,
            )
        }

        assertEquals(
            "Reader page start offset must not be negative",
            exception.message,
        )
    }

    @Test
    fun `rejects empty range`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            ReaderPageRange(
                startOffset = 10,
                endOffsetExclusive = 10,
            )
        }

        assertEquals(
            "Reader page end offset must be greater than start offset",
            exception.message,
        )
    }
}
