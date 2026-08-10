package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val GsmProColorScheme = darkColorScheme(
    primary = GoldPrimary,
    onPrimary = TextDark,
    primaryContainer = GoldDark,
    onPrimaryContainer = TextWhite,
    secondary = NeonCyan,
    onSecondary = TextDark,
    tertiary = NeonPink,
    background = CyberDarkBg,
    onBackground = TextWhite,
    surface = CyberCardBg,
    onSurface = TextWhite,
    surfaceVariant = Color(0xFF252D42),
    onSurfaceVariant = TextMuted,
    outline = GoldPrimary
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = GsmProColorScheme,
        typography = Typography,
        content = content
    )
}
