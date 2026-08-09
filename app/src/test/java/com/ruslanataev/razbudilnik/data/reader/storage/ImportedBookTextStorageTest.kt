package com.ruslanataev.razbudilnik.data.reader.storage

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ImportedBookTextStorageTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private val context: Context = mockk()

    private lateinit var storage: ImportedBookTextStorage

    @Before
    fun setUp() {
        every { context.filesDir } returns temporaryFolder.root

        storage = ImportedBookTextStorage(context)
    }

    @Test
    fun `saved text can be read`() = runTest {
        val bookId = "book-1"
        val expectedText = "Привет"

        storage.save(bookId, expectedText)
        val actualText = storage.read(bookId)

        assertEquals(expectedText, actualText)
    }

    @Test
    fun `missing book returns null`() = runTest {
        val bookId = "book-1"

        val actualText = storage.read(bookId)

        assertNull(actualText)
    }

    @Test
    fun `deleting saved book removes it`() = runTest {
        val bookId = "book-1"
        val expectedText = "Привет"
        storage.save(bookId, expectedText)

        storage.delete(bookId)

        val actualText = storage.read(bookId)
        assertNull(actualText)
    }

    @Test
    fun `deleting missing book succeeds`() = runTest {
        val bookId = "book-1"

        storage.delete(bookId)

        val actualText = storage.read(bookId)
        assertNull(actualText)
    }

    @Test
    fun `save writes text as UTF-8`() = runTest {
        val bookId = "book-1"
        val expectedText = "Привет"
        val expectedBytes = expectedText.toByteArray(Charsets.UTF_8)

        storage.save(bookId, expectedText)
        val savedFile = temporaryFolder.root
            .walkTopDown()
            .single { file -> file.isFile }
        val actualBytes = savedFile.readBytes()

        assertArrayEquals(expectedBytes, actualBytes)
    }
}
