package com.example.model

import java.util.UUID

data class VideoSegment(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "Clip 1",
    val startMs: Long = 0L,
    val endMs: Long = 10000L,
    val speed: Float = 1.0f
) {
    val durationMs: Long
        get() = (endMs - startMs).coerceAtLeast(0L)
}

data class TextOverlayItem(
    val id: String = UUID.randomUUID().toString(),
    val text: String = "",
    val colorHex: String = "#FFFFFF",
    val position: String = "Bottom", // "Top", "Center", "Bottom"
    val fontSizeSp: Int = 20,
    val startMs: Long = 0L,
    val endMs: Long = 0L // 0 means spans full video
) {
    fun isVisibleAt(positionMs: Long, totalDurationMs: Long): Boolean {
        if (text.isBlank()) return false
        val effectiveEnd = if (endMs > 0) endMs else totalDurationMs
        return positionMs in startMs..effectiveEnd
    }
}
