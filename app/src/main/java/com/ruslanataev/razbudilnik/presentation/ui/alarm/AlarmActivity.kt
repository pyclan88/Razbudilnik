package com.ruslanataev.razbudilnik.presentation.ui.alarm

import android.os.Bundle
import android.view.KeyEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import com.ruslanataev.razbudilnik.presentation.ui.reader.ReaderRoute
import com.ruslanataev.razbudilnik.presentation.ui.theme.RazbudilnikTheme
import com.ruslanataev.razbudilnik.runtime.alarm.AlarmRingingService
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AlarmActivity : ComponentActivity() {

    private var isAlarmStopping: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prepareAlarmWindow()

        setContent {
            RazbudilnikTheme {
                BackHandler(enabled = true) {
                    // Only completing the reader challenge may dismiss the alarm.
                }

                ReaderRoute(
                    onChallengeFinished = ::stopAlarm,
                    onAlarmMuteChanged = ::setAlarmMuted,
                )
            }
        }
    }

    override fun onPause() {
        if (!isAlarmStopping) {
            setAlarmMuted(false)
        }

        super.onPause()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return if (keyCode.isAlarmSilencingKey()) {
            true
        } else {
            super.onKeyDown(keyCode, event)
        }
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        return if (keyCode.isAlarmSilencingKey()) {
            true
        } else {
            super.onKeyUp(keyCode, event)
        }
    }

    private fun Int.isAlarmSilencingKey(): Boolean {
        return this == KeyEvent.KEYCODE_VOLUME_DOWN ||
                this == KeyEvent.KEYCODE_VOLUME_MUTE
    }

    private fun prepareAlarmWindow() {
        setShowWhenLocked(true)
        setTurnScreenOn(true)

        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON,
        )
    }

    private fun setAlarmMuted(isMuted: Boolean) {
        val intent = if (isMuted) {
            AlarmRingingService.createMuteIntent(this)
        } else {
            AlarmRingingService.createResumeIntent(this)
        }

        startService(intent)
    }

    private fun stopAlarm() {
        isAlarmStopping = true

        startService(
            AlarmRingingService.createStopIntent(this),
        )
        finish()
    }
}
