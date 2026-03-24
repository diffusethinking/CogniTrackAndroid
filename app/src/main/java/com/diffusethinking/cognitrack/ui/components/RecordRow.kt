package com.diffusethinking.cognitrack.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.diffusethinking.cognitrack.data.TestRecord
import com.diffusethinking.cognitrack.data.performanceLevel
import com.diffusethinking.cognitrack.ui.theme.AppRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun RecordRow(record: TestRecord, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = recordIcon(record),
            fontSize = 18.sp,
            modifier = Modifier.width(32.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = recordLabel(record),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = formatTime(record.startDate),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${record.averageRT.roundToInt()} ms",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            if (!record.isGuestMode && !record.isBaseline && record.lapseCount > 0) {
                Text(
                    text = "${record.lapseCount} lapse${if (record.lapseCount == 1) "" else "s"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = AppRed
                )
            }
        }
    }
}

private fun recordIcon(record: TestRecord): String {
    if (record.isBaseline) return "⚓"
    if (record.isGuestMode) return "\uD83E\uDDD1\u200D\uD83E\uDD1D\u200D\uD83E\uDDD1"
    return record.performanceLevel.icon
}

private fun recordLabel(record: TestRecord): String {
    if (record.isBaseline) return "Baseline"
    if (record.isGuestMode) return if (record.guestName.isNullOrBlank()) "Guest" else record.guestName
    return record.performanceLevel.label
}

private fun formatTime(epochMillis: Long): String {
    val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
    return sdf.format(Date(epochMillis))
}
