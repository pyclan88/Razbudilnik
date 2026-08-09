package com.ruslanataev.razbudilnik.di

import android.content.Context
import androidx.room3.Room
import androidx.sqlite.driver.AndroidSQLiteDriver
import com.ruslanataev.razbudilnik.data.reader.database.ReaderDatabase
import com.ruslanataev.razbudilnik.data.reader.database.dao.ImportedBookDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object ReaderDatabaseModule {

    @Provides
    @Singleton
    fun provideReaderDatabase(@ApplicationContext context: Context): ReaderDatabase {
        return Room
            .databaseBuilder<ReaderDatabase>(
                context = context,
                name = DATABASE_NAME,
            )
            .setDriver(AndroidSQLiteDriver())
            .build()
    }

    @Provides
    fun provideImportedBookDao(database: ReaderDatabase): ImportedBookDao {
        return database.importedBookDao()
    }

    private const val DATABASE_NAME = "reader.db"
}
