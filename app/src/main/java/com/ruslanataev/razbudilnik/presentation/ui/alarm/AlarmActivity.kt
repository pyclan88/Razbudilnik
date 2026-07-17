package com.ruslanataev.razbudilnik.presentation.ui.alarm

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.ruslanataev.razbudilnik.presentation.ui.theme.RazbudilnikTheme
import com.ruslanataev.razbudilnik.runtime.alarm.AlarmReceiver
import com.ruslanataev.razbudilnik.runtime.alarm.AlarmRingingService

class AlarmActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prepareAlarmWindow()

        val hour = intent.getIntExtra(AlarmReceiver.EXTRA_HOUR, 7)
        val minute = intent.getIntExtra(AlarmReceiver.EXTRA_MINUTE, 0)

        setContent {
            RazbudilnikTheme {
                AlarmScreen(
                    time = "%02d:%02d".format(hour, minute),
                    onStopClick = ::stopAlarm,
                )
            }
        }
    }

    private fun prepareAlarmWindow() {
        setShowWhenLocked(true)
        setTurnScreenOn(true)

        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON,
        )
    }

    private fun stopAlarm() {
        startService(
            AlarmRingingService.createStopIntent(this),
        )
        finish()
    }
}
