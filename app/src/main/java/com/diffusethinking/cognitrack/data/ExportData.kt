package com.diffusethinking.cognitrack.data

data class ExportData(
    val version: Int,
    val exportedAt: Long,
    val baselineRT: Double,
    val baselineDate: Double,
    val guestMode: Boolean,
    val guestName: String,
    val records: List<ExportRecord>
) {
    data class ExportRecord(
        val startDate: Long,
        val isBaseline: Boolean,
        val isGuestMode: Boolean,
        val guestName: String?,
        val trials: List<Double>,
        val averageRT: Double,
        val lapseCount: Int,
        val baselineRT: Double
    )
}
