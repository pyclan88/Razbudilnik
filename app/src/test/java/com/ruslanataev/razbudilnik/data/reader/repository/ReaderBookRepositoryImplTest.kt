package com.ruslanataev.razbudilnik.data.reader.repository

import android.content.Context
import android.content.res.AssetManager
import com.ruslanataev.razbudilnik.domain.reader.models.ReaderBookIds
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream

class ReaderBookRepositoryImplTest {

    private val context: Context = mockk()
    private val assetManager: AssetManager = mockk()

    private lateinit var repository: ReaderBookRepositoryImpl

    @Before
    fun setUp() {
        every { context.assets } returns assetManager

        repository = ReaderBookRepositoryImpl(context)
    }

    @Test
    fun `loads bundled book and creates stable pages`() = runTest {
        val longParagraph = List(100) { "word" }.joinToString(" ")
        val bookText = "$longParagraph\n\nSecond paragraph."

        stubBookAsset(bookText)

        val book = repository.getBook(ReaderBookIds.CAUCASIAN_PRISONER)

        assertEquals(ReaderBookIds.CAUCASIAN_PRISONER, book.id)
        assertEquals("Кавказский пленник", book.title)
        assertEquals("Лев Николаевич Толстой", book.author)

        assertEquals(
            listOf(
                "tolstoy-caucasian-prisoner-page-001",
                "tolstoy-caucasian-prisoner-page-002",
            ),
            book.pages.map { page -> page.id },
        )

        assertTrue(
            book.pages.all { page ->
                page.text.length <= MAX_EXPECTED_PAGE_CHARACTER_COUNT
            },
        )

        assertTrue(book.pages.last().text.contains("Second paragraph"))

        verify(exactly = 1) {
            assetManager.open(BOOK_ASSET_PATH)
        }
    }

    @Test
    fun `rejects unknown book ID`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            runTest {
                repository.getBook("unknown-book")
            }
        }

        assertEquals(
            "Unknown reader book ID: unknown-book",
            exception.message,
        )
    }

    @Test
    fun `uses remaining page space for start of next paragraph`() = runTest {
        val chapterMarker = "I"
        val shortParagraph = "First short paragraph"
        val longParagraph = List(100) { index ->
            "word$index"
        }.joinToString(" ")

        stubBookAsset(
            "$chapterMarker\n\n$shortParagraph\n\n$longParagraph"
        )

        val book = repository.getBook(
            ReaderBookIds.CAUCASIAN_PRISONER
        )

        val firstPageText = book.pages.first().text

        assertTrue(firstPageText.contains(shortParagraph))
        assertTrue(firstPageText.contains("word0"))
    }

    private fun stubBookAsset(bookText: String) {
        every {
            assetManager.open(BOOK_ASSET_PATH)
        } answers {
            ByteArrayInputStream(bookText.toByteArray(Charsets.UTF_8))
        }
    }

    private companion object {
        const val BOOK_ASSET_PATH = "reader_books/tolstoy_caucasian_prisoner.txt"

        const val MAX_EXPECTED_PAGE_CHARACTER_COUNT = 350
    }
}