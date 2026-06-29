package com.ruslanataev.razbudilnik.data.setup.mappers

import com.ruslanataev.razbudilnik.data.setup.models.AlarmSettingsDto
import com.ruslanataev.razbudilnik.domain.setup.models.AlarmSettings

object AlarmSettingsDtoToAlarmSettingsMapper {
    fun map(alarmSettingsDto: AlarmSettingsDto): AlarmSettings {
        return AlarmSettings(
            hour = alarmSettingsDto.hour,
            minute = alarmSettingsDto.minute,
            enabled = alarmSettingsDto.enabled
        )
    }
}