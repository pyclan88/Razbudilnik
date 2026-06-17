package com.ruslanataev.razbudilnik.di

import com.ruslanataev.razbudilnik.data.setup.repository.AlarmSettingsRepositoryImpl
import com.ruslanataev.razbudilnik.domain.setup.api.AlarmSettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SetupModule {

    @Binds
    @Singleton
    abstract fun bindAlarmSettingsRepository(
        repository: AlarmSettingsRepositoryImpl
    ): AlarmSettingsRepository
}