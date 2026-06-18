package com.ruslanataev.razbudilnik.di

import com.ruslanataev.razbudilnik.data.alarm.scheduler.AlarmSchedulerImpl
import com.ruslanataev.razbudilnik.domain.alarm.api.AlarmScheduler
import dagger.Binds
import javax.inject.Singleton

abstract class AlarmModule {

    @Binds
    @Singleton
    abstract fun bindAlarmScheduler(
        schedulerImpl: AlarmSchedulerImpl,
    ): AlarmScheduler
}