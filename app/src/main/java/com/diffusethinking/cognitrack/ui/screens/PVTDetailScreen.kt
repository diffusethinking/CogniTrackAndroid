package com.diffusethinking.cognitrack.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.diffusethinking.cognitrack.ui.theme.AppBlue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun PVTDetailScreen(
    baselineRT: Double,
    baselineDate: Double
) {
    var showOnboarding by remember { mutableStateOf(false) }

    if (showOnboarding) {
        OnboardingScreen(isRevisit = true) { showOnboarding = false }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        SectionLabel("About")
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "This Reaction Time Test is powered by the Psychomotor Vigilance Test (PVT), " +
                    "a well-established scientific measure of sustained attention and fatigue.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            lineHeight = 22.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "When a visual cue appears on screen, you simply tap as fast as you can. " +
                    "Your reaction times and \"lapses\" (delayed responses) reveal how alert " +
                    "and focused your brain is in the moment.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            lineHeight = 22.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "By establishing a morning baseline when you are fully rested, you can use quick checks " +
                    "throughout the day to objectively measure the impact of sleep deprivation, caffeine, " +
                    "or mental fatigue on your cognitive performance.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        SectionLabel("Your Baseline")
        Spacer(modifier = Modifier.height(8.dp))

        if (baselineRT > 0) {
            DetailRow("Reaction Time", "${baselineRT.roundToInt()} ms")
            if (baselineDate > 0) {
                val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                DetailRow("Set On", sdf.format(Date((baselineDate * 1000).toLong())))
            }
            DetailRow("Lapse Threshold", "${(baselineRT * 1.5).roundToInt()} ms (1.5× baseline)")
        } else {
            Text(
                "No baseline set yet. Run a baseline test from the Home screen to establish your personal reference.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.6f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        SectionLabel("Test Parameters")
        Spacer(modifier = Modifier.height(8.dp))
        DetailRow("Baseline Trials", "10 (middle 6 averaged)")
        DetailRow("Quick Check Trials", "6 (slowest 1 excluded)")

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { showOnboarding = true },
            colors = ButtonDefaults.buttonColors(containerColor = AppBlue)
        ) {
            Text("📖 How to Use This App", fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelMedium,
        color = Color.White.copy(alpha = 0.5f),
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
    ) {
        Text(label, color = Color.White)
        Spacer(modifier = Modifier.weight(1f))
        Text(value, color = Color.White.copy(alpha = 0.6f))
    }
    HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
}
