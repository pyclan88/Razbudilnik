package com.ruslanataev.razbudilnik.domain.reader.models

data class ReaderChallenge(
    val book: ReaderBook,
    val firstPageIndex: Int,
    val requiredPageCount: Int,
    val currentPageIndex: Int,
) {

    val pages: List<ReaderPage>
        get() = book.pages
            .drop(firstPageIndex)
            .take(requiredPageCount)
}
