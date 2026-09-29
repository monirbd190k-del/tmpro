package com.example.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import java.util.UUID

enum class CaptionLanguage(val displayName: String, val flag: String) {
    BANGLA("Bangla (বাংলা)", "🇧🇩"),
    ENGLISH("English", "🇺🇸"),
    BILINGUAL("Bangla + English", "🌐")
}

data class CaptionWord(
    val word: String,
    val startMs: Long,
    val endMs: Long
)

data class AutoCaptionPhrase(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val words: List<CaptionWord> = emptyList(),
    val startMs: Long,
    val endMs: Long
) {
    fun getActiveWordIndex(currentMs: Long): Int {
        if (currentMs < startMs || currentMs > endMs) return -1
        return words.indexOfFirst { currentMs in it.startMs..it.endMs }
    }
}

enum class CaptionAnimationType(val title: String) {
    WORD_BOUNCE("Word Bounce"),
    WORD_HIGHLIGHT("Word Highlight"),
    KARAOKE_COLOR("Karaoke Color"),
    FADE_IN("Fade Reveal"),
    POP_UP("Pop Up")
}

data class CaptionTemplate(
    val id: String,
    val name: String,
    val textColor: Color,
    val activeWordColor: Color,
    val activeWordBg: Color = Color.Transparent,
    val strokeColor: Color = Color.Black,
    val strokeWidth: Float = 4f,
    val bgColor: Color = Color.Transparent,
    val bgRounded: Boolean = true,
    val fontWeight: FontWeight = FontWeight.ExtraBold,
    val animation: CaptionAnimationType = CaptionAnimationType.WORD_BOUNCE,
    val previewBadge: String = "Aa"
)

