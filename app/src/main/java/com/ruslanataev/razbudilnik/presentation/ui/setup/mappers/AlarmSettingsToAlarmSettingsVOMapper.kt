package com.ruslanataev.razbudilnik.presentation.ui.setup.mappers

import com.ruslanataev.razbudilnik.domain.setup.models.AlarmSettings
import com.ruslanataev.razbudilnik.presentation.ui.setup.models.AlarmSettingsVO

object AlarmSettingsToAlarmSettingsVOMapper {
    fun map(alarmSettings: AlarmSettings): AlarmSettingsVO {
        return AlarmSettingsVO(
            hour = alarmSettings.hour,
            minute = alarmSettings.minute,
            enabled = alarmSettings.enabled
        )
    }
}