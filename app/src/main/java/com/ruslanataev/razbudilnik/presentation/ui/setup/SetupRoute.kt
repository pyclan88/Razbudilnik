package com.ruslanataev.razbudilnik.presentation.ui.setup

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.currentStateAsState
import com.ruslanataev.razbudilnik.presentation.ui.setup.viewmodel.SetupViewModel

@Composable
fun SetupRoute(
    onOpenReader: () -> Unit,
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

    val alarmChannelSettingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) {
        if (hasAlarmChannelAccess(context)) {
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
                !hasAlarmChannelAccess(context) -> {
                    alarmChannelSettingsLauncher.launch(
                        createAlarmChannelSettingsIntent(context),
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

        if (
            state.enabled &&
            isAppResumed &&
            !hasRequiredAlarmAccess(context)
        ) {
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

                !hasAlarmChannelAccess(context) -> {
                    alarmChannelSettingsLauncher.launch(
                        createAlarmChannelSettingsIntent(context),
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
        onOpenReader = onOpenReader,
        modifier = modifier,
    )
}
