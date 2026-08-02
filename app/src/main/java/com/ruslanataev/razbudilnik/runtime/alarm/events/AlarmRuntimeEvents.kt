package com.ruslanataev.razbudilnik.runtime.alarm.events

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object AlarmRuntimeEvents {

    private val mutableAlarmStarted = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val alarmStarted: SharedFlow<Unit> = mutableAlarmStarted.asSharedFlow()

    fun notifyAlarmStarted() {
        mutableAlarmStarted.tryEmit(Unit)
    }
}
