package com.example.util

import java.util.Locale

object TimeFormatter {
    fun formatMs(ms: Long): String {
        if (ms <= 0L) return "00:00"
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }

    fun formatMsWithMillis(ms: Long): String {
        if (ms <= 0L) return "00:00.0"
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        val tenths = (ms % 1000) / 100
        return String.format(Locale.getDefault(), "%02d:%02d.%d", minutes, seconds, tenths)
    }

    fun formatDuration(ms: Long): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return if (minutes > 0) {
            "${minutes}m ${seconds}s"
        } else {
            "${seconds}s"
        }
    }
}
