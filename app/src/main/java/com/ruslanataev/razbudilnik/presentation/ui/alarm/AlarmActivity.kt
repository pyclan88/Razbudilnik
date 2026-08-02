package com.ruslanataev.razbudilnik.presentation.ui.alarm

import android.app.PictureInPictureParams
import android.os.Bundle
import android.util.Rational
import android.view.KeyEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.ruslanataev.razbudilnik.presentation.ui.reader.ReaderRoute
import com.ruslanataev.razbudilnik.presentation.ui.theme.RazbudilnikTheme
import com.ruslanataev.razbudilnik.runtime.alarm.AlarmRingingService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

private val ALERT_NOTIFICATION_DISPLAY_DURATION = 2.seconds
private val ALARM_PIP_ASPECT_RATIO = Rational(9, 16)

@AndroidEntryPoint
class AlarmActivity : ComponentActivity() {

    private var isAlarmStopping: Boolean = false

    private var notificationTransitionJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prepareAlarmWindow()

        preparePictureInPicture()

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

    override fun onStart() {
        super.onStart()

        notificationTransitionJob?.cancel()

        notificationTransitionJob = lifecycleScope.launch {
            delay(ALERT_NOTIFICATION_DISPLAY_DURATION)

            startService(AlarmRingingService.createAlarmUiVisibleIntent(this@AlarmActivity))
        }
    }

    override fun onPause() {
        if (!isAlarmStopping) {
            setAlarmMuted(false)
        }

        super.onPause()
    }

    override fun onStop() {
        notificationTransitionJob?.cancel()
        notificationTransitionJob = null

        super.onStop()
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

    private fun preparePictureInPicture() {
        val params = PictureInPictureParams.Builder()
            .setAspectRatio(ALARM_PIP_ASPECT_RATIO)
            .setAutoEnterEnabled(true)
            .setSeamlessResizeEnabled(false)
            .build()

        setPictureInPictureParams(params)
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
