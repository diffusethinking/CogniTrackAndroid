package com.diffusethinking.cognitrack.ui.screens

import android.os.SystemClock
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.diffusethinking.cognitrack.engine.PVTEngine
import com.diffusethinking.cognitrack.ui.theme.AppGreen
import com.diffusethinking.cognitrack.ui.theme.AppRed
import com.diffusethinking.cognitrack.ui.theme.AppYellow
import com.diffusethinking.cognitrack.ui.theme.ResponseBoxDimFill
import com.diffusethinking.cognitrack.ui.theme.ResponseBoxDimStroke
import com.diffusethinking.cognitrack.ui.theme.ResponseBoxLitFill
import com.diffusethinking.cognitrack.ui.theme.ResponseBoxLitStroke
import kotlinx.coroutines.delay

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PVTTestScreen(
    isBaseline: Boolean,
    baselineRT: Double,
    isGuestMode: Boolean,
    guestName: String,
    onComplete: (trials: List<Double>, startDate: Long) -> Unit,
    onCancel: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val engine = remember { PVTEngine(isBaseline = isBaseline, baselineRT = baselineRT) }
    val view = LocalView.current

    DisposableEffect(Unit) {
        onDispose { engine.tearDown() }
    }

    LaunchedEffect(engine.phase) {
        when (engine.phase) {
            is PVTEngine.Phase.Stimulus -> {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            }
            is PVTEngine.Phase.Responded -> {
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            }
            is PVTEngine.Phase.FalseStart -> {
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            }
            is PVTEngine.Phase.Complete -> {
                onComplete(engine.trialResults.toList(), engine.testStartDate)
            }
            else -> {}
        }
    }

    val boxIsLit = when (engine.phase) {
        is PVTEngine.Phase.Stimulus, is PVTEngine.Phase.Responded, is PVTEngine.Phase.Complete -> true
        else -> false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInteropFilter { event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    engine.handleTap(scope)
                    true
                } else false
            },
        contentAlignment = Alignment.Center
    ) {
        // Top bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(top = 48.dp, start = 16.dp, end = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onCancel) {
                    Icon(
                        Icons.Filled.Cancel,
                        contentDescription = "Cancel",
                        tint = Color.Gray.copy(alpha = 0.5f),
                        modifier = Modifier.size(32.dp)
                    )
                }

                if (engine.phase != PVTEngine.Phase.Ready) {
                    Text(
                        "Trial ${engine.currentTrial} of ${engine.totalTrials}",
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }

            if (isGuestMode) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .background(
                            Color.White.copy(alpha = 0.08f),
                            RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        "👤 ${guestName.ifBlank { "Guest" }}",
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Center content
        when (val phase = engine.phase) {
            is PVTEngine.Phase.Ready -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (isBaseline) {
                        Text("⚓", fontSize = 80.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    Text(
                        if (isBaseline) "Baseline Test" else "Quick Check",
                        color = Color.Gray,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "TAP TO START",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppYellow
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "${engine.totalTrials} trials",
                        color = AppYellow.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        "In each trial, tap as fast as you can\nwhen the box lights up",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = AppYellow.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center,
                        lineHeight = 26.sp
                    )
                }
            }

            is PVTEngine.Phase.Waiting -> {
                ResponseBox(isLit = false)
            }

            is PVTEngine.Phase.Stimulus -> {
                var elapsedMs by remember(engine.stimulusOnsetTime) { mutableLongStateOf(0L) }
                LaunchedEffect(engine.stimulusOnsetTime) {
                    while (true) {
                        elapsedMs = SystemClock.elapsedRealtime() - engine.stimulusOnsetTime
                        delay(16L)
                    }
                }
                Box(contentAlignment = Alignment.Center) {
                    ResponseBox(isLit = true)
                    Text(
                        "$elapsedMs",
                        fontSize = 64.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = AppYellow
                    )
                }
            }

            is PVTEngine.Phase.FalseStart -> {
                Box(contentAlignment = Alignment.Center) {
                    ResponseBox(isLit = false)
                    Text(
                        "TOO EARLY",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AppRed
                    )
                }
            }

            is PVTEngine.Phase.Responded -> {
                val isLapse = !isBaseline && engine.lapseThreshold?.let { phase.ms.toDouble() > it } == true
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(contentAlignment = Alignment.Center) {
                        ResponseBox(isLit = true)
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                "${phase.ms}",
                                fontSize = 64.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = if (isLapse) AppRed else AppGreen
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "ms",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isLapse) AppRed else AppGreen,
                                modifier = Modifier.padding(bottom = 10.dp)
                            )
                        }
                    }
                    if (isLapse) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "LAPSE",
                            fontWeight = FontWeight.ExtraBold,
                            color = AppRed,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            is PVTEngine.Phase.Complete -> {
                Box(contentAlignment = Alignment.Center) {
                    ResponseBox(isLit = true)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("✅", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Complete",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppGreen
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ResponseBox(isLit: Boolean) {
    Box(
        modifier = Modifier
            .widthIn(max = 260.dp)
            .width(260.dp)
            .height(100.dp)
            .background(
                if (isLit) ResponseBoxLitFill else ResponseBoxDimFill,
                RoundedCornerShape(16.dp)
            )
            .border(
                width = if (isLit) 1.dp else 2.dp,
                color = if (isLit) ResponseBoxLitStroke else ResponseBoxDimStroke,
                shape = RoundedCornerShape(16.dp)
            )
    )
}
