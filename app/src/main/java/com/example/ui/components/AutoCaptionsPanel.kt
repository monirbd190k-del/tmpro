package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CaptionAnimationType
import com.example.model.CaptionLanguage
import com.example.model.CaptionTemplate
import com.example.model.CaptionTemplatesCatalog
import com.example.model.VideoEditState
import com.example.util.TimeFormatter

@Composable
fun AutoCaptionsPanel(
    state: VideoEditState,
    onLanguageChange: (CaptionLanguage) -> Unit,
    onGenerate: (CaptionLanguage) -> Unit,
    onApplyTemplate: (CaptionTemplate) -> Unit,
    onUpdateConfig: (
        templateId: String?,
        fontSizeSp: Int?,
        textColor: Color?,
        activeWordColor: Color?,
        activeWordBg: Color?,
        strokeColor: Color?,
        strokeWidth: Float?,
        bgColor: Color?,
        bgOpacity: Float?,
        position: String?,
        fontFamilyName: String?,
        animation: CaptionAnimationType?
    ) -> Unit,
    onClearCaptions: () -> Unit
) {
    var activeSubTab by remember { mutableIntStateOf(0) } // 0: Templates, 1: Style, 2: Animation & Position

    val colorPalette = listOf(
        Color.White,
        Color(0xFFFFE600), // Neon Yellow
        Color(0xFF00E5FF), // Cyber Cyan
        Color(0xFFFF3D00), // Coral Flame
        Color(0xFF22C55E), // Lime Green
        Color(0xFFFF007F), // Vivid Magenta
        Color(0xFFFFD700), // Gold
        Color(0xFFA855F7)  // Electric Purple
    )

    val fontFamilies = listOf("Default", "SansSerif", "Serif", "Monospace", "Cursive")

    Column(modifier = Modifier.fillMaxWidth()) {

        // Top Row: Language Selector & Generate Action
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Language selector chips
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                CaptionLanguage.entries.forEach { lang ->
                    val isSel = state.selectedCaptionLanguage == lang
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSel) Color(0xFF06B6D4) else Color(0xFF242430))
                            .clickable { onLanguageChange(lang) }
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                            .testTag("caption_lang_${lang.name.lowercase()}")
                    ) {
                        Text(
                            text = "${lang.flag} ${lang.displayName.split(" ").first()}",
                            color = if (isSel) Color.Black else Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Generate or Re-generate Button
            if (state.isGeneratingCaptions) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = Color(0xFF06B6D4)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Transcribing...",
                        color = Color(0xFF06B6D4),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else if (state.autoCaptions.isEmpty()) {
                Button(
                    onClick = { onGenerate(state.selectedCaptionLanguage) },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFE600),
                        contentColor = Color.Black
                    ),
                    contentPadding = ButtonDefaults.TextButtonContentPadding,
                    modifier = Modifier
                        .height(34.dp)
                        .testTag("generate_captions_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Generate",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Auto Generate", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = { onGenerate(state.selectedCaptionLanguage) },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Regenerate",
                            tint = Color(0xFF06B6D4),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onClearCaptions,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Clear",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Generating progress bar
        if (state.isGeneratingCaptions) {
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { state.captionGenProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = Color(0xFF06B6D4),
                trackColor = Color(0xFF282832)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Customization Sub-Tabs: Templates, Style, Animation
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF131318), RoundedCornerShape(8.dp))
                .padding(3.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            listOf("12+ Templates", "Font & Stroke", "Animation & Pos").forEachIndexed { idx, label ->
                val isSel = activeSubTab == idx
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSel) Color(0xFF282834) else Color.Transparent)
                        .clickable { activeSubTab = idx }
                        .padding(vertical = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = if (isSel) Color.White else Color(0xFF888892),
                        fontSize = 11.sp,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        when (activeSubTab) {
            0 -> {
                // Templates Carousel (12 Distinct CapCut Style Templates)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CaptionTemplatesCatalog.templates.forEach { tpl ->
                        val isSel = state.captionConfig.templateId == tpl.id
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSel) Color(0xFF262634) else Color(0xFF1E1E26))
                                .border(
                                    width = if (isSel) 2.dp else 0.5.dp,
                                    color = if (isSel) Color(0xFF06B6D4) else Color(0xFF383848),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { onApplyTemplate(tpl) }
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            // Mini Preview Box
                            Box(
                                modifier = Modifier
                                    .size(width = 54.dp, height = 30.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(tpl.bgColor.takeIf { it != Color.Transparent } ?: Color(0xFF141418)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = tpl.previewBadge,
                                    color = tpl.activeWordColor,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = tpl.name,
                                color = if (isSel) Color.White else Color(0xFFAAAAAA),
                                fontSize = 10.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            1 -> {
                // Font, Size & Stroke controls
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Font Family Selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Font:", color = Color(0xFFAAAAAA), fontSize = 11.sp)
                        fontFamilies.forEach { font ->
                            val isSel = state.captionConfig.fontFamilyName == font
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSel) Color.White else Color(0xFF282834))
                                    .clickable { onUpdateConfig(null, null, null, null, null, null, null, null, null, null, font, null) }
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = font,
                                    color = if (isSel) Color.Black else Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Font Size Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Size: ${state.captionConfig.fontSizeSp}sp", color = Color(0xFFAAAAAA), fontSize = 11.sp)
                        Slider(
                            value = state.captionConfig.fontSizeSp.toFloat(),
                            onValueChange = { onUpdateConfig(null, it.toInt(), null, null, null, null, null, null, null, null, null, null) },
                            valueRange = 14f..36f,
                            modifier = Modifier.width(180.dp),
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF06B6D4),
                                activeTrackColor = Color(0xFF06B6D4),
                                inactiveTrackColor = Color(0xFF282832)
                            )
                        )
                    }

                    // Stroke Width Slider & Active Color
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Stroke: ${state.captionConfig.strokeWidth.toInt()}px", color = Color(0xFFAAAAAA), fontSize = 11.sp)
                        Slider(
                            value = state.captionConfig.strokeWidth,
                            onValueChange = { onUpdateConfig(null, null, null, null, null, null, it, null, null, null, null, null) },
                            valueRange = 0f..8f,
                            modifier = Modifier.width(180.dp),
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFFFFE600),
                                activeTrackColor = Color(0xFFFFE600),
                                inactiveTrackColor = Color(0xFF282832)
                            )
                        )
                    }

                    // Color Palette (Text & Highlight)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Active Word:", color = Color(0xFFAAAAAA), fontSize = 11.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            colorPalette.forEach { c ->
                                val isSel = state.captionConfig.activeWordColor == c
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(c)
                                        .border(
                                            width = if (isSel) 2.dp else 0.dp,
                                            color = if (isSel) Color.White else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable {
                                            onUpdateConfig(null, null, null, c, null, null, null, null, null, null, null, null)
                                        }
                                )
                            }
                        }
                    }
                }
            }

            2 -> {
                // Animation & Position Controls
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Animation Style Selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Animation:", color = Color(0xFFAAAAAA), fontSize = 11.sp)
                        CaptionAnimationType.entries.forEach { anim ->
                            val isSel = state.captionConfig.animation == anim
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSel) Color(0xFFFFE600) else Color(0xFF282834))
                                    .clickable { onUpdateConfig(null, null, null, null, null, null, null, null, null, null, null, anim) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = anim.title,
                                    color = if (isSel) Color.Black else Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Position Selector (Top, Center, Bottom)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Screen Position:", color = Color(0xFFAAAAAA), fontSize = 11.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("Top", "Center", "Bottom").forEach { pos ->
                                val isSel = state.captionConfig.position.equals(pos, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSel) Color(0xFF06B6D4) else Color(0xFF282834))
                                        .clickable { onUpdateConfig(null, null, null, null, null, null, null, null, null, pos, null, null) }
                                        .padding(horizontal = 12.dp, vertical = 4.dp)
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

                    Spacer(modifier = Modifier.height(8.dp))

                    // Background Opacity
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Badge Opacity: ${(state.captionConfig.bgOpacity * 100).toInt()}%", color = Color(0xFFAAAAAA), fontSize = 11.sp)
                        Slider(
                            value = state.captionConfig.bgOpacity,
                            onValueChange = { onUpdateConfig(null, null, null, null, null, null, null, null, it, null, null, null) },
                            valueRange = 0f..1f,
                            modifier = Modifier.width(180.dp),
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF06B6D4),
                                activeTrackColor = Color(0xFF06B6D4),
                                inactiveTrackColor = Color(0xFF282832)
                            )
                        )
                    }
                }
            }
        }
    }
}
