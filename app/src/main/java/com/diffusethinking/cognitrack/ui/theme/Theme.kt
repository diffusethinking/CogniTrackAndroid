package com.diffusethinking.cognitrack.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = AppGreen,
    secondary = AppIndigo,
    tertiary = AppCyan,
    background = Color.Black,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    error = AppRed,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color(0xFF8E8E93),
    onError = Color.White
)

@Composable
fun CogniTrackTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = AppTypography,
        content = content
    )
}
