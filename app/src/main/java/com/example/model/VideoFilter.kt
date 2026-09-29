package com.example.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorMatrix

enum class VideoFilter(
    val filterName: String,
    val description: String,
    val previewColor: Color,
    val matrix: FloatArray? = null
) {
    NORMAL(
        filterName = "Normal",
        description = "Original",
        previewColor = Color(0xFF6B7280),
        matrix = null
    ),
    CINEMATIC(
        filterName = "Cinematic",
        description = "Teal & Orange",
        previewColor = Color(0xFF0284C7),
        matrix = floatArrayOf(
            1.2f, 0.0f, 0.0f, 0.0f, 10f,
            0.0f, 1.0f, 0.0f, 0.0f, -5f,
            0.0f, 0.0f, 1.3f, 0.0f, 25f,
            0.0f, 0.0f, 0.0f, 1.0f, 0f
        )
    ),
    CYBERPUNK(
        filterName = "Cyberpunk",
        description = "Neon Magenta",
        previewColor = Color(0xFFD946EF),
        matrix = floatArrayOf(
            1.4f, 0.0f, 0.2f, 0.0f, 30f,
            0.0f, 0.8f, 0.0f, 0.0f, 0f,
            0.2f, 0.0f, 1.5f, 0.0f, 40f,
            0.0f, 0.0f, 0.0f, 1.0f, 0f
        )
    ),
    NOIR(
        filterName = "Noir",
        description = "B&W Classic",
        previewColor = Color(0xFF1F2937),
        matrix = floatArrayOf(
            0.33f, 0.59f, 0.11f, 0.0f, 0f,
            0.33f, 0.59f, 0.11f, 0.0f, 0f,
            0.33f, 0.59f, 0.11f, 0.0f, 0f,
            0.00f, 0.00f, 0.00f, 1.0f, 0f
        )
    ),
    SUNSET(
        filterName = "Sunset",
        description = "Golden Warmth",
        previewColor = Color(0xFFF59E0B),
        matrix = floatArrayOf(
            1.3f, 0.0f, 0.0f, 0.0f, 35f,
            0.0f, 1.1f, 0.0f, 0.0f, 15f,
            0.0f, 0.0f, 0.8f, 0.0f, -15f,
            0.0f, 0.0f, 0.0f, 1.0f, 0f
        )
    ),
    VIBRANT(
        filterName = "Vibrant",
        description = "Rich & Punchy",
        previewColor = Color(0xFF10B981),
        matrix = floatArrayOf(
            1.25f, 0.0f, 0.0f, 0.0f, 0f,
            0.0f, 1.25f, 0.0f, 0.0f, 0f,
            0.0f, 0.0f, 1.25f, 0.0f, 0f,
            0.0f, 0.0f, 0.0f, 1.0f, 0f
        )
    ),
    VINTAGE(
        filterName = "Vintage",
        description = "Retro 90s",
        previewColor = Color(0xFFB45309),
        matrix = floatArrayOf(
            0.9f, 0.0f, 0.0f, 0.0f, 25f,
            0.0f, 0.85f, 0.0f, 0.0f, 20f,
            0.0f, 0.0f, 0.75f, 0.0f, 10f,
            0.0f, 0.0f, 0.0f, 1.0f, 0f
        )
    ),
    SEPIA(
        filterName = "Sepia",
        description = "Antique Film",
        previewColor = Color(0xFF78350F),
        matrix = floatArrayOf(
            0.393f, 0.769f, 0.189f, 0f, 0f,
            0.349f, 0.686f, 0.168f, 0f, 0f,
            0.272f, 0.534f, 0.131f, 0f, 0f,
            0.000f, 0.000f, 0.000f, 1f, 0f
        )
    );

    fun toComposeColorMatrix(): ColorMatrix? {
        return matrix?.let { ColorMatrix(it) }
    }

    companion object {
        fun fromName(name: String): VideoFilter {
            return entries.find { it.filterName.equals(name, ignoreCase = true) } ?: NORMAL
        }
    }
}
