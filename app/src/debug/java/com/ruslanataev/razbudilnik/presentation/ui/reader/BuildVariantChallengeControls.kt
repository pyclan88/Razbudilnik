package com.ruslanataev.razbudilnik.presentation.ui.reader

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
internal fun BuildVariantChallengeControls(
    onFinishChallenge: () -> Unit,
) {
    OutlinedButton(
        onClick = onFinishChallenge,
    ) {
        Text(
            text = "DEBUG: Finish challenge",
            color = MaterialTheme.colorScheme.error,
        )
    }
}
