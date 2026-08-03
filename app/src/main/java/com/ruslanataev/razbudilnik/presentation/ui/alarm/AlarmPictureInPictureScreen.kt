package com.ruslanataev.razbudilnik.presentation.ui.alarm

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ruslanataev.razbudilnik.R
import com.ruslanataev.razbudilnik.presentation.ui.reader.states.ReaderUiState
import com.ruslanataev.razbudilnik.presentation.ui.theme.RazbudilnikTheme
import kotlin.time.Duration.Companion.seconds

@Composable
fun AlarmPictureInPictureScreen(
    state: ReaderUiState,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface,
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_alarm),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = stringResource(
                        R.string.alarm_pip_page,
                        state.pageNumber,
                        state.pageCount,
                    ),
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    letterSpacing = 0.sp,
                )
            }

            Text(
                text = stringResource(
                    R.string.alarm_pip_progress_seconds,
                    state.activeReadingTime.inWholeSeconds,
                    state.requiredReadingTime.inWholeSeconds,
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium,
                letterSpacing = 0.sp,
            )

            Text(
                text = state.pageText,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 6,
                overflow = TextOverflow.Ellipsis,
                letterSpacing = 0.sp,
            )

            HorizontalDivider()

            Text(
                text = stringResource(R.string.alarm_pip_sounding),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelLarge,
                letterSpacing = 0.sp,
            )

            Text(
                text = stringResource(R.string.alarm_pip_return),
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                letterSpacing = 0.sp,
            )
        }
    }
}


@Preview(
    name = "Alarm Picture-in-Picture - English",
    locale = "en",
    showBackground = true,
    widthDp = 180,
    heightDp = 320,
)
@Preview(
    name = "Alarm Picture-in-Picture - Russian",
    locale = "ru",
    showBackground = true,
    widthDp = 180,
    heightDp = 320,
)
@Composable
private fun AlarmPictureInPictureScreenPreview() {
    RazbudilnikTheme {
        AlarmPictureInPictureScreen(
            state = ReaderUiState(
                pageText = "The morning was still dark when the first bell rang. " +
                        "He opened his eyes and tried to remember why today should be different.",
                pageNumber = 2,
                pageCount = 5,
                activeReadingTime = 6.seconds,
                requiredReadingTime = 10.seconds,
                canGoToPreviousPage = true,
                canGoToNextPage = false,
                canFinishChallenge = false,
                shouldMuteAlarm = false,
            ),
        )
    }
}
