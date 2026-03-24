package com.diffusethinking.cognitrack.data

import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "test_records")
@TypeConverters(Converters::class)
data class TestRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startDate: Long,
    val isBaseline: Boolean,
    val isGuestMode: Boolean,
    val guestName: String?,
    val trials: List<Double>,
    val averageRT: Double,
    val lapseCount: Int,
    val baselineRT: Double
)

val TestRecord.performanceLevel: PerformanceLevel
    get() {
        if (baselineRT <= 0) return PerformanceLevel.NO_BASELINE
        if (lapseCount > 0 || averageRT > baselineRT * 1.2) return PerformanceLevel.IMPAIRED
        if (averageRT > baselineRT * 1.05) return PerformanceLevel.SLUGGISH
        if (averageRT <= baselineRT * 0.90) return PerformanceLevel.SUPERB
        return PerformanceLevel.GOOD
    }

enum class PerformanceLevel(val label: String, val icon: String, val color: Color) {
    SUPERB("Superb", "\uD83D\uDD25", Color(0xFFAF52DE)),
    GOOD("Good", "✅", Color(0xFF34C759)),
    SLUGGISH("Sluggish", "\uD83D\uDE34", Color(0xFFFF9500)),
    IMPAIRED("Impaired", "\uD83D\uDEA8", Color(0xFFFF3B30)),
    NO_BASELINE("Great Start!", "✨", Color(0xFF30B0C7))
}
