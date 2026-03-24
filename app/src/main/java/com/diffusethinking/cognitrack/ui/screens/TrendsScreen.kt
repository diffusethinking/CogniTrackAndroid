package com.diffusethinking.cognitrack.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.diffusethinking.cognitrack.data.PerformanceLevel
import com.diffusethinking.cognitrack.data.TestRecord
import com.diffusethinking.cognitrack.ui.MainViewModel
import com.diffusethinking.cognitrack.ui.components.RecordRow
import com.diffusethinking.cognitrack.ui.theme.AppCyan
import com.diffusethinking.cognitrack.ui.theme.AppIndigo
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

private enum class TimePeriod(val label: String, val days: Int?) {
    SEVEN_DAYS("7D", 7),
    THIRTY_DAYS("30D", 30),
    NINETY_DAYS("90D", 90),
    ALL("All", null)
}

private data class DailyAggregate(
    val dateMs: Long,
    val averageRT: Double,
    val totalLapses: Int,
    val testCount: Int,
    val records: List<TestRecord>
)

private data class BaselineSegment(
    val startMs: Long,
    val endMs: Long,
    val value: Double
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrendsScreen(
    allRecords: List<TestRecord>,
    viewModel: MainViewModel
) {
    val trendRecords = allRecords.filter { !it.isGuestMode && !it.isBaseline }
    var selectedPeriod by remember { mutableStateOf(TimePeriod.THIRTY_DAYS) }
    var pinnedDay by remember { mutableStateOf<Long?>(null) }
    var selectedRecord by remember { mutableStateOf<TestRecord?>(null) }

    if (selectedRecord != null) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Quick Check", fontWeight = FontWeight.SemiBold) },
                    navigationIcon = {
                        IconButton(onClick = { selectedRecord = null }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Black)
                )
            },
            containerColor = Color.Black
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                ResultsScreenForRecord(
                    record = selectedRecord!!,
                    viewModel = viewModel,
                    onDone = { selectedRecord = null }
                )
            }
        }
        return
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Trends", fontWeight = FontWeight.SemiBold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Black)
            )
        },
        containerColor = Color.Black
    ) { padding ->
        if (trendRecords.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(48.dp)
                ) {
                    Text("📈", fontSize = 64.sp)
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        "No trend data yet",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Complete a few quick checks to start seeing trends.",
                        color = Color.White.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                }
            }
            return@Scaffold
        }

        val dailyAggregates = computeDailyAggregates(trendRecords, selectedPeriod)
        val baselineSegments = computeBaselineSegments(allRecords, dailyAggregates)
        val pinnedAggregate = pinnedDay?.let { day ->
            dailyAggregates.firstOrNull { isSameDay(it.dateMs, day) }
        }

        if (dailyAggregates.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
            ) {
                PeriodPicker(selectedPeriod) {
                    selectedPeriod = it
                    pinnedDay = null
                }
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📅", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No tests in this period",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                        Text(
                            "Try selecting a longer time range.",
                            color = Color.White.copy(alpha = 0.4f)
                        )
                    }
                }
            }
            return@Scaffold
        }

        val totalTests = dailyAggregates.sumOf { it.testCount }
        val overallAvgRT = if (totalTests > 0) {
            dailyAggregates.sumOf { it.averageRT * it.testCount } / totalTests
        } else 0.0
        val bestDailyRT = dailyAggregates.minOfOrNull { it.averageRT } ?: 0.0
        val avgLapsesPerTest = if (totalTests > 0) {
            dailyAggregates.sumOf { it.totalLapses }.toDouble() / totalTests
        } else 0.0

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            PeriodPicker(selectedPeriod) {
                selectedPeriod = it
                pinnedDay = null
            }
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TrendStatCard("Tests", "$totalTests", Modifier.weight(1f))
                TrendStatCard("Avg RT", "${overallAvgRT.roundToInt()} ms", Modifier.weight(1f))
                TrendStatCard("Best RT", "${bestDailyRT.roundToInt()} ms", Modifier.weight(1f))
                TrendStatCard("Avg Lapses", String.format("%.1f", avgLapsesPerTest), Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        "Reaction Time",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    if (pinnedAggregate != null) {
                        Column(horizontalAlignment = Alignment.End) {
                            val sdf = SimpleDateFormat("MMM d", Locale.getDefault())
                            Text(
                                sdf.format(Date(pinnedAggregate.dateMs)) +
                                        if (pinnedAggregate.testCount > 1) " · Daily Avg" else "",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.5f)
                            )
                            Text(
                                "${pinnedAggregate.averageRT.roundToInt()} ms",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = AppCyan
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .width(16.dp)
                                .height(3.dp)
                                .background(AppCyan, RoundedCornerShape(1.dp))
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            "Quick checks",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .width(16.dp)
                                .height(2.dp)
                                .background(AppIndigo.copy(alpha = 0.6f), RoundedCornerShape(1.dp))
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            "Baseline",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                RTChart(
                    dailyAggregates = dailyAggregates,
                    baselineSegments = baselineSegments,
                    pinnedDay = pinnedDay,
                    onDaySelected = { pinnedDay = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (dailyAggregates.size == 1) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.06f), RoundedCornerShape(8.dp))
                            .padding(vertical = 8.dp, horizontal = 12.dp)
                    ) {
                        Text(
                            "Complete more tests on different days to see trends.",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                            color = Color.White.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else if (pinnedAggregate == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.06f), RoundedCornerShape(8.dp))
                            .padding(vertical = 8.dp, horizontal = 12.dp)
                    ) {
                        Text(
                            "👆 Tap a dot to see that day's tests",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                            color = Color.White.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            if (pinnedAggregate != null) {
                Spacer(modifier = Modifier.height(12.dp))
                DayDrillDown(
                    aggregate = pinnedAggregate,
                    allRecords = allRecords,
                    onRecordClick = { selectedRecord = it }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PeriodPicker(selected: TimePeriod, onSelect: (TimePeriod) -> Unit) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        TimePeriod.entries.forEachIndexed { index, period ->
            SegmentedButton(
                selected = selected == period,
                onClick = { onSelect(period) },
                shape = SegmentedButtonDefaults.itemShape(index, TimePeriod.entries.size)
            ) {
                Text(period.label)
            }
        }
    }
}

@Composable
private fun TrendStatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
            .padding(vertical = 14.dp),
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
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace
            ),
            color = Color.White,
            maxLines = 1
        )
    }
}

