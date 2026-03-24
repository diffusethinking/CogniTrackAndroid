package com.diffusethinking.cognitrack.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.diffusethinking.cognitrack.ui.theme.AppBlue
import com.diffusethinking.cognitrack.ui.theme.AppGreen
import com.diffusethinking.cognitrack.ui.theme.AppTeal

@Composable
fun AboutScreen() {
    val context = LocalContext.current
    val packageInfo = try {
        context.packageManager.getPackageInfo(context.packageName, 0)
    } catch (_: Exception) {
        null
    }
    val versionName = packageInfo?.versionName ?: "—"
    val versionCode = packageInfo?.longVersionCode?.toString() ?: "—"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("⚡", fontSize = 64.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "CogniTrack",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Track and stay sharp!",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        SectionTitle("App Info")
        InfoRow("Version", versionName)
        InfoRow("Build", versionCode)

        Spacer(modifier = Modifier.height(24.dp))

        SectionTitle("Connect")
        LinkRow(
            icon = "🌐",
            title = "Website",
            subtitle = "cognitrack.org",
            color = AppTeal,
            onClick = {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://cognitrack.org")))
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        SectionTitle("Disclaimer")
        Text(
            "CogniTrack is a personal wellness tool, not a medical device. " +
                    "It is not intended to diagnose, treat, or prevent any medical condition. " +
                    "If you have concerns about your cognitive health, please consult a qualified healthcare professional.",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.4f),
            lineHeight = 16.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        SectionTitle("Legal")
        LinkRow(
            icon = "🛡️",
            title = "Privacy Policy",
            subtitle = "How we handle your data",
            color = AppGreen,
            onClick = {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://cognitrack.org/privacy")))
            }
        )
        LinkRow(
            icon = "📄",
            title = "Terms of Use",
            subtitle = "Google Play Terms of Service",
            color = AppBlue,
            onClick = {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/about/play-terms/")))
            }
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            "© 2026 Diffuse Thinking LLC. All rights reserved.",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.25f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.labelMedium,
        color = Color.White.copy(alpha = 0.5f),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    )
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(label, color = Color.White)
        Spacer(modifier = Modifier.weight(1f))
        Text(value, color = Color.White.copy(alpha = 0.6f))
    }
    HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
}

@Composable
private fun LinkRow(
    icon: String,
    title: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, fontSize = 20.sp)
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, color = Color.White)
            Text(
                subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.5f)
            )
        }
    }
    HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
}
