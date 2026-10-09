package com.sriramanappindi.openwire.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = AccentLight,
    onPrimary = SurfaceLight,
    background = PaperGrey,
    onBackground = InkNavy,
    surface = SurfaceLight,
    onSurface = InkNavy,
    surfaceVariant = LineLight,
    onSurfaceVariant = MutedLight,
    error = HotLight
)

private val DarkColors = darkColorScheme(
    primary = AccentDark,
    onPrimary = InkNavy,
    background = InkNavy,
    onBackground = PaperGrey,
    surface = SurfaceDark,
    onSurface = PaperGrey,
    surfaceVariant = LineDark,
    onSurfaceVariant = MutedDark,
    error = HotDark
)

@Composable
fun OpenWireTheme(content: @Composable () -> Unit) {
    val colors = if (isSystemInDarkTheme()) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = OpenWireTypography,
        content = content
    )
}
