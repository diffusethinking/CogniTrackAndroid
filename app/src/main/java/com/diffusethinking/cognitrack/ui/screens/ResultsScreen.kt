package com.diffusethinking.cognitrack.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.diffusethinking.cognitrack.data.PerformanceLevel
import com.diffusethinking.cognitrack.data.TestRecord
import com.diffusethinking.cognitrack.ui.MainViewModel
import com.diffusethinking.cognitrack.ui.theme.AppGreen
import com.diffusethinking.cognitrack.ui.theme.AppIndigo
import com.diffusethinking.cognitrack.ui.theme.AppRed
import com.diffusethinking.cognitrack.ui.theme.AppTeal
import kotlin.math.roundToInt

@Composable
fun ResultsScreen(
    trials: List<Double>,
    isBaseline: Boolean,
    startDate: Long,
    baselineRT: Double,
    isGuestMode: Boolean,
    guestName: String,
    isLive: Boolean = false,
    viewModel: MainViewModel,
    onDone: () -> Unit,
    onRestart: (() -> Unit)? = null,
    onStartBaseline: (() -> Unit)? = null
) {
    var hasSaved by remember { mutableStateOf(false) }

    val trimmedRT = computeTrimmedRT(trials, isBaseline)
    val trimmedIndices = computeTrimmedIndices(trials, isBaseline)
    val lapseThreshold = if (!isGuestMode && !isBaseline && baselineRT > 0) baselineRT * 1.5 else null
    val includedLapseCount = lapseThreshold?.let { threshold ->
        trials.indices.count { i -> !trimmedIndices.contains(i) && trials[i] > threshold }
    } ?: 0

    val performance = computePerformance(trimmedRT, baselineRT, isBaseline, includedLapseCount)

    LaunchedEffect(Unit) {
        if (isLive && !hasSaved) {
            if (isBaseline) {
                viewModel.updateBaselineRT(trimmedRT)
                viewModel.updateBaselineDate(System.currentTimeMillis() / 1000.0)
            }
            val record = TestRecord(
                startDate = startDate,
                isBaseline = isBaseline,
                isGuestMode = isGuestMode,
                guestName = guestName.ifBlank { null },
                trials = trials,
                averageRT = trimmedRT,
                lapseCount = includedLapseCount,
                baselineRT = if (isBaseline || isGuestMode) 0.0 else baselineRT
            )
            viewModel.saveRecord(record)
            hasSaved = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.widthIn(max = 600.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isBaseline) {
                BaselineHeader(trimmedRT, isLive)
            } else if (isGuestMode) {
                GuestHeader(trimmedRT, guestName)
            } else {
                CheckHeader(trimmedRT, baselineRT, performance, onStartBaseline)
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (!isBaseline && !isGuestMode) {
                StatsRow(baselineRT, includedLapseCount)
                Spacer(modifier = Modifier.height(24.dp))
            }

            TrialBreakdown(trials, trimmedIndices, lapseThreshold, isBaseline)

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (onRestart != null) {
                    Button(
                        onClick = onRestart,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppIndigo),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Restart", fontWeight = FontWeight.Bold)
                    }
                }
                Button(
                    onClick = onDone,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Done", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ResultsScreenForRecord(
    record: TestRecord,
    viewModel: MainViewModel,
    onDone: () -> Unit
) {
    val trimmedIndices = computeTrimmedIndices(record.trials, record.isBaseline)
    val lapseThreshold = if (!record.isGuestMode && !record.isBaseline && record.baselineRT > 0)
        record.baselineRT * 1.5 else null
    val performance = computePerformance(record.averageRT, record.baselineRT, record.isBaseline, record.lapseCount)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.widthIn(max = 600.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (record.isBaseline) {
                BaselineHeader(record.averageRT, isLive = false)
            } else if (record.isGuestMode) {
                GuestHeader(record.averageRT, record.guestName ?: "")
            } else {
                CheckHeader(record.averageRT, record.baselineRT, performance, null)
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (!record.isBaseline && !record.isGuestMode) {
                StatsRow(record.baselineRT, record.lapseCount)
                Spacer(modifier = Modifier.height(24.dp))
            }

            TrialBreakdown(record.trials, trimmedIndices, lapseThreshold, record.isBaseline)

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onDone,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Done", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun BaselineHeader(average: Double, isLive: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Text("⚓", fontSize = 56.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            if (isLive) "Baseline Saved!" else "Baseline",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "${average.roundToInt()} ms",
            fontSize = 52.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            "Middle 6 of 10 trials averaged",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun GuestHeader(average: Double, name: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Text("\uD83E\uDDD1\u200D\uD83E\uDD1D\u200D\uD83E\uDDD1", fontSize = 56.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            name.ifBlank { "Guest" },
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "${average.roundToInt()} ms",
            fontSize = 52.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            "5 of 6 trials averaged (slowest excluded)",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun CheckHeader(
    average: Double,
    baselineRT: Double,
    performance: PerformanceLevel,
    onStartBaseline: (() -> Unit)?
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Text(performance.icon, fontSize = 56.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            performance.label,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = performance.color
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "${average.roundToInt()} ms",
            fontSize = 52.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            "5 of 6 trials averaged (slowest excluded)",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        if (baselineRT > 0) {
            val pct = (average / baselineRT - 1) * 100
            Text(
                String.format("%+.0f%% vs baseline", pct),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.6f)
            )
        } else if (onStartBaseline != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onStartBaseline,
                colors = ButtonDefaults.buttonColors(containerColor = AppTeal),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text(
                    "Set a baseline to start tracking changes →",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun StatsRow(baselineRT: Double, lapseCount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (baselineRT > 0) {
            StatCard(
                title = "Baseline",
                value = "${baselineRT.roundToInt()} ms",
                modifier = Modifier.weight(1f)
            )
        }
        StatCard(
            title = "Lapses",
            value = "$lapseCount",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            title,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace
            ),
            color = Color.White
        )
    }
}

@Composable
private fun TrialBreakdown(
    trials: List<Double>,
    trimmedIndices: Set<Int>,
    lapseThreshold: Double?,
    isBaseline: Boolean
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Trial Breakdown",
                style = MaterialTheme.typography.titleSmall,
                color = Color.White.copy(alpha = 0.6f)
            )
            Text(
                if (isBaseline) "Fastest 2 & slowest 2 excluded" else "Slowest 1 excluded",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.4f)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        trials.forEachIndexed { index, rt ->
            val isTrimmed = trimmedIndices.contains(index)
            val isLapse = lapseThreshold?.let { rt > it } == true

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(if (isTrimmed) 0.6f else 1f)
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Trial ${index + 1}",
                    color = Color.White.copy(alpha = if (isTrimmed) 0.4f else 0.8f)
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    "${rt.roundToInt()} ms",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = when {
                        isTrimmed -> Color.White.copy(alpha = 0.4f)
                        isLapse -> AppRed
                        else -> Color.White
                    }
                )
                if (isTrimmed) {
                    Box(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .background(Color.Gray.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            "EXCLUDED",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                } else if (isLapse) {
                    Box(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .background(AppRed.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            "LAPSE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppRed
                        )
                    }
                }
            }
            if (index < trials.size - 1) {
                HorizontalDivider(
                    modifier = Modifier.padding(start = 16.dp),
                    color = Color.White.copy(alpha = 0.1f)
                )
            }
        }
    }
}

private fun computeTrimmedRT(trials: List<Double>, isBaseline: Boolean): Double {
    if (trials.isEmpty()) return 0.0
    val avg = trials.sum() / trials.size
    if (isBaseline) {
        if (trials.size < 5) return avg
        val sorted = trials.sorted()
        val trimmed = sorted.drop(2).dropLast(2)
        return trimmed.sum() / trimmed.size
    } else {
        if (trials.size < 2) return avg
        val sorted = trials.sorted()
        val trimmed = sorted.dropLast(1)
        return trimmed.sum() / trimmed.size
    }
}

private fun computeTrimmedIndices(trials: List<Double>, isBaseline: Boolean): Set<Int> {
    if (isBaseline) {
        if (trials.size < 5) return emptySet()
        val indexed = trials.withIndex().sortedBy { it.value }
        return (indexed.take(2).map { it.index } + indexed.takeLast(2).map { it.index }).toSet()
    } else {
        if (trials.size < 2) return emptySet()
        val indexed = trials.withIndex().sortedBy { it.value }
        return indexed.takeLast(1).map { it.index }.toSet()
    }
}

private fun computePerformance(
    average: Double,
    baselineRT: Double,
    isBaseline: Boolean,
    lapseCount: Int
): PerformanceLevel {
    if (isBaseline || baselineRT <= 0) return PerformanceLevel.NO_BASELINE
    if (lapseCount > 0 || average > baselineRT * 1.2) return PerformanceLevel.IMPAIRED
    if (average > baselineRT * 1.05) return PerformanceLevel.SLUGGISH
    if (average <= baselineRT * 0.90) return PerformanceLevel.SUPERB
    return PerformanceLevel.GOOD
}
