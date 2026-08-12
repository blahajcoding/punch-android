package com.punch.android.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val PunchColors = darkColorScheme(
    primary = PunchMint,
    onPrimary = PunchInk,
    secondary = PunchMintDeep,
    onSecondary = PunchIvory,
    background = PunchBlack,
    onBackground = PunchIvory,
    surface = PunchCharcoal,
    onSurface = PunchIvory,
    surfaceVariant = PunchBubble,
    onSurfaceVariant = PunchMuted,
    outline = PunchStroke,
    error = PunchRose,
)

@Composable
fun PunchTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PunchColors,
        typography = PunchTypography,
        content = content,
    )
}
