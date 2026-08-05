package com.ruslanataev.razbudilnik.domain.reader.models

data class ReaderBook(
    val id: String,
    val title: String,
    val author: String,
    val text: String,
    val pages: List<ReaderPage>,
)
