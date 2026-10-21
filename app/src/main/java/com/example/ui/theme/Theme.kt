package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = CncPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = Color(0xFF0369A1),
    secondary = CncCyanAccent,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFECFEFF),
    onSecondaryContainer = Color(0xFF0E7490),
    tertiary = CncProfit,
    onTertiary = Color.White,
    background = CncBackground,
    onBackground = CncTextPrimary,
    surface = CncSurface,
    onSurface = CncTextPrimary,
    surfaceVariant = CncSurfaceVariant,
    onSurfaceVariant = CncTextSecondary,
    outline = CncBorder,
    outlineVariant = CncBorderBright
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
