package com.example.model

import android.net.Uri
import java.util.UUID

enum class MediaType {
    VIDEO,
    IMAGE
}

data class GalleryMediaItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: MediaType,
    val uri: String,
    val sizeBytes: Long = 0L,
    val durationMs: Long = 0L,
    val thumbnailUri: String? = null,
    val isSample: Boolean = false
) {
    val formattedSize: String
        get() {
            if (sizeBytes <= 0) return ""
            val kb = sizeBytes / 1024
            val mb = kb / 1024
            return if (mb > 0) "${mb} MB" else "${kb} KB"
        }
}
