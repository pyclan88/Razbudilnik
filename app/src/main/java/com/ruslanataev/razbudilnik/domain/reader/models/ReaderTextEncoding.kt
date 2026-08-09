package com.ruslanataev.razbudilnik.domain.reader.models

enum class ReaderTextEncoding(
    val charsetName: String,
) {
    UTF_8("UTF-8"),
    UTF_16_LE("UTF-16LE"),
    UTF_16_BE("UTF-16BE"),
    WINDOWS_1251("windows-1251"),
}