@Composable
private fun RTChart(
    dailyAggregates: List<DailyAggregate>,
    baselineSegments: List<BaselineSegment>,
    pinnedDay: Long?,
    onDaySelected: (Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    if (dailyAggregates.isEmpty()) return

    val xMin = dailyAggregates.first().dateMs.toFloat()
    val xMax = dailyAggregates.last().dateMs.toFloat()
    val xRange = if (xMax == xMin) 1f else xMax - xMin

    val allYValues = dailyAggregates.map { it.averageRT } + baselineSegments.map { it.value }
    val yLo = allYValues.min()
    val yHi = allYValues.max()
    val ySpan = max(yHi - yLo, 20.0)
    val yPadding = ySpan * 0.3
    val yMin = max(0.0, yLo - yPadding)
    val yMax = yHi + yPadding
    val yRange = yMax - yMin

    val cyanColor = AppCyan
    val indigoColor = AppIndigo.copy(alpha = 0.6f)

    Canvas(
        modifier = modifier.pointerInput(dailyAggregates) {
            detectTapGestures { offset ->
                val w = size.width.toFloat()
                val tappedMs = xMin + (offset.x / w) * xRange
                val nearest = dailyAggregates.minByOrNull { abs(it.dateMs - tappedMs) }
                onDaySelected(nearest?.dateMs)
            }
        }
    ) {
        val w = size.width
        val h = size.height

        fun mapX(ms: Long) = if (xMax == xMin) w / 2 else ((ms - xMin) / xRange) * w
        fun mapY(v: Double) = h - ((v - yMin) / yRange * h).toFloat()

        for (seg in baselineSegments) {
            val sx = mapX(seg.startMs)
            val ex = mapX(seg.endMs)
            val sy = mapY(seg.value)
            drawLine(
                color = indigoColor,
                start = Offset(sx, sy),
                end = Offset(ex, sy),
                strokeWidth = 2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f))
            )
        }

        if (dailyAggregates.size > 1) {
            val linePath = Path().apply {
                val first = dailyAggregates.first()
                moveTo(mapX(first.dateMs), mapY(first.averageRT))
                for (i in 1 until dailyAggregates.size) {
                    val agg = dailyAggregates[i]
                    lineTo(mapX(agg.dateMs), mapY(agg.averageRT))
                }
            }
            drawPath(linePath, cyanColor, style = Stroke(width = 3f, cap = StrokeCap.Round))
        }

        for (agg in dailyAggregates) {
            val cx = mapX(agg.dateMs)
            val cy = mapY(agg.averageRT)
            val dotColor = dotColorForAggregate(agg)
            val radius = if (agg.testCount > 1) 6f else 4f
            drawCircle(dotColor, radius, Offset(cx, cy))
        }

        if (pinnedDay != null) {
            val px = mapX(pinnedDay)
            drawLine(
                color = Color.White.copy(alpha = 0.3f),
                start = Offset(px, 0f),
                end = Offset(px, h),
                strokeWidth = 1f
            )
        }

        val textPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.argb(128, 255, 255, 255)
            textSize = 24f
            isAntiAlias = true
        }
        val sdf = SimpleDateFormat("M/d", Locale.getDefault())
        val step = max(1, dailyAggregates.size / 5)
        for (i in dailyAggregates.indices step step) {
            val agg = dailyAggregates[i]
            val label = sdf.format(Date(agg.dateMs))
            val tx = mapX(agg.dateMs)
            drawContext.canvas.nativeCanvas.drawText(label, tx - 12f, h + 20f, textPaint)
        }
    }
}

