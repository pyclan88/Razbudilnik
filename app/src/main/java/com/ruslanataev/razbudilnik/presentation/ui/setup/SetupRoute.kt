package com.ruslanataev.razbudilnik.presentation.ui.setup

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.currentStateAsState
import com.ruslanataev.razbudilnik.presentation.ui.setup.viewmodel.SetupViewModel

@Composable
fun SetupRoute(
    modifier: Modifier = Modifier,
    viewModel: SetupViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val state by viewModel.state.collectAsStateWithLifecycle()
    val lifecycleState by lifecycleOwner.lifecycle.currentStateAsState()

    val fullScreenIntentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) {
        if (hasFullScreenIntentAccess(context)) {
            viewModel.onEnabledChange(true)
        }
    }

    val batteryOptimizationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) {
        if (hasBatteryOptimizationExemption(context)) {
            if (hasFullScreenIntentAccess(context)) {
                viewModel.onEnabledChange(true)
            } else {
                fullScreenIntentLauncher.launch(
                    createFullScreenIntentSettingsIntent(context),
                )
            }
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            when {
                !hasBatteryOptimizationExemption(context) -> {
                    batteryOptimizationLauncher.launch(
                        createBatteryOptimizationRequestIntent(context),
                    )
                }

                !hasFullScreenIntentAccess(context) -> {
                    fullScreenIntentLauncher.launch(
                        createFullScreenIntentSettingsIntent(context),
                    )
                }

                else -> viewModel.onEnabledChange(true)
            }
        }
    }

    LaunchedEffect(state.enabled, lifecycleState) {
        val isAppResumed = lifecycleState.isAtLeast(Lifecycle.State.RESUMED)

        val isRequiredAccessMissing = !hasNotificationPermission(context) ||
                !hasBatteryOptimizationExemption(context) ||
                !hasFullScreenIntentAccess(context)

        if (state.enabled && isAppResumed && isRequiredAccessMissing) {
            viewModel.onEnabledChange(false)
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

                !hasFullScreenIntentAccess(context) -> {
                    fullScreenIntentLauncher.launch(
                        createFullScreenIntentSettingsIntent(context),
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

@SuppressLint("BatteryLife")
private fun createBatteryOptimizationRequestIntent(context: Context): Intent {
    return Intent(
        Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
        "package:${context.packageName}".toUri(),
    )
}

private fun hasFullScreenIntentAccess(context: Context): Boolean {
    val notificationManager =
        context.getSystemService(NotificationManager::class.java)

    return notificationManager.canUseFullScreenIntent()
}

private fun createFullScreenIntentSettingsIntent(context: Context): Intent {
    return Intent(
        Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT,
        "package:${context.packageName}".toUri(),
    )
}
