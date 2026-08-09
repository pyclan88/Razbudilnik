package com.ruslanataev.razbudilnik.data.reader.database

import androidx.room3.Database
import androidx.room3.RoomDatabase
import com.ruslanataev.razbudilnik.data.reader.database.dao.ImportedBookDao
import com.ruslanataev.razbudilnik.data.reader.database.entities.ImportedBookEntity

@Database(
    entities = [ImportedBookEntity::class],
    version = 1,
    exportSchema = true,
)
internal abstract class ReaderDatabase : RoomDatabase() {

    abstract fun importedBookDao(): ImportedBookDao
}
