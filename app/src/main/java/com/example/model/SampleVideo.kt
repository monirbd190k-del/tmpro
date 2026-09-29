package com.example.model

import androidx.compose.ui.graphics.Color

data class SampleVideo(
    val id: String,
    val title: String,
    val subtitle: String,
    val durationText: String,
    val durationMs: Long,
    val videoUrl: String,
    val gradientColors: List<Color>,
    val tags: List<String>
)

object SampleVideoCatalog {
    val samples = listOf(
        SampleVideo(
            id = "sample_cyberpunk",
            title = "Neon Tokyo Lights",
            subtitle = "Cyberpunk Nightscape 4K",
            durationText = "00:15",
            durationMs = 15000L,
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
            gradientColors = listOf(Color(0xFF8B5CF6), Color(0xFF06B6D4)),
            tags = listOf("Cyberpunk", "Neon", "Cinematic")
        ),
        SampleVideo(
            id = "sample_nature",
            title = "Mountain Forest Drone",
            subtitle = "Scenic Nature Aerial View",
            durationText = "00:12",
            durationMs = 12000L,
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
            gradientColors = listOf(Color(0xFF059669), Color(0xFF10B981)),
            tags = listOf("Nature", "Drone", "Landscape")
        ),
        SampleVideo(
            id = "sample_motion",
            title = "Urban Speed Runners",
            subtitle = "High Energy Action Reel",
            durationText = "00:14",
            durationMs = 14000L,
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
            gradientColors = listOf(Color(0xFFEF4444), Color(0xFFF59E0B)),
            tags = listOf("Action", "Sports", "Vlog")
        ),
        SampleVideo(
            id = "sample_synth",
            title = "Studio Color Test Reel",
            subtitle = "TM PRO Studio Test Pattern",
            durationText = "00:10",
            durationMs = 10000L,
            videoUrl = "", // Will trigger local generator
            gradientColors = listOf(Color(0xFF6366F1), Color(0xFFEC4899)),
            tags = listOf("Studio", "Color Grid", "Offline")
        )
    )
}
