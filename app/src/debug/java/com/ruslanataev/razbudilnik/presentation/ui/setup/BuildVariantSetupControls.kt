package com.ruslanataev.razbudilnik.presentation.ui.setup

import android.content.Context
import android.content.Intent
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.ruslanataev.razbudilnik.presentation.ui.reader.ReaderPreviewActivity
import com.ruslanataev.razbudilnik.runtime.alarm.AlarmReceiver
import java.time.LocalTime

@Composable
internal fun BuildVariantSetupControls() {
    val context = LocalContext.current

    OutlinedButton(
        onClick = {
            val intent = Intent(context, ReaderPreviewActivity::class.java)
            context.startActivity(intent)
        },
    ) {
        Text("DEBUG: Open reader")
    }

    OutlinedButton(
        onClick = {
            triggerAlarm(context)
        },
    ) {
        Text("DEBUG: Trigger alarm")
    }
}

private fun triggerAlarm(context: Context) {
    val currentTime = LocalTime.now()

    val intent = AlarmReceiver.createTriggerIntent(
        context = context,
        hour = currentTime.hour,
        minute = currentTime.minute,
    )

    context.sendBroadcast(intent)
}
