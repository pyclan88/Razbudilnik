package com.ruslanataev.razbudilnik.data.reader.database.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import com.ruslanataev.razbudilnik.data.reader.database.entities.ImportedBookEntity
import kotlinx.coroutines.flow.Flow

@Dao
internal interface ImportedBookDao {

    @Upsert
    suspend fun upsert(importedBook: ImportedBookEntity)

    @Query("SELECT * FROM imported_books WHERE id = :bookId")
    suspend fun getById(bookId: String): ImportedBookEntity?

    @Query("SELECT * FROM imported_books ORDER BY imported_at_epoch_millis DESC")
    fun observeAll(): Flow<List<ImportedBookEntity>>

    @Query("DELETE FROM imported_books WHERE id = :bookId")
    suspend fun deleteById(bookId: String)
}
