package com.ruslanataev.razbudilnik.presentation.ui.setup

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
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

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            viewModel.onEnabledChange(true)
        }
    }

    SetupScreen(
        state = state,
        onTimeSelected = viewModel::onTimeSelected,
        onEnabledChange = { enabled ->
            when {
                !enabled -> viewModel.onEnabledChange(false)

                hasNotificationPermission(context) -> {
                    viewModel.onEnabledChange(true)
                }

                else -> {
                    notificationPermissionLauncher.launch(
                        Manifest.permission.POST_NOTIFICATIONS,
                    )
                }
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
