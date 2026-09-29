package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AspectRatioOption
import com.example.model.EditorTool
import com.example.model.TextOverlayItem
import com.example.model.VideoEditState
import com.example.model.VideoFilter
import com.example.model.VideoSegment
import com.example.ui.components.ExportDialog
import com.example.ui.components.TimelineView
import com.example.ui.components.VideoPlayerView
import com.example.util.TimeFormatter
import com.example.viewmodel.TMProViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    viewModel: TMProViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.editState.collectAsState()
    var showRenameDialog by remember { mutableStateOf(false) }
    var renameInput by remember { mutableStateOf(state.projectTitle) }
    var showExportModal by remember { mutableStateOf(false) }

    // Android Back button handler
    BackHandler {
        viewModel.closeEditor(saveFirst = true)
    }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        color = Color(0xFF0D0D0F)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    IconButton(
                        onClick = { viewModel.closeEditor(saveFirst = true) },
                        modifier = Modifier.testTag("editor_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                renameInput = state.projectTitle
                                showRenameDialog = true
                            }
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = state.projectTitle,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.testTag("project_title_text")
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Rename",
                            tint = Color(0xFFAAAAAA),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                // Action Controls: Undo, Redo, Save, Export
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { viewModel.undo() },
                        enabled = state.canUndo,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Undo,
                            contentDescription = "Undo",
                            tint = if (state.canUndo) Color.White else Color(0xFF4A4A50)
                        )
                    }

                    IconButton(
                        onClick = { viewModel.redo() },
                        enabled = state.canRedo,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Redo,
                            contentDescription = "Redo",
                            tint = if (state.canRedo) Color.White else Color(0xFF4A4A50)
                        )
                    }

                    IconButton(
                        onClick = { viewModel.saveCurrentProject() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Save",
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Export Button
                    Button(
                        onClick = { showExportModal = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        ),
                        contentPadding = ButtonDefaults.TextButtonContentPadding,
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("export_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = "Export",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Export",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Video Player Preview Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 12.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                VideoPlayerView(
                    state = state,
                    onDurationKnown = { viewModel.updateDuration(it) },
                    onPositionUpdate = { viewModel.updatePosition(it) },
                    onTogglePlay = { viewModel.togglePlayPause() },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Quick Split & Segments Action Strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF141419))
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Split Action Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF06B6D4).copy(alpha = 0.2f))
                        .border(1.dp, Color(0xFF06B6D4), RoundedCornerShape(8.dp))
                        .clickable { viewModel.splitAtPlayhead() }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                        .testTag("split_at_playhead_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ContentCut,
                            contentDescription = "Split",
                            tint = Color(0xFF06B6D4),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Split Clip",
                            color = Color(0xFF06B6D4),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Segments Selector Pill Row
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    state.segments.forEach { segment ->
                        val isSelected = segment.id == state.selectedSegmentId
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) Color(0xFF2E2E3A) else Color(0xFF1C1C22))
                                .border(
                                    width = if (isSelected) 1.5.dp else 0.5.dp,
                                    color = if (isSelected) Color(0xFF06B6D4) else Color(0xFF383844),
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .clickable { viewModel.selectSegment(segment.id) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = segment.name,
                                color = if (isSelected) Color.White else Color(0xFFAAAAAA),
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                // Delete segment (if > 1) or Duplicate
                if (state.segments.size > 1) {
                    IconButton(
                        onClick = { viewModel.deleteSegment(state.selectedSegmentId) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Clip",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else {
                    IconButton(
                        onClick = { viewModel.duplicateSegment(state.selectedSegmentId) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Duplicate Clip",
                            tint = Color(0xFFAAAAAA),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }

            // Playback Transport Controls Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rewind 5s
                IconButton(
                    onClick = { viewModel.seekTo(state.currentPositionMs - 5000L) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FastRewind,
                        contentDescription = "Rewind 5s",
                        tint = Color.White
                    )
                }

                // Play / Pause central button
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(Color(0xFF1E1E26), CircleShape)
                        .clickable { viewModel.togglePlayPause() }
                        .testTag("play_pause_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (state.isPlaying) "Pause" else "Play",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }

                // Forward 5s
                IconButton(
                    onClick = { viewModel.seekTo(state.currentPositionMs + 5000L) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FastForward,
                        contentDescription = "Forward 5s",
                        tint = Color.White
                    )
                }

                // Ratio chip indicator
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF222228))
                        .clickable { viewModel.setActiveTool(EditorTool.RATIO) }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = state.aspectRatio.label,
                        color = Color(0xFF06B6D4),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Visual Multi-Track Timeline View
            TimelineView(
                state = state,
                onSeek = { viewModel.seekTo(it) },
                onSelectSegment = { viewModel.selectSegment(it) },
                onTrimStartChange = { viewModel.setTrimStart(it) },
                onTrimEndChange = { viewModel.setTrimEnd(it) },
                onSelectTextOverlay = {
                    viewModel.selectTextOverlay(it)
                    viewModel.setActiveTool(EditorTool.TEXT)
                },
                onOpenAutoCaptions = { viewModel.setActiveTool(EditorTool.CAPTIONS) }
            )

            // Dynamic Tool Settings Panel
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF19191E))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                when (state.activeTool) {
                    EditorTool.TRIM -> {
                        TrimSettingsPanel(
                            state = state,
                            onTrimStart = { viewModel.setTrimStart(it) },
                            onTrimEnd = { viewModel.setTrimEnd(it) },
                            onReset = { viewModel.resetTrim() },
                            onSplitAtPlayhead = { viewModel.splitAtPlayhead() }
                        )
                    }
                    EditorTool.SPLIT -> {
                        SplitSettingsPanel(
                            state = state,
                            onSplit = { viewModel.splitAtPlayhead() },
                            onSelectSegment = { viewModel.selectSegment(it) },
                            onDeleteSegment = { viewModel.deleteSegment(it) },
                            onDuplicateSegment = { viewModel.duplicateSegment(it) }
                        )
                    }
                    EditorTool.CAPTIONS -> {
                        com.example.ui.components.AutoCaptionsPanel(
                            state = state,
                            onLanguageChange = { viewModel.setCaptionLanguage(it) },
                            onGenerate = { viewModel.generateAutoCaptions(it) },
                            onApplyTemplate = { viewModel.applyCaptionTemplate(it) },
                            onUpdateConfig = { tpl, size, textCol, actCol, actBg, strkCol, strkW, bgCol, bgOp, pos, font, anim ->
                                viewModel.updateCaptionConfig(tpl, size, textCol, actCol, actBg, strkCol, strkW, bgCol, bgOp, pos, font, anim)
                            },
                            onClearCaptions = { viewModel.clearAutoCaptions() }
                        )
                    }
                    EditorTool.SPEED -> {
                        SpeedSettingsPanel(
                            currentSpeed = state.playbackSpeed,
                            onSpeedSelected = { viewModel.setPlaybackSpeed(it) }
                        )
                    }
                    EditorTool.FILTERS -> {
                        FiltersSettingsPanel(
                            selected = state.selectedFilter,
                            onFilterSelected = { viewModel.setFilter(it) }
                        )
                    }
                    EditorTool.TEXT -> {
                        TextSettingsPanel(
                            state = state,
                            onAddText = { text, color, pos -> viewModel.addTextOverlay(text, color, pos) },
                            onUpdateText = { text, color, pos, start, end ->
                                viewModel.updateSelectedTextOverlay(text, color, pos, start, end)
                            },
                            onDeleteText = { viewModel.deleteTextOverlay(it) },
                            onSelectText = { viewModel.selectTextOverlay(it) }
                        )
                    }
                    EditorTool.AUDIO -> {
                        AudioSettingsPanel(
                            volume = state.volume,
                            currentMusic = state.bgMusicTitle,
                            onVolumeChange = { viewModel.setVolume(it) },
                            onMusicSelected = { viewModel.setBgMusic(it) }
                        )
                    }
                    EditorTool.RATIO -> {
                        RatioSettingsPanel(
                            selected = state.aspectRatio,
                            onRatioSelected = { viewModel.setAspectRatio(it) }
                        )
                    }
                    EditorTool.ADJUST -> {
                        AdjustSettingsPanel(
                            brightness = state.brightness,
                            contrast = state.contrast,
                            saturation = state.saturation,
                            onAdjust = { b, c, s -> viewModel.setAdjustments(b, c, s) }
                        )
                    }
                }
            }

            // Bottom Tool Navigation Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F0F12))
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                EditorToolItem(
                    title = "Trim",
                    icon = Icons.Default.ContentCut,
                    selected = state.activeTool == EditorTool.TRIM,
                    onClick = { viewModel.setActiveTool(EditorTool.TRIM) }
                )

                EditorToolItem(
                    title = "Split",
                    icon = Icons.Default.CallSplit,
                    selected = state.activeTool == EditorTool.SPLIT,
                    onClick = { viewModel.setActiveTool(EditorTool.SPLIT) }
                )

                EditorToolItem(
                    title = "Auto Captions",
                    icon = Icons.Default.ClosedCaption,
                    selected = state.activeTool == EditorTool.CAPTIONS,
                    onClick = { viewModel.setActiveTool(EditorTool.CAPTIONS) }
                )

                EditorToolItem(
                    title = "Text",
                    icon = Icons.Default.TextFields,
                    selected = state.activeTool == EditorTool.TEXT,
                    onClick = { viewModel.setActiveTool(EditorTool.TEXT) }
                )

                EditorToolItem(
                    title = "Speed",
                    icon = Icons.Default.Speed,
                    selected = state.activeTool == EditorTool.SPEED,
                    onClick = { viewModel.setActiveTool(EditorTool.SPEED) }
                )

                EditorToolItem(
                    title = "Filters",
                    icon = Icons.Default.ColorLens,
                    selected = state.activeTool == EditorTool.FILTERS,
                    onClick = { viewModel.setActiveTool(EditorTool.FILTERS) }
                )

                EditorToolItem(
                    title = "Audio",
                    icon = Icons.Default.VolumeUp,
                    selected = state.activeTool == EditorTool.AUDIO,
                    onClick = { viewModel.setActiveTool(EditorTool.AUDIO) }
                )

                EditorToolItem(
                    title = "Ratio",
                    icon = Icons.Default.AspectRatio,
                    selected = state.activeTool == EditorTool.RATIO,
                    onClick = { viewModel.setActiveTool(EditorTool.RATIO) }
                )

                EditorToolItem(
                    title = "Adjust",
                    icon = Icons.Default.Tune,
                    selected = state.activeTool == EditorTool.ADJUST,
                    onClick = { viewModel.setActiveTool(EditorTool.ADJUST) }
                )
            }
        }
    }

    // Rename Dialog
    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Project", color = Color.White) },
            text = {
                OutlinedTextField(
                    value = renameInput,
                    onValueChange = { renameInput = it },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF06B6D4),
                        unfocusedBorderColor = Color(0xFF4A4A50)
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (renameInput.isNotBlank()) {
                            viewModel.renameProject(renameInput.trim())
                        }
                        showRenameDialog = false
                    }
                ) {
                    Text("Rename", color = Color(0xFF06B6D4), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel", color = Color(0xFFAAAAAA))
                }
            },
            containerColor = Color(0xFF1E1E26)
        )
    }

    // Export Dialog
    if (showExportModal) {
        ExportDialog(
            state = state,
            onDismiss = {
                showExportModal = false
                viewModel.dismissExportDialog()
            },
            onStartExport = { res, fps ->
                viewModel.startExport(res, fps)
            }
        )
    }
}

