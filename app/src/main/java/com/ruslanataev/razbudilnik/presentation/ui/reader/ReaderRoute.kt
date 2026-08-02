package com.ruslanataev.razbudilnik.presentation.ui.reader

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ruslanataev.razbudilnik.presentation.ui.reader.viewmodel.ReaderViewModel
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

private val ALARM_RESUME_GRACE_PERIOD = 1.seconds

@Composable
fun ReaderRoute(
    onChallengeFinished: () -> Unit,
    modifier: Modifier = Modifier,
    onAlarmMuteChanged: (Boolean) -> Unit = {},
    viewModel: ReaderViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val currentState = state

    if (currentState == null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }

        return
    }

    LaunchedEffect(currentState.shouldMuteAlarm) {
        if (currentState.shouldMuteAlarm) {
            onAlarmMuteChanged(true)
        } else {
            delay(ALARM_RESUME_GRACE_PERIOD)
            onAlarmMuteChanged(false)
        }
    }

    val requiredMovementDistancePx = with(LocalDensity.current) { 64.dp.toPx() }

    var isFingerDown by remember { mutableStateOf(false) }
    var movementDistanceSinceLastTick by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1.seconds)

            val isFingerMovingNow = movementDistanceSinceLastTick >= requiredMovementDistancePx

            viewModel.onReadingInteractionTick(
                elapsedTime = 1.seconds,
                isFingerDown = isFingerDown,
                isFingerMoving = isFingerMovingNow,
            )

            movementDistanceSinceLastTick = 0f
        }
    }

    ReaderScreen(
        state = currentState,
        onReaderInteractionChanged = { fingerDown, movementDistancePx ->
            isFingerDown = fingerDown
            movementDistanceSinceLastTick += movementDistancePx

            val isFingerMovingEnough = movementDistanceSinceLastTick >= requiredMovementDistancePx

            viewModel.onReadingInteractionTick(
                elapsedTime = 0.seconds,
                isFingerDown = fingerDown,
                isFingerMoving = isFingerMovingEnough,
            )
        },
        onPreviousPageClick = {
            viewModel.onPreviousPageClick()
        },
        onNextPageClick = {
            viewModel.onNextPageClick()
        },
        onFinishChallengeClick = onChallengeFinished,
        modifier = modifier,
    )
}
