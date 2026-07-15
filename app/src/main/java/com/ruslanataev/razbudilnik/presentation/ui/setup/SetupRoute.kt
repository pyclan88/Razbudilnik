package com.ruslanataev.razbudilnik.presentation.ui.setup

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ruslanataev.razbudilnik.presentation.ui.setup.viewmodel.SetupViewModel

@Composable
fun SetupRoute(
    modifier: Modifier = Modifier,
    viewModel: SetupViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    val batteryOptimizationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) {
        if (hasBatteryOptimizationExemption(context)) {
            viewModel.onEnabledChange(true)
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            if (hasBatteryOptimizationExemption(context)) {
                viewModel.onEnabledChange(true)
            } else {
                batteryOptimizationLauncher.launch(
                    createBatteryOptimizationRequestIntent(context),
                )
            }
        }
    }

    SetupScreen(
        state = state,
        onTimeSelected = viewModel::onTimeSelected,
        onEnabledChange = { enabled ->
            when {
                !enabled -> viewModel.onEnabledChange(false)

                !hasNotificationPermission(context) -> {
                    notificationPermissionLauncher.launch(
                        Manifest.permission.POST_NOTIFICATIONS,
                    )
                }

                !hasBatteryOptimizationExemption(context) -> {
                    batteryOptimizationLauncher.launch(
                        createBatteryOptimizationRequestIntent(context),
                    )
                }

                else -> viewModel.onEnabledChange(true)
            }
        },
        modifier = modifier,
    )
}

private fun hasNotificationPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.POST_NOTIFICATIONS,
    ) == PackageManager.PERMISSION_GRANTED
}

private fun hasBatteryOptimizationExemption(context: Context): Boolean {
    val powerManager = context.getSystemService(PowerManager::class.java)

    return powerManager.isIgnoringBatteryOptimizations(context.packageName)
}

private fun createBatteryOptimizationRequestIntent(context: Context): Intent {
    return Intent(
        Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
        "package:${context.packageName}".toUri(),
    )
}
