package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.VideoEditState
import com.example.model.VideoSegment
import com.example.util.TimeFormatter

@Composable
fun TimelineView(
    state: VideoEditState,
    onSeek: (Long) -> Unit,
    onSelectSegment: (String) -> Unit,
    onTrimStartChange: (Long) -> Unit,
    onTrimEndChange: (Long) -> Unit,
    onSelectTextOverlay: (String) -> Unit,
    onOpenAutoCaptions: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val durationMs = if (state.durationMs > 0) state.durationMs else 10000L
    val density = LocalDensity.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF141418))
            .padding(vertical = 8.dp)
    ) {
        // Timecode display & Info Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(if (state.isPlaying) Color(0xFF10B981) else Color(0xFFEF4444), CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = TimeFormatter.formatMsWithMillis(state.currentPositionMs),
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = " / ${TimeFormatter.formatMs(durationMs)}",
                    color = Color(0xFF8E8E93),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Segments & Auto Captions badges
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (state.autoCaptions.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFFFE600).copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                            .clickable { onOpenAutoCaptions() }
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ClosedCaption,
                                contentDescription = "Captions",
                                tint = Color(0xFFFFE600),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${state.autoCaptions.size} Captions (${state.selectedCaptionLanguage.flag})",
                                color = Color(0xFFFFE600),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .background(Color(0xFF06B6D4).copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${state.segments.size} Clip${if (state.segments.size == 1) "" else "s"}",
                        color = Color(0xFF06B6D4),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Main Multi-Track Timeline Canvas
        val timelineHeight = if (state.autoCaptions.isNotEmpty()) 132.dp else 105.dp
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(timelineHeight)
                .padding(horizontal = 16.dp)
        ) {
            val totalWidthPx = constraints.maxWidth.toFloat()
            val safeWidth = if (totalWidthPx > 0) totalWidthPx else 1000f

            val currentFraction = (state.currentPositionMs.toFloat() / durationMs).coerceIn(0f, 1f)
            val playheadPx = currentFraction * safeWidth

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(durationMs) {
                        detectTapGestures { offset ->
                            val tappedFraction = (offset.x / safeWidth).coerceIn(0f, 1f)
                            val targetMs = (tappedFraction * durationMs).toLong()
                            onSeek(targetMs)
                        }
                    }
                    .pointerInput(durationMs) {
                        detectDragGestures { change, _ ->
                            change.consume()
                            val dragFraction = (change.position.x / safeWidth).coerceIn(0f, 1f)
                            val targetMs = (dragFraction * durationMs).toLong()
                            onSeek(targetMs)
                        }
                    }
            ) {
                // 1. Timecode Ruler on Top
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                ) {
                    val seconds = (durationMs / 1000).toInt().coerceAtLeast(1)
                    val stepSec = when {
                        seconds <= 15 -> 1
                        seconds <= 60 -> 5
                        else -> 10
                    }

                    for (sec in 0..seconds step stepSec) {
                        val frac = (sec * 1000f) / durationMs
                        val x = frac * size.width
                        drawLine(
                            color = Color(0xFF4A4A55),
                            start = Offset(x, size.height - 6f),
                            end = Offset(x, size.height),
                            strokeWidth = 2f
                        )
                    }
                }

                // 2. Video Clips & Segments Track (Height 46dp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .offset(y = 16.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1E1E26))
                ) {
                    val segmentsToRender = if (state.segments.isNotEmpty()) {
                        state.segments
                    } else {
                        listOf(VideoSegment(name = "Clip 1", startMs = 0L, endMs = durationMs))
                    }

                    segmentsToRender.forEachIndexed { index, segment ->
                        val segStartFrac = (segment.startMs.toFloat() / durationMs).coerceIn(0f, 1f)
                        val segEndFrac = (segment.endMs.toFloat() / durationMs).coerceIn(0f, 1f)
                        val segStartPx = segStartFrac * safeWidth
                        val segEndPx = segEndFrac * safeWidth
                        val segWidthPx = (segEndPx - segStartPx).coerceAtLeast(20f)

                        val segStartDp = with(density) { segStartPx.toDp() }
                        val segWidthDp = with(density) { segWidthPx.toDp() }
                        val isSelected = segment.id == state.selectedSegmentId

                        Box(
                            modifier = Modifier
                                .offset(x = segStartDp)
                                .width(segWidthDp)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    Brush.verticalGradient(
                                        if (isSelected) listOf(
                                            Color(0xFF282838),
                                            Color(0xFF323246),
                                            Color(0xFF232332)
                                        ) else listOf(
                                            Color(0xFF1C1C24),
                                            Color(0xFF242430),
                                            Color(0xFF181820)
                                        )
                                    )
                                )
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) Color(0xFF06B6D4) else Color(0xFF383848),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .clickable { onSelectSegment(segment.id) }
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = segment.name,
                                        color = if (isSelected) Color.White else Color(0xFFAAAAAA),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Text(
                                        text = TimeFormatter.formatDuration(segment.durationMs),
                                        color = if (isSelected) Color(0xFF06B6D4) else Color(0xFF7E7E88),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    Box(modifier = Modifier.size(2.dp).background(Color(0xFF555566), CircleShape))
                                    Box(modifier = Modifier.size(2.dp).background(Color(0xFF555566), CircleShape))
                                    Box(modifier = Modifier.size(2.dp).background(Color(0xFF555566), CircleShape))
                                }
                            }

                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.CenterStart)
                                        .width(10.dp)
                                        .fillMaxHeight()
                                        .background(Color(0xFF06B6D4))
                                        .pointerInput(durationMs) {
                                            detectDragGestures { change, dragAmount ->
                                                change.consume()
                                                val newPx = (segStartPx + dragAmount.x).coerceIn(0f, segEndPx - 30f)
                                                val newMs = ((newPx / safeWidth) * durationMs).toLong()
                                                onTrimStartChange(newMs)
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(modifier = Modifier.width(2.dp).height(14.dp).background(Color.Black))
                                }

                                Box(
                                    modifier = Modifier
                                        .align(Alignment.CenterEnd)
                                        .width(10.dp)
                                        .fillMaxHeight()
                                        .background(Color(0xFF06B6D4))
                                        .pointerInput(durationMs) {
                                            detectDragGestures { change, dragAmount ->
                                                change.consume()
                                                val newPx = (segEndPx + dragAmount.x).coerceIn(segStartPx + 30f, safeWidth)
                                                val newMs = ((newPx / safeWidth) * durationMs).toLong()
                                                onTrimEndChange(newMs)
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(modifier = Modifier.width(2.dp).height(14.dp).background(Color.Black))
                                }
                            }
                        }

                        if (index > 0) {
                            Box(
                                modifier = Modifier
                                    .offset(x = segStartDp - 2.dp)
                                    .width(4.dp)
                                    .fillMaxHeight()
                                    .background(Color(0xFFF59E0B)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCut,
                                    contentDescription = "Split Cut",
                                    tint = Color.Black,
                                    modifier = Modifier.size(10.dp)
                                )
                            }
                        }
                    }
                }

                // 3. Auto Captions Track (Rendered when auto captions exist)
                if (state.autoCaptions.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp)
                            .offset(y = 66.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF1E1C14))
                            .clickable { onOpenAutoCaptions() }
                    ) {
                        state.autoCaptions.forEach { phrase ->
                            val capStartFrac = (phrase.startMs.toFloat() / durationMs).coerceIn(0f, 1f)
                            val capEndFrac = (phrase.endMs.toFloat() / durationMs).coerceIn(0f, 1f)
                            val capStartPx = capStartFrac * safeWidth
                            val capWidthPx = ((capEndFrac - capStartFrac) * safeWidth).coerceAtLeast(24f)

                            val capStartDp = with(density) { capStartPx.toDp() }
                            val capWidthDp = with(density) { capWidthPx.toDp() }
                            val isActive = state.currentPositionMs in phrase.startMs..phrase.endMs

                            Box(
                                modifier = Modifier
                                    .offset(x = capStartDp)
                                    .width(capWidthDp)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isActive) Color(0xFFFFE600) else Color(0xFFCA8A04).copy(alpha = 0.5f))
                                    .border(
                                        width = if (isActive) 1.dp else 0.5.dp,
                                        color = if (isActive) Color.White else Color(0xFFA16207),
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 4.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.ClosedCaption,
                                        contentDescription = "Captions",
                                        tint = if (isActive) Color.Black else Color.White,
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = phrase.text,
                                        color = if (isActive) Color.Black else Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. Text Overlay Track
                val textTrackOffsetY = if (state.autoCaptions.isNotEmpty()) 94.dp else 66.dp
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .offset(y = textTrackOffsetY)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF191922))
                ) {
                    if (state.textOverlays.isEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.TextFields,
                                contentDescription = "Text Track",
                                tint = Color(0xFF555566),
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Text Overlays",
                                color = Color(0xFF555566),
                                fontSize = 10.sp
                            )
                        }
                    } else {
                        state.textOverlays.forEach { overlay ->
                            val textStartFrac = (overlay.startMs.toFloat() / durationMs).coerceIn(0f, 1f)
                            val effectiveEnd = if (overlay.endMs > 0) overlay.endMs else durationMs
                            val textEndFrac = (effectiveEnd.toFloat() / durationMs).coerceIn(0f, 1f)
                            val textStartPx = textStartFrac * safeWidth
                            val textWidthPx = ((textEndFrac - textStartFrac) * safeWidth).coerceAtLeast(30f)

                            val textStartDp = with(density) { textStartPx.toDp() }
                            val textWidthDp = with(density) { textWidthPx.toDp() }
                            val isSel = overlay.id == state.selectedTextOverlayId

                            Box(
                                modifier = Modifier
                                    .offset(x = textStartDp)
                                    .width(textWidthDp)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF06B6D4).copy(alpha = if (isSel) 0.85f else 0.45f))
                                    .border(
                                        width = if (isSel) 1.5.dp else 0.5.dp,
                                        color = if (isSel) Color.White else Color(0xFF0891B2),
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .clickable { onSelectTextOverlay(overlay.id) }
                                    .padding(horizontal = 6.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = overlay.text.ifBlank { "Text" },
                                    color = Color.Black,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                // 5. Scrubbable Vertical Playhead Cursor (Spans across all tracks)
                val playheadOffsetDp = with(density) { (playheadPx - 8f).toDp() }
                val playheadLineHeight = if (state.autoCaptions.isNotEmpty()) 116.dp else 90.dp
                Box(
                    modifier = Modifier
                        .offset(x = playheadOffsetDp)
                        .width(16.dp)
                        .height(playheadLineHeight),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(Color.White, RoundedCornerShape(2.dp))
                            .border(1.dp, Color(0xFF06B6D4), RoundedCornerShape(2.dp))
                    )
                    Box(
                        modifier = Modifier
                            .offset(y = 10.dp)
                            .width(2.dp)
                            .height(playheadLineHeight - 10.dp)
                            .background(Color.White)
                    )
                }
            }
        }
    }
}
