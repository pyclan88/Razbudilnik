package com.ruslanataev.razbudilnik.data.reader.repository

import android.content.Context
import com.ruslanataev.razbudilnik.data.reader.mappers.ReaderBookDtoToReaderBookMapper
import com.ruslanataev.razbudilnik.data.reader.models.ReaderBookDto
import com.ruslanataev.razbudilnik.data.reader.models.ReaderPageDto
import com.ruslanataev.razbudilnik.domain.reader.api.ReaderBookRepository
import com.ruslanataev.razbudilnik.domain.reader.models.ReaderBook
import com.ruslanataev.razbudilnik.domain.reader.models.ReaderBookIds
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ReaderBookRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : ReaderBookRepository {

    override suspend fun getBook(bookId: String): ReaderBook =
        withContext(Dispatchers.IO) {
            require(bookId == ReaderBookIds.CAUCASIAN_PRISONER) {
                "Unknown reader book ID: $bookId"
            }

            val bookText = context.assets
                .open(BOOK_ASSET_PATH)
                .bufferedReader(Charsets.UTF_8)
                .use { reader -> reader.readText() }

            val bookDto = ReaderBookDto(
                id = ReaderBookIds.CAUCASIAN_PRISONER,
                title = "Кавказский пленник",
                author = "Лев Николаевич Толстой",
                pages = createPageDtos(
                    bookId = bookId,
                    bookText = bookText,
                ),
            )

            ReaderBookDtoToReaderBookMapper.map(bookDto)
        }

    private fun createPageDtos(
        bookId: String,
        bookText: String,
    ): List<ReaderPageDto> {
        return splitIntoPageTexts(bookText).mapIndexed { index, pageText ->
            val pageNumber = (index + 1).toString().padStart(
                length = PAGE_NUMBER_LENGTH,
                padChar = '0',
            )

            ReaderPageDto(
                id = "$bookId-page-$pageNumber",
                text = pageText,
            )
        }
    }

    private fun splitIntoPageTexts(bookText: String): List<String> {
        require(bookText.isNotBlank()) {
            "Bundled reader book must not be empty"
        }

        val paragraphs = bookText
            .trim()
            .split(PARAGRAPH_SEPARATOR_REGEX)

        val pages = mutableListOf<String>()
        val currentPage = StringBuilder()

        paragraphs.forEach { paragraph ->
            val words = paragraph
                .trim()
                .split(WHITESPACE_REGEX)

            var isFirstWordInParagraph = true

            words.forEach { word ->
                var separator = when {
                    currentPage.isEmpty() -> ""
                    isFirstWordInParagraph -> PARAGRAPH_SEPARATOR
                    else -> " "
                }

                val wordFitsCurrentPage =
                    currentPage.length + separator.length + word.length <= MAX_PAGE_CHARACTER_COUNT

                if (!wordFitsCurrentPage && currentPage.isNotEmpty()) {
                    pages += currentPage.toString()
                    currentPage.clear()

                    separator = ""
                }

                currentPage.append(separator)
                currentPage.append(word)

                isFirstWordInParagraph = false
            }
        }

        if (currentPage.isNotEmpty()) {
            pages += currentPage.toString()
        }

        return pages
    }

    private companion object {
        const val BOOK_ASSET_PATH = "reader_books/tolstoy_caucasian_prisoner.txt"

        const val MAX_PAGE_CHARACTER_COUNT = 350
        const val PAGE_NUMBER_LENGTH = 3
        const val PARAGRAPH_SEPARATOR = "\n\n"

        val PARAGRAPH_SEPARATOR_REGEX = Regex("""\r?\n\s*\r?\n""")
        val WHITESPACE_REGEX = Regex("""\s+""")
    }
}
