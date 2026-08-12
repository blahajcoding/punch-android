package com.hermes.launcher.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.hermes.launcher.launcher.ConnectionStatus
import com.hermes.launcher.ui.theme.HerbAmber
import com.hermes.launcher.ui.theme.HerbGlass
import com.hermes.launcher.ui.theme.HerbGlassStrong
import com.hermes.launcher.ui.theme.HerbInk
import com.hermes.launcher.ui.theme.HerbInkDeep
import com.hermes.launcher.ui.theme.HerbMint
import com.hermes.launcher.ui.theme.HerbMoss
import com.hermes.launcher.ui.theme.HerbRose
import com.hermes.launcher.ui.theme.HerbStroke

val HerbPanelShape = RoundedCornerShape(28.dp)

@Composable
fun HerbBackdrop(
    useSystemWallpaper: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(modifier = modifier.fillMaxSize()) {
        if (useSystemWallpaper) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0f to Color.Black.copy(alpha = 0.38f),
                            0.45f to Color.Black.copy(alpha = 0.22f),
                            1f to Color.Black.copy(alpha = 0.55f),
                        ),
                    ),
            )
        } else {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0f to HerbInkDeep,
                            0.55f to HerbInk,
                            1f to HerbMoss.copy(alpha = 0.85f),
                        ),
                    ),
            )
        }
        content()
    }
}

@Composable
fun GlassPanel(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .clip(HerbPanelShape)
            .background(HerbGlassStrong)
            .border(1.dp, HerbStroke, HerbPanelShape)
            .padding(18.dp),
        content = content,
    )
}

@Composable
fun ConnectionChip(
    status: ConnectionStatus,
    modifier: Modifier = Modifier,
) {
    val color = when (status) {
        ConnectionStatus.Connected -> HerbMint
        ConnectionStatus.Connecting -> HerbAmber
        ConnectionStatus.Disconnected -> HerbRose
    }
    val pulse = rememberInfiniteTransition(label = "conn")
    val alpha by pulse.animateFloat(
        initialValue = 1f,
        targetValue = if (status == ConnectionStatus.Connecting) 0.35f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "connAlpha",
    )
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(HerbGlass)
            .border(1.dp, HerbStroke, CircleShape)
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("connection_status"),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = alpha)),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = status.name.lowercase(),
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.92f),
        )
    }
}
