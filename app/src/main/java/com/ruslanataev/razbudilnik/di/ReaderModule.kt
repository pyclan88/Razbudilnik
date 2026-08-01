package com.ruslanataev.razbudilnik.di

import com.ruslanataev.razbudilnik.data.reader.repository.ReaderBookRepositoryImpl
import com.ruslanataev.razbudilnik.domain.reader.api.ReaderBookRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ReaderModule {

    @Binds
    @Singleton
    abstract fun bindReaderBookRepository(
        repositoryImpl: ReaderBookRepositoryImpl,
    ): ReaderBookRepository
}