@Composable
private fun DayDrillDown(
    aggregate: DailyAggregate,
    allRecords: List<TestRecord>,
    onRecordClick: (TestRecord) -> Unit
) {
    val calendar = Calendar.getInstance()
    val dayRecords = allRecords.filter { record ->
        !record.isGuestMode && isSameDay(record.startDate, aggregate.dateMs)
    }.sortedBy { it.startDate }

    Column(modifier = Modifier.fillMaxWidth()) {
        val sdf = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                sdf.format(Date(aggregate.dateMs)),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = Color.White
            )
            Row {
                if (aggregate.testCount > 1) {
                    Text(
                        "Avg ${aggregate.averageRT.roundToInt()} ms",
                        style = MaterialTheme.typography.labelSmall,
                        color = AppCyan
                    )
                    Text(" · ", color = Color.White.copy(alpha = 0.5f))
                }
                Text(
                    "${aggregate.testCount} test${if (aggregate.testCount == 1) "" else "s"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))

        dayRecords.forEachIndexed { index, record ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onRecordClick(record) }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RecordRow(record = record, modifier = Modifier.weight(1f))
                Icon(
                    Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.size(16.dp)
                )
            }
            if (index < dayRecords.size - 1) {
                HorizontalDivider(
                    modifier = Modifier.padding(start = 36.dp),
                    color = Color.White.copy(alpha = 0.08f)
                )
            }
        }
    }
}

private fun computeDailyAggregates(
    records: List<TestRecord>,
    period: TimePeriod
): List<DailyAggregate> {
    val calendar = Calendar.getInstance()
    val todayStart = run {
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        calendar.timeInMillis
    }
    val cutoff = period.days?.let {
        calendar.timeInMillis = todayStart
        calendar.add(Calendar.DAY_OF_YEAR, -it)
        calendar.timeInMillis
    }

    val filtered = if (cutoff != null) records.filter { it.startDate >= cutoff } else records

    val grouped = filtered.groupBy { record ->
        calendar.timeInMillis = record.startDate
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        calendar.timeInMillis
    }

    return grouped.map { (day, recs) ->
        DailyAggregate(
            dateMs = day,
            averageRT = recs.map { it.averageRT }.average(),
            totalLapses = recs.sumOf { it.lapseCount },
            testCount = recs.size,
            records = recs.sortedBy { it.startDate }
        )
    }.sortedBy { it.dateMs }
}

private fun computeBaselineSegments(
    allRecords: List<TestRecord>,
    dailyAggregates: List<DailyAggregate>
): List<BaselineSegment> {
    val baselineRecords = allRecords.filter { it.isBaseline }
    if (baselineRecords.isEmpty() || dailyAggregates.isEmpty()) return emptyList()

    val calendar = Calendar.getInstance()
    val grouped = baselineRecords.groupBy { record ->
        calendar.timeInMillis = record.startDate
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        calendar.timeInMillis
    }
    val dailyBaselines = grouped.map { (day, recs) ->
        day to recs.map { it.averageRT }.average()
    }.sortedBy { it.first }

    val chartStart = dailyAggregates.first().dateMs
    calendar.timeInMillis = dailyAggregates.last().dateMs
    calendar.add(Calendar.DAY_OF_YEAR, 1)
    val chartEnd = calendar.timeInMillis

    val segments = mutableListOf<BaselineSegment>()
    for ((i, entry) in dailyBaselines.withIndex()) {
        val segStart = maxOf(entry.first, chartStart)
        val segEnd = if (i + 1 < dailyBaselines.size) dailyBaselines[i + 1].first else chartEnd
        if (segEnd <= chartStart || segStart >= chartEnd) continue
        segments.add(BaselineSegment(segStart, minOf(segEnd, chartEnd), entry.second))
    }

    val priorBaseline = dailyBaselines.lastOrNull { it.first < chartStart }
    if (priorBaseline != null && (segments.isEmpty() || segments.first().startMs != chartStart)) {
        val segEnd = if (segments.isNotEmpty()) segments.first().startMs else chartEnd
        segments.add(0, BaselineSegment(chartStart, segEnd, priorBaseline.second))
    }

    return segments
}

private fun dotColorForAggregate(agg: DailyAggregate): Color {
    val baselineRT = agg.records.lastOrNull()?.baselineRT ?: 0.0
    if (baselineRT <= 0) return AppCyan
    if (agg.totalLapses > 0 || agg.averageRT > baselineRT * 1.2) return PerformanceLevel.IMPAIRED.color
    if (agg.averageRT > baselineRT * 1.05) return PerformanceLevel.SLUGGISH.color
    if (agg.averageRT <= baselineRT * 0.90) return PerformanceLevel.SUPERB.color
    return PerformanceLevel.GOOD.color
}

private fun isSameDay(ms1: Long, ms2: Long): Boolean {
    val cal1 = Calendar.getInstance().apply { timeInMillis = ms1 }
    val cal2 = Calendar.getInstance().apply { timeInMillis = ms2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}
