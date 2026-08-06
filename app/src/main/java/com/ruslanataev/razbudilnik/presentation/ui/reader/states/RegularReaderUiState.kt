package com.ruslanataev.razbudilnik.presentation.ui.reader.states

data class RegularReaderUiState(
    val bookId: String,
    val bookTitle: String,
    val bookAuthor: String,
    val bookText: String,
    val currentViewOffset: Int = 0,
    val readingProgressOffset: Int = 0,
) {

    init {
        require(bookText.isNotBlank()) {
            "Regular reader book text must not be blank"
        }
        require(currentViewOffset in bookText.indices) {
            "Current reader position must be inside the book text"
        }
        require(readingProgressOffset in 0..bookText.length) {
            "Reading progress must be inside the book text"
        }
    }
}
