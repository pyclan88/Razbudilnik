package com.ruslanataev.razbudilnik.data.reader.models

data class ReaderBookDto(
    val id: String,
    val title: String,
    val author: String,
    val pages: List<ReaderPageDto>,
)
