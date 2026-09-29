package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val videoUri: String,
    val durationMs: Long = 0L,
    val trimStartMs: Long = 0L,
    val trimEndMs: Long = 0L,
    val playbackSpeed: Float = 1.0f,
    val filterName: String = "Normal",
    val aspectRatio: String = "16:9",
    val overlayText: String = "",
    val overlayColorHex: String = "#FFFFFF",
    val overlayPosition: String = "Center", // Top, Center, Bottom
    val volume: Float = 1.0f,
    val bgMusicTitle: String = "None",
    val brightness: Float = 0f,
    val contrast: Float = 1f,
    val saturation: Float = 1f,
    val lastModified: Long = System.currentTimeMillis(),
    val thumbnailColorHex: String = "#6366F1"
)
