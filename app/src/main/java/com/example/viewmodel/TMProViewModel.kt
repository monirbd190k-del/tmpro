package com.example.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ProjectEntity
import com.example.data.ProjectRepository
import com.example.model.AspectRatioOption
import com.example.model.CaptionLanguage
import com.example.model.EditorTool
import com.example.model.SampleVideo
import com.example.model.TextOverlayItem
import com.example.model.VideoEditState
import com.example.model.VideoFilter
import com.example.model.VideoSegment
import com.example.util.SampleVideoGenerator
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Stack
import java.util.UUID

enum class MainTab {
    HOME,
    PROJECTS,
    SETTINGS
}

class TMProViewModel(
    private val repository: ProjectRepository
) : ViewModel() {

    // Main Navigation
    private val _currentTab = MutableStateFlow(MainTab.HOME)
    val currentTab: StateFlow<MainTab> = _currentTab.asStateFlow()

    private val _isEditorOpen = MutableStateFlow(false)
    val isEditorOpen: StateFlow<Boolean> = _isEditorOpen.asStateFlow()

    // All Saved Projects from Room
    val allProjects: StateFlow<List<ProjectEntity>> = repository.allProjects
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Current Editor State
    private val _editState = MutableStateFlow(VideoEditState())
    val editState: StateFlow<VideoEditState> = _editState.asStateFlow()

    // Imported Gallery Items & Gallery Modal
    private val _isGalleryOpen = MutableStateFlow(false)
    val isGalleryOpen: StateFlow<Boolean> = _isGalleryOpen.asStateFlow()

    private val _galleryItems = MutableStateFlow<List<com.example.model.GalleryMediaItem>>(
        listOf(
            com.example.model.GalleryMediaItem(
                name = "Tokyo_Neon_Night.mp4",
                type = com.example.model.MediaType.VIDEO,
                uri = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                sizeBytes = 15_400_000L,
                durationMs = 15000L,
                isSample = true
            ),
            com.example.model.GalleryMediaItem(
                name = "Mountain_Drone_4K.mp4",
                type = com.example.model.MediaType.VIDEO,
                uri = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
                sizeBytes = 12_800_000L,
                durationMs = 12000L,
                isSample = true
            ),
            com.example.model.GalleryMediaItem(
                name = "Action_Runners_Reel.mp4",
                type = com.example.model.MediaType.VIDEO,
                uri = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
                sizeBytes = 14_100_000L,
                durationMs = 14000L,
                isSample = true
            )
        )
    )
    val galleryItems: StateFlow<List<com.example.model.GalleryMediaItem>> = _galleryItems.asStateFlow()

    fun openGallery() {
        _isGalleryOpen.value = true
    }

    fun closeGallery() {
        _isGalleryOpen.value = false
    }

    fun addGalleryItem(item: com.example.model.GalleryMediaItem) {
        _galleryItems.update { listOf(item) + it }
    }

    fun addGalleryItems(items: List<com.example.model.GalleryMediaItem>) {
        _galleryItems.update { items + it }
    }

    // Undo / Redo history stacks
    private val undoStack = Stack<VideoEditState>()
    private val redoStack = Stack<VideoEditState>()

    // Export Job
    private var exportJob: Job? = null

    fun selectTab(tab: MainTab) {
        _currentTab.value = tab
    }

    fun openNewProjectFromUri(uri: Uri, title: String? = null, durationMs: Long? = null) {
        val projectTitle = title ?: "Project ${System.currentTimeMillis() % 1000}"
        val initialDuration = if (durationMs != null && durationMs > 0L) durationMs else 15000L
        val initialSegment = VideoSegment(
            id = UUID.randomUUID().toString(),
            name = "Clip 1",
            startMs = 0L,
            endMs = initialDuration
        )
        val initialText = TextOverlayItem(
            id = UUID.randomUUID().toString(),
            text = "TM PRO Edit",
            colorHex = "#06B6D4",
            position = "Bottom",
            startMs = 0L,
            endMs = (initialDuration / 2).coerceAtLeast(3000L)
        )

        val newState = VideoEditState(
            projectId = 0L,
            projectTitle = projectTitle,
            videoUri = uri.toString(),
            durationMs = initialDuration,
            currentPositionMs = 0L,
            isPlaying = true,
            segments = listOf(initialSegment),
            selectedSegmentId = initialSegment.id,
            textOverlays = listOf(initialText),
            selectedTextOverlayId = initialText.id,
            playbackSpeed = 1.0f,
            selectedFilter = VideoFilter.NORMAL,
            aspectRatio = AspectRatioOption.RATIO_16_9,
            activeTool = EditorTool.TRIM
        )
        undoStack.clear()
        redoStack.clear()
        _editState.value = newState
        _isEditorOpen.value = true
    }

    fun openSampleProject(sample: SampleVideo, context: Context) {
        viewModelScope.launch {
            val uri = if (sample.videoUrl.isNotBlank()) {
                Uri.parse(sample.videoUrl)
            } else {
                SampleVideoGenerator.getOrCreateSampleVideo(context)
            }
            val initialSegment = VideoSegment(
                id = UUID.randomUUID().toString(),
                name = "Clip 1",
                startMs = 0L,
                endMs = sample.durationMs
            )
            val initialText = TextOverlayItem(
                id = UUID.randomUUID().toString(),
                text = sample.title,
                colorHex = "#FFFFFF",
                position = "Bottom",
                startMs = 0L,
                endMs = (sample.durationMs / 2).coerceAtLeast(3000L)
            )

            val newState = VideoEditState(
                projectId = 0L,
                projectTitle = sample.title,
                videoUri = uri.toString(),
                durationMs = sample.durationMs,
                currentPositionMs = 0L,
                isPlaying = true,
                segments = listOf(initialSegment),
                selectedSegmentId = initialSegment.id,
                textOverlays = listOf(initialText),
                selectedTextOverlayId = initialText.id,
                playbackSpeed = 1.0f,
                selectedFilter = VideoFilter.NORMAL,
                aspectRatio = AspectRatioOption.RATIO_16_9,
                activeTool = EditorTool.TRIM
            )
            undoStack.clear()
            redoStack.clear()
            _editState.value = newState
            _isEditorOpen.value = true
        }
    }

    fun openProjectFromEntity(project: ProjectEntity) {
        val filter = VideoFilter.fromName(project.filterName)
        val ratio = AspectRatioOption.fromLabel(project.aspectRatio)
        val initialSegment = VideoSegment(
            id = UUID.randomUUID().toString(),
            name = "Clip 1",
            startMs = project.trimStartMs,
            endMs = if (project.trimEndMs > 0) project.trimEndMs else project.durationMs
        )
        val overlays = if (project.overlayText.isNotBlank()) {
            listOf(
                TextOverlayItem(
                    id = UUID.randomUUID().toString(),
                    text = project.overlayText,
                    colorHex = project.overlayColorHex,
                    position = project.overlayPosition,
                    startMs = 0L,
                    endMs = project.durationMs
                )
            )
        } else emptyList()

        val newState = VideoEditState(
            projectId = project.id,
            projectTitle = project.title,
            videoUri = project.videoUri,
            durationMs = project.durationMs,
            currentPositionMs = project.trimStartMs,
            isPlaying = false,
            segments = listOf(initialSegment),
            selectedSegmentId = initialSegment.id,
            textOverlays = overlays,
            selectedTextOverlayId = overlays.firstOrNull()?.id ?: "",
            playbackSpeed = project.playbackSpeed,
            selectedFilter = filter,
            aspectRatio = ratio,
            volume = project.volume,
            bgMusicTitle = project.bgMusicTitle,
            brightness = project.brightness,
            contrast = project.contrast,
            saturation = project.saturation
        )
        undoStack.clear()
        redoStack.clear()
        _editState.value = newState
        _isEditorOpen.value = true
    }

    fun closeEditor(saveFirst: Boolean = true) {
        if (saveFirst) {
            saveCurrentProject()
        }
        _isEditorOpen.value = false
    }

    fun updateDuration(durationMs: Long) {
        _editState.update { current ->
            val updatedSegments = if (current.segments.isEmpty()) {
                listOf(
                    VideoSegment(
                        id = UUID.randomUUID().toString(),
                        name = "Clip 1",
                        startMs = 0L,
                        endMs = durationMs
                    )
                )
            } else {
                current.segments.mapIndexed { index, seg ->
                    if (index == current.segments.lastIndex && seg.endMs > durationMs) {
                        seg.copy(endMs = durationMs)
                    } else seg
                }
            }

            current.copy(
                durationMs = durationMs,
                segments = updatedSegments,
                selectedSegmentId = current.selectedSegmentId.ifBlank { updatedSegments.first().id }
            )
        }
    }

    fun updatePosition(positionMs: Long) {
        _editState.update { current ->
            current.copy(currentPositionMs = positionMs)
        }
    }

    fun togglePlayPause() {
        _editState.update { it.copy(isPlaying = !it.isPlaying) }
    }

    fun setPlaying(playing: Boolean) {
        _editState.update { it.copy(isPlaying = playing) }
    }

    fun seekTo(positionMs: Long) {
        val totalDur = _editState.value.durationMs.coerceAtLeast(1L)
        val clamped = positionMs.coerceIn(0L, totalDur)
        _editState.update { it.copy(currentPositionMs = clamped) }
    }

    fun setActiveTool(tool: EditorTool) {
        _editState.update { it.copy(activeTool = tool) }
    }

    private fun pushUndo() {
        val current = _editState.value
        undoStack.push(current)
        redoStack.clear()
        updateUndoRedoAvailability()
    }

    private fun updateUndoRedoAvailability() {
        _editState.update {
            it.copy(
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty()
            )
        }
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            val previous = undoStack.pop()
            redoStack.push(_editState.value)
            _editState.value = previous
            updateUndoRedoAvailability()
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            val next = redoStack.pop()
            undoStack.push(_editState.value)
            _editState.value = next
            updateUndoRedoAvailability()
        }
    }

    // --- VIDEO SEGMENTS & SPLITTING ---

    fun selectSegment(segmentId: String) {
        val seg = _editState.value.segments.find { it.id == segmentId }
        _editState.update { current ->
            current.copy(selectedSegmentId = segmentId)
        }
        if (seg != null) {
            seekTo(seg.startMs)
        }
    }

    fun splitAtPlayhead() {
        val state = _editState.value
        val playhead = state.currentPositionMs
        val targetSegment = state.segments.find { playhead in (it.startMs + 400L)..(it.endMs - 400L) }
            ?: state.selectedSegment?.takeIf { playhead in (it.startMs + 400L)..(it.endMs - 400L) }

        if (targetSegment != null) {
            pushUndo()
            val index = state.segments.indexOf(targetSegment)
            val clipNum = state.segments.size + 1

            val leftSeg = targetSegment.copy(
                endMs = playhead
            )
            val rightSeg = VideoSegment(
                id = UUID.randomUUID().toString(),
                name = "Clip $clipNum",
                startMs = playhead,
                endMs = targetSegment.endMs,
                speed = targetSegment.speed
            )

            val newSegments = state.segments.toMutableList().apply {
                removeAt(index)
                add(index, leftSeg)
                add(index + 1, rightSeg)
            }

            _editState.update {
                it.copy(
                    segments = newSegments,
                    selectedSegmentId = rightSeg.id
                )
            }
        }
    }

    fun deleteSegment(segmentId: String) {
        val currentSegments = _editState.value.segments
        if (currentSegments.size > 1) {
            pushUndo()
            val newSegments = currentSegments.filterNot { it.id == segmentId }
            val nextSelected = newSegments.first().id
            _editState.update {
                it.copy(
                    segments = newSegments,
                    selectedSegmentId = nextSelected
                )
            }
        }
    }

    fun duplicateSegment(segmentId: String) {
        val currentSegments = _editState.value.segments
        val seg = currentSegments.find { it.id == segmentId } ?: return
        pushUndo()
        val index = currentSegments.indexOf(seg)
        val copySeg = seg.copy(
            id = UUID.randomUUID().toString(),
            name = "${seg.name} (Copy)"
        )
        val newSegments = currentSegments.toMutableList().apply {
            add(index + 1, copySeg)
        }
        _editState.update {
            it.copy(
                segments = newSegments,
                selectedSegmentId = copySeg.id
            )
        }
    }

    fun setTrimStart(startMs: Long) {
        val state = _editState.value
        val seg = state.selectedSegment ?: return
        pushUndo()

        val safeStart = startMs.coerceIn(0L, (seg.endMs - 400L).coerceAtLeast(0L))
        val updatedSegments = state.segments.map {
            if (it.id == seg.id) it.copy(startMs = safeStart) else it
        }

        _editState.update { it.copy(segments = updatedSegments) }
    }

    fun setTrimEnd(endMs: Long) {
        val state = _editState.value
        val seg = state.selectedSegment ?: return
        pushUndo()

        val maxAllowed = state.durationMs.coerceAtLeast(seg.startMs + 500L)
        val safeEnd = endMs.coerceIn((seg.startMs + 400L).coerceAtMost(maxAllowed), maxAllowed)
        val updatedSegments = state.segments.map {
            if (it.id == seg.id) it.copy(endMs = safeEnd) else it
        }

        _editState.update { it.copy(segments = updatedSegments) }
    }

    fun resetTrim() {
        val state = _editState.value
        val seg = state.selectedSegment ?: return
        pushUndo()

        val updatedSegments = state.segments.map {
            if (it.id == seg.id) it.copy(startMs = 0L, endMs = state.durationMs) else it
        }

        _editState.update { it.copy(segments = updatedSegments) }
    }

    // --- TEXT OVERLAY MANAGEMENT ---

    fun addTextOverlay(text: String, colorHex: String = "#FFFFFF", position: String = "Bottom") {
        if (text.isBlank()) return
        pushUndo()
        val totalDur = _editState.value.durationMs.coerceAtLeast(5000L)
        val playhead = _editState.value.currentPositionMs
        val newItem = TextOverlayItem(
            id = UUID.randomUUID().toString(),
            text = text,
            colorHex = colorHex,
            position = position,
            startMs = playhead,
            endMs = (playhead + 4000L).coerceAtMost(totalDur)
        )
        _editState.update {
            it.copy(
                textOverlays = it.textOverlays + newItem,
                selectedTextOverlayId = newItem.id
            )
        }
    }

    fun updateSelectedTextOverlay(
        text: String,
        colorHex: String,
        position: String,
        startMs: Long? = null,
        endMs: Long? = null
    ) {
        val active = _editState.value.activeTextOverlay
        if (active != null) {
            pushUndo()
            val updated = active.copy(
                text = text,
                colorHex = colorHex,
                position = position,
                startMs = startMs ?: active.startMs,
                endMs = endMs ?: active.endMs
            )
            val newList = _editState.value.textOverlays.map {
                if (it.id == active.id) updated else it
            }
            _editState.update { it.copy(textOverlays = newList) }
        } else {
            addTextOverlay(text, colorHex, position)
        }
    }

    fun selectTextOverlay(id: String) {
        _editState.update { it.copy(selectedTextOverlayId = id) }
        val overlay = _editState.value.textOverlays.find { it.id == id }
        if (overlay != null) {
            seekTo(overlay.startMs)
        }
    }

    fun deleteTextOverlay(id: String) {
        pushUndo()
        val newList = _editState.value.textOverlays.filterNot { it.id == id }
        _editState.update {
            it.copy(
                textOverlays = newList,
                selectedTextOverlayId = newList.firstOrNull()?.id ?: ""
            )
        }
    }

    fun setOverlayText(text: String, colorHex: String = "#FFFFFF", position: String = "Bottom") {
        updateSelectedTextOverlay(text, colorHex, position)
    }

    // --- GENERAL SETTINGS ---

    fun setPlaybackSpeed(speed: Float) {
        pushUndo()
        _editState.update { it.copy(playbackSpeed = speed) }
    }

    fun setFilter(filter: VideoFilter) {
        pushUndo()
        _editState.update { it.copy(selectedFilter = filter) }
    }

    fun setAspectRatio(ratio: AspectRatioOption) {
        pushUndo()
        _editState.update { it.copy(aspectRatio = ratio) }
    }

    fun setVolume(volume: Float) {
        _editState.update { it.copy(volume = volume.coerceIn(0f, 2.0f)) }
    }

    fun setBgMusic(musicTitle: String) {
        pushUndo()
        _editState.update { it.copy(bgMusicTitle = musicTitle) }
    }

    fun setAdjustments(brightness: Float, contrast: Float, saturation: Float) {
        _editState.update {
            it.copy(
                brightness = brightness,
                contrast = contrast,
                saturation = saturation
            )
        }
    }

    fun renameProject(newTitle: String) {
        _editState.update { it.copy(projectTitle = newTitle) }
        saveCurrentProject()
    }

    fun saveCurrentProject() {
        val state = _editState.value
        if (state.videoUri.isBlank()) return

        viewModelScope.launch {
            val entity = ProjectEntity(
                id = state.projectId,
                title = state.projectTitle,
                videoUri = state.videoUri,
                durationMs = state.durationMs,
                trimStartMs = state.trimStartMs,
                trimEndMs = state.trimEndMs,
                playbackSpeed = state.playbackSpeed,
                filterName = state.selectedFilter.filterName,
                aspectRatio = state.aspectRatio.label,
                overlayText = state.overlayText,
                overlayColorHex = state.overlayTextColorHex,
                overlayPosition = state.overlayTextPosition,
                volume = state.volume,
                bgMusicTitle = state.bgMusicTitle,
                brightness = state.brightness,
                contrast = state.contrast,
                saturation = state.saturation,
                lastModified = System.currentTimeMillis()
            )

            val newId = repository.saveProject(entity)
            if (state.projectId == 0L) {
                _editState.update { it.copy(projectId = newId) }
            }
        }
    }

    fun deleteProject(project: ProjectEntity) {
        viewModelScope.launch {
            repository.deleteProject(project)
        }
    }

    fun deleteProjectById(id: Long) {
        viewModelScope.launch {
            repository.deleteProjectById(id)
        }
    }

    fun startExport(resolution: String, fps: Int, onComplete: () -> Unit = {}) {
        exportJob?.cancel()
        _editState.update {
            it.copy(
                isExporting = true,
                exportProgress = 0f,
                isExportSuccess = false
            )
        }

        exportJob = viewModelScope.launch {
            for (step in 1..20) {
                delay(120)
                _editState.update { it.copy(exportProgress = step / 20f) }
            }
            saveCurrentProject()
            _editState.update {
                it.copy(
                    isExporting = false,
                    isExportSuccess = true
                )
            }
            onComplete()
        }
    }

    // --- AUTO CAPTIONS MANAGEMENT ---

    fun setCaptionLanguage(language: CaptionLanguage) {
        _editState.update { it.copy(selectedCaptionLanguage = language) }
    }

    fun generateAutoCaptions(language: CaptionLanguage? = null, customText: String? = null) {
        val selectedLang = language ?: _editState.value.selectedCaptionLanguage
        _editState.update {
            it.copy(
                isGeneratingCaptions = true,
                captionGenProgress = 0f,
                selectedCaptionLanguage = selectedLang
            )
        }

        viewModelScope.launch {
            for (step in 1..10) {
                delay(90)
                _editState.update { it.copy(captionGenProgress = step / 10f) }
            }

            val totalDur = _editState.value.durationMs.coerceAtLeast(4000L)
            val generated = com.example.util.AutoCaptionGenerator.generateCaptions(
                durationMs = totalDur,
                language = selectedLang,
                customPromptText = customText
            )

            pushUndo()
            _editState.update {
                it.copy(
                    autoCaptions = generated,
                    isGeneratingCaptions = false,
                    captionGenProgress = 1f,
                    activeTool = EditorTool.CAPTIONS
                )
            }
        }
    }

    fun applyCaptionTemplate(template: com.example.model.CaptionTemplate) {
        pushUndo()
        _editState.update { current ->
            val updatedConfig = current.captionConfig.copy(
                templateId = template.id,
                textColor = template.textColor,
                activeWordColor = template.activeWordColor,
                activeWordBg = template.activeWordBg,
                strokeColor = template.strokeColor,
                strokeWidth = template.strokeWidth,
                bgColor = template.bgColor,
                animation = template.animation
            )
            current.copy(captionConfig = updatedConfig)
        }
    }

    fun updateCaptionConfig(
        templateId: String? = null,
        fontSizeSp: Int? = null,
        textColor: androidx.compose.ui.graphics.Color? = null,
        activeWordColor: androidx.compose.ui.graphics.Color? = null,
        activeWordBg: androidx.compose.ui.graphics.Color? = null,
        strokeColor: androidx.compose.ui.graphics.Color? = null,
        strokeWidth: Float? = null,
        bgColor: androidx.compose.ui.graphics.Color? = null,
        bgOpacity: Float? = null,
        position: String? = null,
        fontFamilyName: String? = null,
        animation: com.example.model.CaptionAnimationType? = null
    ) {
        pushUndo()
        _editState.update { current ->
            val prev = current.captionConfig
            val updated = prev.copy(
                templateId = templateId ?: prev.templateId,
                fontSizeSp = fontSizeSp ?: prev.fontSizeSp,
                textColor = textColor ?: prev.textColor,
                activeWordColor = activeWordColor ?: prev.activeWordColor,
                activeWordBg = activeWordBg ?: prev.activeWordBg,
                strokeColor = strokeColor ?: prev.strokeColor,
                strokeWidth = strokeWidth ?: prev.strokeWidth,
                bgColor = bgColor ?: prev.bgColor,
                bgOpacity = bgOpacity ?: prev.bgOpacity,
                position = position ?: prev.position,
                fontFamilyName = fontFamilyName ?: prev.fontFamilyName,
                animation = animation ?: prev.animation
            )
            current.copy(captionConfig = updated)
        }
    }

    fun clearAutoCaptions() {
        pushUndo()
        _editState.update { it.copy(autoCaptions = emptyList()) }
    }

    fun dismissExportDialog() {
        _editState.update {
            it.copy(
                isExporting = false,
                isExportSuccess = false,
                exportProgress = 0f
            )
        }
    }
}