@Composable
fun EditorToolItem(
    title: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    val tint by animateColorAsState(if (selected) Color.White else Color(0xFF7E7E88), label = "tint")
    val bg by animateColorAsState(if (selected) Color(0xFF2E2E38) else Color.Transparent, label = "bg")

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .testTag("tool_button_${title.lowercase()}"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = title,
            color = tint,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun TrimSettingsPanel(
    state: VideoEditState,
    onTrimStart: (Long) -> Unit,
    onTrimEnd: (Long) -> Unit,
    onReset: () -> Unit,
    onSplitAtPlayhead: () -> Unit
) {
    val activeSeg = state.selectedSegment
    val startMs = activeSeg?.startMs ?: state.trimStartMs
    val endMs = activeSeg?.endMs ?: state.trimEndMs

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "${activeSeg?.name ?: "Clip"}: ${TimeFormatter.formatMs(startMs)} - ${TimeFormatter.formatMs(endMs)}",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Trimmed Length: ${TimeFormatter.formatDuration(endMs - startMs)}",
                    color = Color(0xFF06B6D4),
                    fontSize = 11.sp
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // Micro Adjust Start: +0.5s
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF2A2A34))
                        .clickable { onTrimStart(startMs + 500L) }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("+0.5s In", color = Color(0xFFAAAAAA), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                }

                // Micro Adjust End: -0.5s
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF2A2A34))
                        .clickable { onTrimEnd(endMs - 500L) }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("-0.5s Out", color = Color(0xFFAAAAAA), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                }

                // Reset
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF2A2A34))
                        .clickable { onReset() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("Reset", color = Color(0xFFEF4444), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun SplitSettingsPanel(
    state: VideoEditState,
    onSplit: () -> Unit,
    onSelectSegment: (String) -> Unit,
    onDeleteSegment: (String) -> Unit,
    onDuplicateSegment: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Split Clip at Current Time",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Playhead: ${TimeFormatter.formatMsWithMillis(state.currentPositionMs)}",
                    color = Color(0xFFAAAAAA),
                    fontSize = 11.sp
                )
            }

            Button(
                onClick = onSplit,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF06B6D4),
                    contentColor = Color.Black
                ),
                contentPadding = ButtonDefaults.TextButtonContentPadding,
                modifier = Modifier.height(34.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CallSplit,
                    contentDescription = "Split",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Split Now", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Clips list
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            state.segments.forEach { seg ->
                val isSel = seg.id == state.selectedSegmentId
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSel) Color(0xFF2C2C38) else Color(0xFF202028))
                        .border(
                            width = if (isSel) 1.5.dp else 0.5.dp,
                            color = if (isSel) Color(0xFF06B6D4) else Color(0xFF383848),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { onSelectSegment(seg.id) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text(
                                text = seg.name,
                                color = if (isSel) Color.White else Color(0xFFAAAAAA),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = TimeFormatter.formatDuration(seg.durationMs),
                                color = if (isSel) Color(0xFF06B6D4) else Color(0xFF7E7E88),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TextSettingsPanel(
    state: VideoEditState,
    onAddText: (String, String, String) -> Unit,
    onUpdateText: (String, String, String, Long?, Long?) -> Unit,
    onDeleteText: (String) -> Unit,
    onSelectText: (String) -> Unit
) {
    val activeOverlay = state.activeTextOverlay
    var input by remember(activeOverlay?.text) { mutableStateOf(activeOverlay?.text ?: "") }
    var selectedColor by remember(activeOverlay?.colorHex) { mutableStateOf(activeOverlay?.colorHex ?: "#FFFFFF") }
    var selectedPos by remember(activeOverlay?.position) { mutableStateOf(activeOverlay?.position ?: "Bottom") }

    val colors = listOf("#FFFFFF", "#FACC15", "#06B6D4", "#EC4899", "#10B981", "#8B5CF6")
    val positions = listOf("Top", "Center", "Bottom")

    Column(modifier = Modifier.fillMaxWidth()) {
        // Text Overlays selector / tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Add Text Button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF2A2A38))
                    .border(1.dp, Color(0xFF06B6D4), RoundedCornerShape(8.dp))
                    .clickable {
                        onAddText("New Caption", selectedColor, selectedPos)
                    }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Text",
                        tint = Color(0xFF06B6D4),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("Add Overlay", color = Color(0xFF06B6D4), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            state.textOverlays.forEach { item ->
                val isSel = item.id == state.selectedTextOverlayId
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSel) Color(0xFFF59E0B).copy(alpha = 0.3f) else Color(0xFF202028))
                        .border(
                            width = if (isSel) 1.5.dp else 0.5.dp,
                            color = if (isSel) Color(0xFFF59E0B) else Color(0xFF383848),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { onSelectText(item.id) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "\"${item.text.take(12)}\"",
                        color = if (isSel) Color.White else Color(0xFFAAAAAA),
                        fontSize = 11.sp,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Text input field
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = {
                    input = it
                    onUpdateText(it, selectedColor, selectedPos, null, null)
                },
                placeholder = { Text("Enter overlay text...", color = Color(0xFF6B7280)) },
                singleLine = true,
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF06B6D4),
                    unfocusedBorderColor = Color(0xFF374151)
                )
            )

            if (activeOverlay != null) {
                IconButton(
                    onClick = { onDeleteText(activeOverlay.id) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Text",
                        tint = Color(0xFFEF4444)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Colors & Positions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Colors
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                colors.forEach { hex ->
                    val color = Color(android.graphics.Color.parseColor(hex))
                    val isSel = selectedColor.equals(hex, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (isSel) 2.dp else 0.dp,
                                color = if (isSel) Color.White else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable {
                                selectedColor = hex
                                onUpdateText(input, hex, selectedPos, null, null)
                            }
                    )
                }
            }

            // Positions
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                positions.forEach { pos ->
                    val isSel = selectedPos.equals(pos, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSel) Color(0xFF06B6D4) else Color(0xFF2A2A34))
                            .clickable {
                                selectedPos = pos
                                onUpdateText(input, selectedColor, pos, null, null)
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = pos,
                            color = if (isSel) Color.Black else Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SpeedSettingsPanel(
    currentSpeed: Float,
    onSpeedSelected: (Float) -> Unit
) {
    val speeds = listOf(0.25f, 0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f, 3.0f)
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Playback Speed", color = Color(0xFFAAAAAA), fontSize = 12.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            speeds.forEach { speed ->
                val selected = currentSpeed == speed
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selected) Color.White else Color(0xFF2A2A34))
                        .clickable { onSpeedSelected(speed) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "${speed}x",
                        color = if (selected) Color.Black else Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun FiltersSettingsPanel(
    selected: VideoFilter,
    onFilterSelected: (VideoFilter) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Color Grading & Filters", color = Color(0xFFAAAAAA), fontSize = 12.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            VideoFilter.entries.forEach { filter ->
                val isSel = selected == filter
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onFilterSelected(filter) }
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(filter.previewColor)
                            .border(
                                width = if (isSel) 2.5.dp else 0.dp,
                                color = if (isSel) Color.White else Color.Transparent,
                                shape = RoundedCornerShape(10.dp)
                            )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = filter.filterName,
                        color = if (isSel) Color.White else Color(0xFF888888),
                        fontSize = 11.sp,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
fun AudioSettingsPanel(
    volume: Float,
    currentMusic: String,
    onVolumeChange: (Float) -> Unit,
    onMusicSelected: (String) -> Unit
) {
    val musicTracks = listOf("None", "Chill Lo-Fi", "Cyberwave", "Epic Cinematic", "Acoustic Beat")

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Original Video Volume", color = Color(0xFFAAAAAA), fontSize = 12.sp)
            Text("${(volume * 100).toInt()}%", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }

        Slider(
            value = volume,
            onValueChange = onVolumeChange,
            valueRange = 0f..2.0f,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF10B981),
                activeTrackColor = Color(0xFF10B981),
                inactiveTrackColor = Color(0xFF2E2E38)
            )
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text("Soundtrack / Background Beat", color = Color(0xFFAAAAAA), fontSize = 12.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            musicTracks.forEach { track ->
                val isSel = currentMusic == track
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSel) Color(0xFF10B981) else Color(0xFF2A2A34))
                        .clickable { onMusicSelected(track) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = track,
                        color = if (isSel) Color.Black else Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun RatioSettingsPanel(
    selected: AspectRatioOption,
    onRatioSelected: (AspectRatioOption) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Canvas & Aspect Ratio", color = Color(0xFFAAAAAA), fontSize = 12.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AspectRatioOption.entries.forEach { option ->
                val isSel = selected == option
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSel) Color(0xFF06B6D4) else Color(0xFF2A2A34))
                        .clickable { onRatioSelected(option) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = option.label,
                            color = if (isSel) Color.Black else Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = option.subtitle.take(14),
                            color = if (isSel) Color.Black.copy(alpha = 0.7f) else Color(0xFFAAAAAA),
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdjustSettingsPanel(
    brightness: Float,
    contrast: Float,
    saturation: Float,
    onAdjust: (Float, Float, Float) -> Unit
) {
    var b by remember(brightness) { mutableFloatStateOf(brightness) }
    var c by remember(contrast) { mutableFloatStateOf(contrast) }
    var s by remember(saturation) { mutableFloatStateOf(saturation) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Brightness", color = Color(0xFFAAAAAA), fontSize = 11.sp)
            Text(String.format("%.1f", b), color = Color.White, fontSize = 11.sp)
        }
        Slider(
            value = b,
            onValueChange = {
                b = it
                onAdjust(b, c, s)
            },
            valueRange = -1.0f..1.0f,
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color.White,
                inactiveTrackColor = Color(0xFF2E2E38)
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Contrast", color = Color(0xFFAAAAAA), fontSize = 11.sp)
            Text(String.format("%.1f", c), color = Color.White, fontSize = 11.sp)
        }
        Slider(
            value = c,
            onValueChange = {
                c = it
                onAdjust(b, c, s)
            },
            valueRange = 0.5f..2.0f,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF06B6D4),
                activeTrackColor = Color(0xFF06B6D4),
                inactiveTrackColor = Color(0xFF2E2E38)
            )
        )
    }
}
