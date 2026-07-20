package com.ruslanataev.razbudilnik.presentation.ui.reader

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ruslanataev.razbudilnik.presentation.ui.reader.viewmodel.ReaderViewModel
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

@Composable
fun ReaderRoute(
    modifier: Modifier = Modifier,
    viewModel: ReaderViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val requiredMovementDistancePx = with(LocalDensity.current) { 64.dp.toPx() }

    var isFingerDown by remember { mutableStateOf(false) }
    var movementDistanceSinceLastTick by remember { mutableStateOf(0f) }

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
        state = state,
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
        onNextPageClick = {
            viewModel.onNextPageClick()
        },
        modifier = modifier,
    )
}
