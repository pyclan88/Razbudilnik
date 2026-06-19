package com.ruslanataev.razbudilnik.di

import com.ruslanataev.razbudilnik.data.alarm.scheduler.AlarmSchedulerImpl
import com.ruslanataev.razbudilnik.domain.alarm.api.AlarmScheduler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AlarmModule {

    @Binds
    @Singleton
    abstract fun bindAlarmScheduler(
        schedulerImpl: AlarmSchedulerImpl,
    ): AlarmScheduler
}