object CaptionTemplatesCatalog {
    val templates = listOf(
        CaptionTemplate(
            id = "hormozi",
            name = "Hormozi Pop",
            textColor = Color.White,
            activeWordColor = Color(0xFFFFE600), // Vibrant Yellow
            activeWordBg = Color.Transparent,
            strokeColor = Color.Black,
            strokeWidth = 6f,
            fontWeight = FontWeight.Black,
            animation = CaptionAnimationType.WORD_BOUNCE,
            previewBadge = "POP"
        ),
        CaptionTemplate(
            id = "neon_cyan",
            name = "Neon Cyan",
            textColor = Color(0xFFE0F7FA),
            activeWordColor = Color(0xFF00E5FF),
            activeWordBg = Color(0xFF00E5FF).copy(alpha = 0.25f),
            strokeColor = Color(0xFF006064),
            strokeWidth = 5f,
            fontWeight = FontWeight.ExtraBold,
            animation = CaptionAnimationType.WORD_HIGHLIGHT,
            previewBadge = "GLOW"
        ),
        CaptionTemplate(
            id = "viral_badge",
            name = "Viral Reels",
            textColor = Color.White,
            activeWordColor = Color(0xFF22C55E), // Emerald Lime
            bgColor = Color.Black.copy(alpha = 0.85f),
            strokeColor = Color.Transparent,
            strokeWidth = 0f,
            bgRounded = true,
            fontWeight = FontWeight.Bold,
            animation = CaptionAnimationType.WORD_BOUNCE,
            previewBadge = "REELS"
        ),
        CaptionTemplate(
            id = "sunset_gold",
            name = "Sunset Gold",
            textColor = Color(0xFFFFD54F),
            activeWordColor = Color(0xFFFF3D00), // Deep Coral Red
            strokeColor = Color(0xFF3E2723),
            strokeWidth = 5f,
            fontWeight = FontWeight.Black,
            animation = CaptionAnimationType.KARAOKE_COLOR,
            previewBadge = "GOLD"
        ),
        CaptionTemplate(
            id = "cyber_magenta",
            name = "Cyber Pink",
            textColor = Color.White,
            activeWordColor = Color(0xFFFF007F), // Vivid Magenta
            activeWordBg = Color(0xFFFF007F).copy(alpha = 0.2f),
            strokeColor = Color(0xFF4A0033),
            strokeWidth = 6f,
            fontWeight = FontWeight.ExtraBold,
            animation = CaptionAnimationType.WORD_HIGHLIGHT,
            previewBadge = "CYBER"
        ),
        CaptionTemplate(
            id = "classic_movie",
            name = "Classic Sub",
            textColor = Color(0xFFFFEB3B),
            activeWordColor = Color.White,
            strokeColor = Color.Black,
            strokeWidth = 4f,
            fontWeight = FontWeight.Bold,
            animation = CaptionAnimationType.KARAOKE_COLOR,
            previewBadge = "SUB"
        ),
        CaptionTemplate(
            id = "karaoke_pill",
            name = "Karaoke Pill",
            textColor = Color.White,
            activeWordColor = Color.Black,
            activeWordBg = Color(0xFF10B981), // Emerald Pill
            bgColor = Color.Black.copy(alpha = 0.6f),
            strokeColor = Color.Transparent,
            strokeWidth = 0f,
            fontWeight = FontWeight.ExtraBold,
            animation = CaptionAnimationType.WORD_HIGHLIGHT,
            previewBadge = "KARAOKE"
        ),
        CaptionTemplate(
            id = "bangla_cinema",
            name = "Bangla Cinema",
            textColor = Color(0xFFFFF176),
            activeWordColor = Color(0xFFE53935),
            strokeColor = Color(0xFF212121),
            strokeWidth = 5f,
            fontWeight = FontWeight.Bold,
            animation = CaptionAnimationType.WORD_BOUNCE,
            previewBadge = "বাংলা"
        ),
        CaptionTemplate(
            id = "minimal_clean",
            name = "Minimal Clean",
            textColor = Color.White,
            activeWordColor = Color(0xFF38BDF8),
            strokeColor = Color.Black.copy(alpha = 0.8f),
            strokeWidth = 2f,
            fontWeight = FontWeight.SemiBold,
            animation = CaptionAnimationType.FADE_IN,
            previewBadge = "CLEAN"
        ),
        CaptionTemplate(
            id = "gradient_fire",
            name = "Gradient Fire",
            textColor = Color(0xFFFFF9C4),
            activeWordColor = Color(0xFFFF5722),
            activeWordBg = Color(0xFFFFAB00).copy(alpha = 0.2f),
            strokeColor = Color.Black,
            strokeWidth = 5f,
            fontWeight = FontWeight.Black,
            animation = CaptionAnimationType.WORD_BOUNCE,
            previewBadge = "FIRE"
        ),
        CaptionTemplate(
            id = "frost_glass",
            name = "Glass Frost",
            textColor = Color.White,
            activeWordColor = Color(0xFF818CF8),
            bgColor = Color(0xFF1E1E2E).copy(alpha = 0.75f),
            strokeColor = Color(0xFF6366F1).copy(alpha = 0.5f),
            strokeWidth = 1f,
            fontWeight = FontWeight.Bold,
            animation = CaptionAnimationType.WORD_HIGHLIGHT,
            previewBadge = "GLASS"
        ),
        CaptionTemplate(
            id = "bold_red",
            name = "Action Red",
            textColor = Color.White,
            activeWordColor = Color(0xFFFF1744),
            strokeColor = Color.Black,
            strokeWidth = 6f,
            fontWeight = FontWeight.Black,
            animation = CaptionAnimationType.WORD_BOUNCE,
            previewBadge = "ACTION"
        )
    )
}

data class CaptionStyleConfig(
    val templateId: String = "hormozi",
    val fontSizeSp: Int = 22,
    val textColor: Color = Color.White,
    val activeWordColor: Color = Color(0xFFFFE600),
    val activeWordBg: Color = Color.Transparent,
    val strokeColor: Color = Color.Black,
    val strokeWidth: Float = 5f,
    val bgColor: Color = Color.Transparent,
    val bgOpacity: Float = 0.7f,
    val position: String = "Bottom", // "Top", "Center", "Bottom"
    val fontFamilyName: String = "Default", // "Default", "SansSerif", "Serif", "Monospace", "Cursive"
    val animation: CaptionAnimationType = CaptionAnimationType.WORD_BOUNCE
) {
    fun getFontFamily(): FontFamily {
        return when (fontFamilyName) {
            "Serif" -> FontFamily.Serif
            "Monospace" -> FontFamily.Monospace
            "Cursive" -> FontFamily.Cursive
            "SansSerif" -> FontFamily.SansSerif
            else -> FontFamily.Default
        }
    }
}
