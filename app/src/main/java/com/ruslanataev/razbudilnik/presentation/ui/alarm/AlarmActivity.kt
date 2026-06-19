package com.ruslanataev.razbudilnik.presentation.ui.alarm

import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.ruslanataev.razbudilnik.runtime.alarm.AlarmReceiver
import com.ruslanataev.razbudilnik.presentation.ui.theme.RazbudilnikTheme

class AlarmActivity : ComponentActivity() {

    private var ringtone: Ringtone? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prepareAlarmWindow()
        startAlarmSound()

        val hour = intent.getIntExtra(AlarmReceiver.EXTRA_HOUR, 7)
        val minute = intent.getIntExtra(AlarmReceiver.EXTRA_MINUTE, 0)

        setContent {
            RazbudilnikTheme {
                AlarmScreen(
                    time = "%02d:%02d".format(hour, minute),
                    onStopClick = {
                        stopAlarmSound()
                        finish()
                    },
                )
            }
        }
    }

    override fun onDestroy() {
        stopAlarmSound()
        super.onDestroy()
    }

    private fun prepareAlarmWindow() {
        setShowWhenLocked(true)
        setTurnScreenOn(true)

        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON,
        )
    }

    private fun startAlarmSound() {
        if (ringtone?.isPlaying == true) {
            return
        }

        val ringtoneUri = resolveAlarmUri()

        ringtone = RingtoneManager.getRingtone(this, ringtoneUri)?.apply {
            isLooping = true

            play()
        }
    }

    private fun stopAlarmSound() {
        ringtone?.stop()
        ringtone = null
    }

    private fun resolveAlarmUri(): Uri {
        return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
    }
}