package com.ruslanataev.razbudilnik.presentation.ui.setup

import android.content.Context
import android.os.SystemClock
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.ruslanataev.razbudilnik.presentation.ui.reader.pagination.ReaderTextPaginator
import com.ruslanataev.razbudilnik.runtime.alarm.AlarmReceiver
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import java.time.LocalTime

@Composable
internal fun BuildVariantSetupControls() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val textMeasurer = rememberTextMeasurer()
    val textStyle = MaterialTheme.typography.bodyLarge
    val density = LocalDensity.current

    val windowSize = LocalWindowInfo.current.containerSize

    val readerPaddingPx = with(density) {
        48.dp.roundToPx()
    }

    val pageConstraints = Constraints(
        maxWidth = (windowSize.width - readerPaddingPx).coerceAtLeast(1),
        maxHeight = (windowSize.height - readerPaddingPx).coerceAtLeast(1),
    )

    val paginator = remember(textMeasurer) { ReaderTextPaginator(textMeasurer) }

    var benchmarkResult by remember { mutableStateOf<String?>(null) }

    var benchmarkJob by remember { mutableStateOf<Job?>(null) }

    val isBenchmarkRunning = benchmarkJob?.isActive == true

    OutlinedButton(
        onClick = {
            triggerAlarm(context)
        },
    ) {
        Text("DEBUG: Trigger alarm")
    }

    OutlinedButton(
        onClick = {
            if (isBenchmarkRunning) {
                benchmarkJob?.cancel()
                return@OutlinedButton
            }

            benchmarkJob = scope.launch {
                benchmarkResult = "Reading benchmark file..."

                try {
                    val bookText = withContext(Dispatchers.IO) {
                        context.assets
                            .open("reader_books/war_and_peace_benchmark.txt")
                            .bufferedReader(Charsets.UTF_8)
                            .use { reader -> reader.readText() }
                    }

                    var pageCount = 0
                    var pageStartOffset = 0
                    val startedAt = SystemClock.elapsedRealtime()

                    while (pageStartOffset < bookText.length) {
                        currentCoroutineContext().ensureActive()

                        val pageRange = requireNotNull(
                            paginator.calculatePageRange(
                                bookText = bookText,
                                startOffset = pageStartOffset,
                                textStyle = textStyle,
                                constraints = pageConstraints,
                            ),
                        )

                        pageCount++
                        pageStartOffset = pageRange.endOffsetExclusive

                        benchmarkResult =
                            "Pages: $pageCount\n" +
                                    "Characters: ${bookText.length}\n" +
                                    "Elapsed: ${SystemClock.elapsedRealtime() - startedAt} ms"

                        yield()
                    }
                    benchmarkResult =
                        "Pages: $pageCount\n" +
                                "Characters: ${bookText.length}\n" +
                                "Elapsed: ${SystemClock.elapsedRealtime() - startedAt} ms"
                } catch (error: CancellationException) {
                    benchmarkResult = "Benchmark cancelled"
                    throw error
                } catch (error: Exception) {
                    benchmarkResult = "Benchmark failed: ${error.message}"
                } finally {
                    benchmarkJob = null
                }
            }
        },
    ) {
        Text(
            if (isBenchmarkRunning) {
                "DEBUG: Stop benchmark"
            } else {
                "DEBUG: Benchmark pagination"
            }
        )
    }

    benchmarkResult?.let { result ->
        Text(result)
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
