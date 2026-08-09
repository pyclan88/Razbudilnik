package com.ruslanataev.razbudilnik.data.reader.database.entities

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "imported_books")
internal data class ImportedBookEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "author")
    val author: String?,
    @ColumnInfo(name = "imported_at_epoch_millis")
    val importedAtEpochMillis: Long,
)
