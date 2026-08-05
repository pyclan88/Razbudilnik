package com.ruslanataev.razbudilnik.presentation.ui.reader.models

data class ReaderPageRange(
    val startOffset: Int,
    val endOffsetExclusive: Int,
) {

    init {
        require(startOffset >= 0) {
            "Reader page start offset must not be negative"
        }
        require(endOffsetExclusive > startOffset) {
            "Reader page end offset must be greater than start offset"
        }
    }

    val length: Int
        get() = endOffsetExclusive - startOffset
}
