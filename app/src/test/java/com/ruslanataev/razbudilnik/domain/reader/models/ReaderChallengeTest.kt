package com.ruslanataev.razbudilnik.domain.reader.models

import org.junit.Assert.assertEquals
import org.junit.Test

class ReaderChallengeTest {

    @Test
    fun `selects required pages from first page index`() {
        val book = createBook(pageCount = 7)

        val challenge = ReaderChallenge(
            book = book,
            firstPageIndex = 1,
            requiredPageCount = 5,
            currentPageIndex = 0,
        )

        val selectedPageIds = challenge.pages.map(ReaderPage::id)

        assertEquals(
            listOf(
                "page-2",
                "page-3",
                "page-4",
                "page-5",
                "page-6",
            ),
            selectedPageIds,
        )
    }

    @Test
    fun `returns remaining pages when fewer than required pages exist`() {
        val book = createBook(pageCount = 7)

        val challenge = ReaderChallenge(
            book = book,
            firstPageIndex = 5,
            requiredPageCount = 5,
            currentPageIndex = 0,
        )

        val selectedPageIds = challenge.pages.map(ReaderPage::id)

        assertEquals(
            listOf(
                "page-6",
                "page-7",
            ),
            selectedPageIds,
        )
    }

    private fun createBook(pageCount: Int): ReaderBook {
        return ReaderBook(
            id = "test-book",
            title = "Test Book",
            author = "Test Author",
            text = "Test book text",
            pages = (1..pageCount).map { pageNumber ->
                ReaderPage(
                    id = "page-$pageNumber",
                    text = "Page $pageNumber",
                )
            },
        )
    }
}
