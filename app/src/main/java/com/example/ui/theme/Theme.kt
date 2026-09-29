package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val TMProDarkColorScheme = darkColorScheme(
    primary = StudioCyan,
    onPrimary = Color.Black,
    primaryContainer = StudioIndigo,
    onPrimaryContainer = Color.White,
    secondary = StudioPurple,
    onSecondary = Color.White,
    background = StudioDarkBg,
    onBackground = StudioTextPrimary,
    surface = StudioCardBg,
    onSurface = StudioTextPrimary,
    surfaceVariant = StudioCardElevated,
    onSurfaceVariant = StudioTextSecondary,
    outline = StudioBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false, // Keep TM PRO signature studio palette
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = TMProDarkColorScheme,
        typography = Typography,
        content = content
    )
}
