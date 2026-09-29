package com.example.model

enum class EditorTool(val title: String) {
    TRIM("Trim & Cut"),
    SPLIT("Split"),
    CAPTIONS("Auto Captions"),
    TEXT("Text"),
    SPEED("Speed"),
    FILTERS("Filters"),
    AUDIO("Audio"),
    RATIO("Ratio"),
    ADJUST("Adjust")
}

data class VideoEditState(
    val projectId: Long = 0L,
    val projectTitle: String = "Untitled Video",
    val videoUri: String = "",
    val durationMs: Long = 0L,
    val currentPositionMs: Long = 0L,
    val isPlaying: Boolean = false,
    val segments: List<VideoSegment> = emptyList(),
    val selectedSegmentId: String = "",
    val playbackSpeed: Float = 1.0f,
    val selectedFilter: VideoFilter = VideoFilter.NORMAL,
    val aspectRatio: AspectRatioOption = AspectRatioOption.RATIO_16_9,
    val textOverlays: List<TextOverlayItem> = emptyList(),
    val selectedTextOverlayId: String = "",
    // Auto Captions
    val autoCaptions: List<AutoCaptionPhrase> = emptyList(),
    val captionConfig: CaptionStyleConfig = CaptionStyleConfig(),
    val isGeneratingCaptions: Boolean = false,
    val captionGenProgress: Float = 0f,
    val selectedCaptionLanguage: CaptionLanguage = CaptionLanguage.BANGLA,
    // Audio & FX
    val volume: Float = 1.0f,
    val bgMusicTitle: String = "None",
    val brightness: Float = 0f,
    val contrast: Float = 1f,
    val saturation: Float = 1f,
    val activeTool: EditorTool = EditorTool.TRIM,
    val isExporting: Boolean = false,
    val exportProgress: Float = 0f,
    val isExportSuccess: Boolean = false,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false
) {
    val selectedSegment: VideoSegment?
        get() = segments.find { it.id == selectedSegmentId } ?: segments.firstOrNull()

    val totalEditedDurationMs: Long
        get() = if (segments.isNotEmpty()) segments.sumOf { it.durationMs } else durationMs

    val trimStartMs: Long
        get() = selectedSegment?.startMs ?: 0L

    val trimEndMs: Long
        get() = selectedSegment?.endMs ?: durationMs

    val trimmedDurationMs: Long
        get() = selectedSegment?.durationMs ?: totalEditedDurationMs

    val activeTextOverlay: TextOverlayItem?
        get() = textOverlays.find { it.id == selectedTextOverlayId } ?: textOverlays.firstOrNull()

    val overlayText: String
        get() = activeTextOverlay?.text ?: ""

    val overlayTextColorHex: String
        get() = activeTextOverlay?.colorHex ?: "#FFFFFF"

    val overlayTextPosition: String
        get() = activeTextOverlay?.position ?: "Bottom"

    // Find current active auto-caption phrase at playhead
    val currentCaptionPhrase: AutoCaptionPhrase?
        get() = autoCaptions.find { currentPositionMs in it.startMs..it.endMs }
}
