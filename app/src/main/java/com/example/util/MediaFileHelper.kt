package com.example.util

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Base64
import android.util.Log
import com.example.model.GalleryMediaItem
import com.example.model.MediaType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object MediaFileHelper {
    private const val TAG = "MediaFileHelper"

    suspend fun saveBase64Media(
        context: Context,
        fileName: String,
        mimeType: String,
        dataUrl: String
    ): GalleryMediaItem? = withContext(Dispatchers.IO) {
        try {
            val base64PrefixIndex = dataUrl.indexOf("base64,")
            val base64Data = if (base64PrefixIndex != -1) {
                dataUrl.substring(base64PrefixIndex + 7)
            } else {
                dataUrl
            }

            val decodedBytes = Base64.decode(base64Data, Base64.DEFAULT)
            val isVideo = mimeType.contains("video", ignoreCase = true) ||
                    fileName.endsWith(".mp4", ignoreCase = true) ||
                    fileName.endsWith(".mov", ignoreCase = true) ||
                    fileName.endsWith(".webm", ignoreCase = true) ||
                    fileName.endsWith(".mkv", ignoreCase = true)

            val safeName = fileName.replace("[^a-zA-Z0-9._-]".toRegex(), "_")
            val targetFile = File(context.cacheDir, "imported_${System.currentTimeMillis()}_$safeName")
            FileOutputStream(targetFile).use { fos ->
                fos.write(decodedBytes)
            }

            var durationMs = 0L
            if (isVideo) {
                try {
                    val retriever = MediaMetadataRetriever()
                    retriever.setDataSource(targetFile.absolutePath)
                    val timeStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    durationMs = timeStr?.toLongOrNull() ?: 12000L
                    retriever.release()
                } catch (e: Exception) {
                    durationMs = 12000L
                }
            }

            GalleryMediaItem(
                name = fileName,
                type = if (isVideo) MediaType.VIDEO else MediaType.IMAGE,
                uri = Uri.fromFile(targetFile).toString(),
                sizeBytes = targetFile.length(),
                durationMs = durationMs
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error saving base64 media", e)
            null
        }
    }

    suspend fun processContentUri(
        context: Context,
        uri: Uri
    ): GalleryMediaItem = withContext(Dispatchers.IO) {
        val mimeType = context.contentResolver.getType(uri) ?: ""
        val isVideo = mimeType.contains("video", ignoreCase = true) || uri.toString().contains("video", ignoreCase = true)

        var durationMs = 0L
        var name = if (isVideo) "Video_${System.currentTimeMillis() % 1000}.mp4" else "Image_${System.currentTimeMillis() % 1000}.jpg"
        var sizeBytes = 0L

        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
                if (cursor.moveToFirst()) {
                    if (nameIndex != -1) name = cursor.getString(nameIndex) ?: name
                    if (sizeIndex != -1) sizeBytes = cursor.getLong(sizeIndex)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not query openable columns", e)
        }

        val safeName = name.replace("[^a-zA-Z0-9._-]".toRegex(), "_")
        val cachedFile = File(context.cacheDir, "gallery_${System.currentTimeMillis()}_$safeName")
        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(cachedFile).use { output ->
                    input.copyTo(output)
                }
            }
            if (cachedFile.exists() && cachedFile.length() > 0) {
                sizeBytes = cachedFile.length()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not copy stream to cache", e)
        }

        val effectiveUri = if (cachedFile.exists() && cachedFile.length() > 0) {
            Uri.fromFile(cachedFile).toString()
        } else {
            uri.toString()
        }

        if (isVideo) {
            try {
                val retriever = MediaMetadataRetriever()
                if (cachedFile.exists() && cachedFile.length() > 0) {
                    retriever.setDataSource(cachedFile.absolutePath)
                } else {
                    retriever.setDataSource(context, uri)
                }
                val timeStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                durationMs = timeStr?.toLongOrNull() ?: 12000L
                retriever.release()
            } catch (e: Exception) {
                durationMs = 12000L
            }
        }

        GalleryMediaItem(
            name = name,
            type = if (isVideo) MediaType.VIDEO else MediaType.IMAGE,
            uri = effectiveUri,
            sizeBytes = sizeBytes,
            durationMs = durationMs
        )
    }
}
