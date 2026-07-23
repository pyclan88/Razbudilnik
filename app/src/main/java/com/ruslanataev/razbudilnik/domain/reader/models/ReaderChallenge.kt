package com.ruslanataev.razbudilnik.domain.reader.models

data class ReaderChallenge(
    val pages: List<ReaderPage>,
    val currentPageIndex: Int,
)
