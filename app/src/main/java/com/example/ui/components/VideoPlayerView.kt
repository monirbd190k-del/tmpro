package com.example.ui.components

import android.media.MediaPlayer
import android.media.PlaybackParams
import android.net.Uri
import android.os.Build
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.VideoView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.model.VideoEditState
import kotlinx.coroutines.delay

@Composable
fun VideoPlayerView(
    state: VideoEditState,
    onDurationKnown: (Long) -> Unit,
    onPositionUpdate: (Long) -> Unit,
    onTogglePlay: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var videoViewRef by remember { mutableStateOf<VideoView?>(null) }
    var mediaPlayerRef by remember { mutableStateOf<MediaPlayer?>(null) }
    var showControlsOverlay by remember { mutableStateOf(false) }

    // Periodically update position while playing
    LaunchedEffect(state.isPlaying, state.segments, state.selectedSegmentId) {
        while (state.isPlaying) {
            videoViewRef?.let { vv ->
                val current = vv.currentPosition.toLong()
                onPositionUpdate(current)

                val activeSeg = state.selectedSegment
                if (activeSeg != null && activeSeg.endMs > 0 && current >= activeSeg.endMs) {
                    val currentIndex = state.segments.indexOf(activeSeg)
                    if (currentIndex in 0 until state.segments.lastIndex) {
                        val nextSeg = state.segments[currentIndex + 1]
                        vv.seekTo(nextSeg.startMs.toInt())
                        onPositionUpdate(nextSeg.startMs)
                    } else {
                        val firstSeg = state.segments.firstOrNull() ?: activeSeg
                        vv.seekTo(firstSeg.startMs.toInt())
                        onPositionUpdate(firstSeg.startMs)
                    }
                }
            }
            delay(100)
        }
    }

    // Seek sync when user seeks manually
    LaunchedEffect(state.currentPositionMs) {
        videoViewRef?.let { vv ->
            if (!state.isPlaying) {
                val diff = Math.abs(vv.currentPosition.toLong() - state.currentPositionMs)
                if (diff > 350) {
                    vv.seekTo(state.currentPositionMs.toInt())
                }
            }
        }
    }

    // Play/Pause sync
    LaunchedEffect(state.isPlaying) {
        videoViewRef?.let { vv ->
            if (state.isPlaying) {
                if (!vv.isPlaying) {
                    vv.start()
                }
            } else {
                if (vv.isPlaying) {
                    vv.pause()
                }
            }
        }
    }

    // Playback Speed sync
    LaunchedEffect(state.playbackSpeed) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mediaPlayerRef?.let { mp ->
                try {
                    val params = mp.playbackParams ?: PlaybackParams()
                    params.speed = state.playbackSpeed
                    mp.playbackParams = params
                } catch (e: Exception) {
                    // Ignore
                }
            }
        }
    }

    // Volume sync
    LaunchedEffect(state.volume) {
        mediaPlayerRef?.let { mp ->
            try {
                mp.setVolume(state.volume, state.volume)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    // Clean up
    DisposableEffect(state.videoUri) {
        onDispose {
            videoViewRef?.stopPlayback()
            mediaPlayerRef = null
            videoViewRef = null
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        // Video View with aspect ratio
        Box(
            modifier = Modifier
                .aspectRatio(state.aspectRatio.ratio, matchHeightConstraintsFirst = true)
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            showControlsOverlay = !showControlsOverlay
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                factory = { ctx ->
                    VideoView(ctx).apply {
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        setZOrderOnTop(false)

                        setOnPreparedListener { mp ->
                            mediaPlayerRef = mp
                            mp.isLooping = false
                            val duration = duration.toLong()
                            onDurationKnown(duration)

                            if (state.playbackSpeed != 1.0f && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                try {
                                    val params = mp.playbackParams ?: PlaybackParams()
                                    params.speed = state.playbackSpeed
                                    mp.playbackParams = params
                                } catch (e: Exception) {
                                    // Ignore
                                }
                            }

                            if (state.trimStartMs > 0) {
                                seekTo(state.trimStartMs.toInt())
                            }

                            if (state.isPlaying) {
                                start()
                            }
                        }

                        setOnCompletionListener {
                            val start = state.selectedSegment?.startMs?.toInt() ?: 0
                            seekTo(start)
                            start()
                        }

                        if (state.videoUri.isNotBlank()) {
                            setVideoURI(Uri.parse(state.videoUri))
                        }
                        videoViewRef = this
                    }
                },
                update = { vv ->
                    videoViewRef = vv
                },
                modifier = Modifier.fillMaxSize()
            )

            // Live Color Filter Matrix Overlay
            val filterMatrix = state.selectedFilter.toComposeColorMatrix()
            if (filterMatrix != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(state.selectedFilter.previewColor.copy(alpha = 0.18f))
                )
            }

            // Adjustments overlay (brightness, contrast, tint)
            if (state.brightness != 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            if (state.brightness > 0) Color.White.copy(alpha = (state.brightness * 0.3f).coerceIn(0f, 0.4f))
                            else Color.Black.copy(alpha = (-state.brightness * 0.4f).coerceIn(0f, 0.5f))
                        )
                )
            }

            // Live Text Overlays
            state.textOverlays.forEach { overlay ->
                if (overlay.isVisibleAt(state.currentPositionMs, state.durationMs)) {
                    val textColor = try {
                        Color(android.graphics.Color.parseColor(overlay.colorHex))
                    } catch (e: Exception) {
                        Color.White
                    }

                    val alignment = when (overlay.position.lowercase()) {
                        "top" -> Alignment.TopCenter
                        "center" -> Alignment.Center
                        else -> Alignment.BottomCenter
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        contentAlignment = alignment
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    color = Color.Black.copy(alpha = 0.7f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .border(
                                    width = if (overlay.id == state.selectedTextOverlayId) 1.5.dp else 0.dp,
                                    color = if (overlay.id == state.selectedTextOverlayId) Color(0xFF06B6D4) else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = overlay.text,
                                color = textColor,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Dynamic CapCut Auto Captions with Word-by-Word Animation
            val activePhrase = state.currentCaptionPhrase
            if (activePhrase != null) {
                val config = state.captionConfig
                val alignment = when (config.position.lowercase()) {
                    "top" -> Alignment.TopCenter
                    "center" -> Alignment.Center
                    else -> Alignment.BottomCenter
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 24.dp),
                    contentAlignment = alignment
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(config.bgColor.copy(alpha = config.bgOpacity))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (wordItem in activePhrase.words) {
                                val isCurrent = state.currentPositionMs in wordItem.startMs..wordItem.endMs
                                val isPast = state.currentPositionMs > wordItem.endMs

                                val wordColor = if (isCurrent) config.activeWordColor else config.textColor
                                val wordBg = if (isCurrent) config.activeWordBg else Color.Transparent
                                val scale = if (isCurrent && config.animation == com.example.model.CaptionAnimationType.WORD_BOUNCE) 1.15f else 1.0f

                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 3.dp, vertical = 2.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(wordBg)
                                        .padding(horizontal = if (wordBg != Color.Transparent) 6.dp else 0.dp, vertical = 1.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (config.strokeWidth > 0f) {
                                        Text(
                                            text = wordItem.word,
                                            color = config.strokeColor,
                                            fontSize = (config.fontSizeSp * scale).sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontFamily = config.getFontFamily(),
                                            textAlign = TextAlign.Center,
                                            style = androidx.compose.ui.text.TextStyle(
                                                drawStyle = androidx.compose.ui.graphics.drawscope.Stroke(
                                                    width = config.strokeWidth * 2f
                                                )
                                            )
                                        )
                                    }

                                    Text(
                                        text = wordItem.word,
                                        color = wordColor,
                                        fontSize = (config.fontSizeSp * scale).sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontFamily = config.getFontFamily(),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.alpha(
                                            if (config.animation == com.example.model.CaptionAnimationType.FADE_IN && !isPast && !isCurrent) 0.35f else 1f
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Central Play/Pause Overlay indicator
            AnimatedVisibility(
                visible = showControlsOverlay || !state.isPlaying,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                        .clickable { onTogglePlay() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (state.isPlaying) "Pause" else "Play",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }
    }
}
