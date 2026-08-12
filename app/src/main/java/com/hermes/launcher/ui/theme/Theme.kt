package com.hermes.launcher.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val HerbColors = darkColorScheme(
    primary = HerbMint,
    onPrimary = HerbInk,
    secondary = HerbMintDim,
    onSecondary = HerbIvory,
    background = HerbInk,
    onBackground = HerbIvory,
    surface = HerbMoss,
    onSurface = HerbIvory,
    surfaceVariant = HerbGlassStrong,
    onSurfaceVariant = HerbIvoryMuted,
    outline = HerbStroke,
    error = HerbRose,
)

@Composable
fun HermesTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = HerbColors,
        typography = HerbTypography,
        content = content,
    )
}
