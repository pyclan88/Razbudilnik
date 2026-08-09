package com.ruslanataev.razbudilnik.data.reader.storage

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import javax.inject.Inject

internal class ImportedBookTextStorage @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {

    suspend fun save(bookId: String, text: String) {
        withContext(Dispatchers.IO) {
            val bookFile = getBookFile(bookId)

            bookFile.parentFile?.mkdirs()

            bookFile.writeText(
                text = text,
                charset = Charsets.UTF_8,
            )
        }
    }

    suspend fun read(bookId: String): String? {
        return withContext(Dispatchers.IO) {
            val bookFile = getBookFile(bookId)

            if (!bookFile.exists()) {
                null
            } else {
                bookFile.readText(Charsets.UTF_8)
            }
        }
    }

    suspend fun delete(bookId: String) {
        withContext(Dispatchers.IO) {
            val bookFile = getBookFile(bookId)

            if (bookFile.exists() && !bookFile.delete()) {
                throw IOException("Failed to delete book file")
            }
        }
    }

    private fun getBookFile(bookId: String): File {
        val readerBooksDirectory = File(
            context.filesDir,
            READER_BOOKS_DIRECTORY,
        )

        val importedBooksDirectory = File(
            readerBooksDirectory,
            IMPORTED_BOOKS_DIRECTORY,
        )

        return File(
            importedBooksDirectory,
            "$bookId$TEXT_FILE_EXTENSION",
        )
    }

    private companion object {
        const val READER_BOOKS_DIRECTORY = "reader_books"
        const val IMPORTED_BOOKS_DIRECTORY = "imported"
        const val TEXT_FILE_EXTENSION = ".txt"
    }
}
