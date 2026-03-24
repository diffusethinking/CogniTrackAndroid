package com.diffusethinking.cognitrack.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import com.diffusethinking.cognitrack.R
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.diffusethinking.cognitrack.ui.theme.AppGreen
import com.diffusethinking.cognitrack.ui.theme.AppIndigo
import com.diffusethinking.cognitrack.ui.theme.BaselineLabelColor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun HomeScreen(
    baselineRT: Double,
    baselineDate: Double,
    guestMode: Boolean,
    guestName: String,
    onGuestModeChange: (Boolean) -> Unit,
    onGuestNameChange: (String) -> Unit,
    onStartTest: (isBaseline: Boolean, isGuestMode: Boolean, guestName: String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Image(
            painter = painterResource(R.drawable.app_icon_display),
            contentDescription = "CogniTrack",
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(22.dp))
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Reaction Time Test",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        Column(
            modifier = Modifier.widthIn(max = 500.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "👤 Guest mode",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = guestMode,
                    onCheckedChange = onGuestModeChange,
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = AppGreen,
                        checkedThumbColor = Color.White
                    )
                )
            }

            if (guestMode) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Guest Name",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = Color.White.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    OutlinedTextField(
                        value = guestName,
                        onValueChange = onGuestNameChange,
                        modifier = Modifier.widthIn(max = 180.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            textAlign = TextAlign.End,
                            color = Color.White
                        ),
                        placeholder = { Text("Name", color = Color.White.copy(alpha = 0.3f)) }
                    )
                }
            }

            Button(
                onClick = { onStartTest(false, guestMode, guestName.trim()) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Quick Check", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("6 Trials", fontSize = 14.sp, color = Color.White.copy(alpha = 0.9f))
                }
            }

            Button(
                onClick = { onStartTest(true, false, "") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .alpha(if (guestMode) 0.5f else 1f),
                colors = ButtonDefaults.buttonColors(containerColor = AppIndigo),
                shape = RoundedCornerShape(12.dp),
                enabled = !guestMode
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        if (baselineRT > 0) "Update Baseline" else "Set Baseline",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text("10 Trials", fontSize = 14.sp, color = Color.White.copy(alpha = 0.9f))
                }
            }
        }

        if (baselineRT > 0) {
            Spacer(modifier = Modifier.height(24.dp))
            Column(
                modifier = Modifier
                    .widthIn(max = 500.dp)
                    .fillMaxWidth()
                    .background(
                        Color.White.copy(alpha = 0.08f),
                        RoundedCornerShape(20.dp)
                    )
                    .padding(vertical = 28.dp, horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "⚓ Baseline",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = BaselineLabelColor
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "${baselineRT.roundToInt()} ms",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                if (baselineDate > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                    Text(
                        sdf.format(Date((baselineDate * 1000).toLong())),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}
