package com.ruslanataev.razbudilnik.presentation.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ruslanataev.razbudilnik.presentation.ui.alarm.AlarmActivity
import com.ruslanataev.razbudilnik.presentation.ui.navigation.AppNavHost
import com.ruslanataev.razbudilnik.presentation.ui.theme.RazbudilnikTheme
import com.ruslanataev.razbudilnik.runtime.alarm.AlarmRingingService
import com.ruslanataev.razbudilnik.runtime.alarm.events.AlarmRuntimeEvents
import com.ruslanataev.razbudilnik.runtime.alarm.session.AlarmSessionStore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (openActiveAlarmIfNeeded()) {
            return
        }

        observeAlarmRuntimeEvents()

        enableEdgeToEdge()

        setContent {
            RazbudilnikTheme {
                AppNavHost(modifier = Modifier.fillMaxSize())
            }
        }
    }

    override fun onResume() {
        super.onResume()

        if (!isFinishing) {
            openActiveAlarmIfNeeded()
        }
    }

    private fun openActiveAlarmIfNeeded(): Boolean {
        val activeSession = AlarmSessionStore(this).getActiveSession() ?: return false

        startForegroundService(
            AlarmRingingService.createStartIntent(
                context = this,
                hour = activeSession.hour,
                minute = activeSession.minute,
            ),
        )

        openAlarmActivity()

        return true
    }

    private fun observeAlarmRuntimeEvents() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                AlarmRuntimeEvents.alarmStarted.collect {
                    openAlarmActivity()
                }
            }
        }
    }

    private fun openAlarmActivity() {
        val alarmActivityIntent = Intent(
            this,
            AlarmActivity::class.java,
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        startActivity(alarmActivityIntent)
        finish()
    }
}
