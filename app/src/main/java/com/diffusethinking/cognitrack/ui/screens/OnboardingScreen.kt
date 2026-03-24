package com.diffusethinking.cognitrack.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import com.diffusethinking.cognitrack.R
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.diffusethinking.cognitrack.ui.theme.AppGreen
import com.diffusethinking.cognitrack.ui.theme.AppYellow
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    isRevisit: Boolean = false,
    onComplete: (startBaseline: Boolean) -> Unit
) {
    val pageCount = 4
    val pagerState = rememberPagerState(pageCount = { pageCount })
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> WelcomePage()
                1 -> HowItWorksPage()
                2 -> BaselinePage()
                3 -> GetStartedPage(isRevisit = isRevisit, onComplete = onComplete)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!isRevisit) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (pagerState.currentPage < pageCount - 1) {
                        TextButton(onClick = { onComplete(false) }) {
                            Text(
                                "Skip",
                                color = Color.White.copy(alpha = 0.7f),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.padding(bottom = 32.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                if (pagerState.currentPage > 0) {
                    IconButton(
                        onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } }
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Previous",
                            tint = Color.White.copy(alpha = 0.5f)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(48.dp))
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(pageCount) { index ->
                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .width(if (index == pagerState.currentPage) 20.dp else 8.dp)
                                .background(
                                    if (index == pagerState.currentPage) Color.White
                                    else Color.White.copy(alpha = 0.3f),
                                    shape = CircleShape
                                )
                        )
                    }
                }

                if (pagerState.currentPage < pageCount - 1) {
                    IconButton(
                        onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } }
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Next",
                            tint = Color.White
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(48.dp))
                }
            }
        }
    }
}

@Composable
private fun WelcomePage() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF3F268C),
                        Color(0xFF14102E)
                    ),
                    center = Offset(0.5f, 0f),
                    radius = 1200f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.widthIn(max = 600.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(R.drawable.app_icon_display),
                contentDescription = "CogniTrack",
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(26.dp))
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "CogniTrack",
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "Track and stay sharp!",
                fontSize = 20.sp,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            MiniChart(
                modifier = Modifier
                    .fillMaxWidth(0.65f)
                    .height(120.dp)
            )
        }
    }
}

@Composable
private fun HowItWorksPage() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF14511E), Color(0xFF0A1A0F)),
                    center = Offset(0.5f, 0f),
                    radius = 1200f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .padding(horizontal = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("👆", fontSize = 80.sp)
            Spacer(modifier = Modifier.height(28.dp))
            Text(
                "Tap When You See It",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "When a colored box appears, tap as fast as you can. " +
                        "Your reaction time reveals how alert and focused you are.",
                fontSize = 18.sp,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                lineHeight = 26.sp
            )
        }
    }
}

@Composable
private fun BaselinePage() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF332980), Color(0xFF0F0D29)),
                    center = Offset(0.5f, 0f),
                    radius = 1200f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .padding(horizontal = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("⚓", fontSize = 80.sp)
            Spacer(modifier = Modifier.height(28.dp))
            Text(
                "Set Your Baseline",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "A baseline captures your best rested reaction time. " +
                        "Quick checks throughout the day compare against it to show changes in alertness.",
                fontSize = 18.sp,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                lineHeight = 26.sp
            )
        }
    }
}

@Composable
private fun GetStartedPage(isRevisit: Boolean, onComplete: (Boolean) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF66470D), Color(0xFF1A1208)),
                    center = Offset(0.5f, 0f),
                    radius = 1200f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .padding(horizontal = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("✨", fontSize = 80.sp)
            Spacer(modifier = Modifier.height(28.dp))
            Text(
                "Ready to Begin?",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Start with a 10-trial baseline test to establish your personal reference point.",
                fontSize = 18.sp,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                lineHeight = 26.sp
            )
            Spacer(modifier = Modifier.height(28.dp))

            if (isRevisit) {
                Button(
                    onClick = { onComplete(false) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Done", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            } else {
                Button(
                    onClick = { onComplete(true) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Set My Baseline", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.height(12.dp))
                TextButton(onClick = { onComplete(false) }) {
                    Text(
                        "Skip for Now",
                        color = Color.White.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun MiniChart(modifier: Modifier = Modifier) {
    val points = listOf(0.65f, 0.55f, 0.60f, 0.42f, 0.48f, 0.35f, 0.38f)
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stepX = w / (points.size - 1)

        for (i in 0..3) {
            val y = h * i / 3f
            drawLine(
                color = Color.White.copy(alpha = 0.08f),
                start = Offset(0f, y),
                end = Offset(w, y),
                strokeWidth = 1f
            )
        }

        val path = Path().apply {
            moveTo(0f, h * points[0])
            for (i in 1 until points.size) {
                val x = stepX * i
                val y = h * points[i]
                val prevX = stepX * (i - 1)
                val prevY = h * points[i - 1]
                val midX = (prevX + x) / 2
                cubicTo(midX, prevY, midX, y, x, y)
            }
        }

        val fillPath = Path().apply {
            addPath(path)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }

        drawPath(
            fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF30B0C7).copy(alpha = 0.4f),
                    Color(0xFF30B0C7).copy(alpha = 0f)
                )
            )
        )

        drawPath(
            path,
            brush = Brush.horizontalGradient(
                colors = listOf(Color(0xFF30B0C7), AppGreen)
            ),
            style = Stroke(width = 3f, cap = StrokeCap.Round)
        )

        for (i in points.indices) {
            drawCircle(
                color = Color.White,
                radius = 3f,
                center = Offset(stepX * i, h * points[i])
            )
        }
    }
}
