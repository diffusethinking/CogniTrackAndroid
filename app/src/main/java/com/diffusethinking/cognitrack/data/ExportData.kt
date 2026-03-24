package com.diffusethinking.cognitrack.data

data class ExportData(
    val version: Int,
    val exportedAt: Double,
    val baselineRT: Double,
    val baselineDate: Double,
    val guestMode: Boolean,
    val guestName: String,
    val records: List<ExportRecord>
) {
    data class ExportRecord(
        val startDate: Double,
        val isBaseline: Boolean,
        val isGuestMode: Boolean,
        val guestName: String?,
        val trials: List<Double>,
        val averageRT: Double,
        val lapseCount: Int,
        val baselineRT: Double
    )

    companion object {
        // Swift's JSONEncoder default encodes Date as seconds since Jan 1, 2001.
        // Android uses milliseconds since Jan 1, 1970 (Unix epoch).
        // Offset between the two reference dates in seconds.
        private const val APPLE_EPOCH_OFFSET_SECONDS = 978307200L

        /**
         * Converts a date value from the export JSON to Unix epoch milliseconds.
         * Auto-detects whether the value is iOS format (seconds since 2001,
         * typically < 2 billion) or Android format (millis since 1970,
         * typically > 1 trillion).
         */
        fun toUnixMillis(value: Double): Long {
            return if (value < 1e10) {
                // iOS format: seconds since Jan 1, 2001
                ((value + APPLE_EPOCH_OFFSET_SECONDS) * 1000).toLong()
            } else {
                // Android format: already Unix epoch milliseconds
                value.toLong()
            }
        }
    }
}
