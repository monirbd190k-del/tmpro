package com.example.model

enum class AspectRatioOption(
    val label: String,
    val subtitle: String,
    val ratio: Float,
    val iconName: String
) {
    RATIO_16_9("16:9", "YouTube & Landscape", 16f / 9f, "landscape"),
    RATIO_9_16("9:16", "Shorts & Reels & TikTok", 9f / 16f, "portrait"),
    RATIO_1_1("1:1", "Square & Feed", 1f, "square"),
    RATIO_4_5("4:5", "Instagram Post", 4f / 5f, "post"),
    RATIO_21_9("21:9", "Cinematic Ultrawide", 21f / 9f, "cinema");

    companion object {
        fun fromLabel(label: String): AspectRatioOption {
            return entries.find { it.label == label } ?: RATIO_16_9
        }
    }
}
