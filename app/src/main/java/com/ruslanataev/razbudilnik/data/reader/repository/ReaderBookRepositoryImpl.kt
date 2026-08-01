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

        val pageBlocks = paragraphs.flatMap { paragraph ->
            splitParagraphIntoPageBlocks(paragraph.trim())
        }

        val pages = mutableListOf<String>()
        val currentPage = StringBuilder()

        pageBlocks.forEach { block ->
            val separatorLength = if (currentPage.isEmpty()) 0 else PARAGRAPH_SEPARATOR.length

            val blockFitsCurrentPage =
                currentPage.length + separatorLength + block.length <= MAX_PAGE_CHARACTER_COUNT

            if (!blockFitsCurrentPage && currentPage.isNotEmpty()) {
                pages += currentPage.toString()
                currentPage.clear()
            }

            if (currentPage.isNotEmpty()) {
                currentPage.append(PARAGRAPH_SEPARATOR)
            }

            currentPage.append(block)
        }

        if (currentPage.isNotEmpty()) {
            pages += currentPage.toString()
        }

        return pages
    }

    private fun splitParagraphIntoPageBlocks(paragraph: String): List<String> {
        val blocks = mutableListOf<String>()
        val currentBlock = StringBuilder()

        paragraph.split(WHITESPACE_REGEX).forEach { word ->
            val separatorLength = if (currentBlock.isEmpty()) 0 else 1
            val wordFitsCurrentBlock =
                currentBlock.length + separatorLength + word.length <= MAX_PAGE_CHARACTER_COUNT

            if (!wordFitsCurrentBlock && currentBlock.isNotEmpty()) {
                blocks += currentBlock.toString()
                currentBlock.clear()
            }

            if (currentBlock.isNotEmpty()) {
                currentBlock.append(' ')
            }

            currentBlock.append(word)
        }

        if (currentBlock.isNotEmpty()) {
            blocks += currentBlock.toString()
        }

        return blocks
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