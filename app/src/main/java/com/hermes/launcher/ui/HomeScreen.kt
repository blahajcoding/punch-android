package com.hermes.launcher.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.hermes.launcher.input.PttPhase
import com.hermes.launcher.input.PttUiState
import com.hermes.launcher.launcher.ConnectionStatus
import com.hermes.launcher.ui.components.ConnectionChip
import com.hermes.launcher.ui.components.GlassPanel
import com.hermes.launcher.ui.theme.HerbGlass
import com.hermes.launcher.ui.theme.HerbIvory
import com.hermes.launcher.ui.theme.HerbIvoryMuted
import com.hermes.launcher.ui.theme.HerbMint
import com.hermes.launcher.ui.theme.HerbStroke

@Composable
fun HomeScreen(
    input: String,
    onInputChange: (String) -> Unit,
    response: String,
    onSend: () -> Unit,
    onStop: () -> Unit,
    connectionStatus: ConnectionStatus,
    pttState: PttUiState,
    onPttPressDown: () -> Unit,
    onPttPressUp: () -> Unit,
    onOpenDrawer: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val stopEnabled = pttState.phase != PttPhase.Idle ||
        connectionStatus == ConnectionStatus.Connecting

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "pincher",
                    style = MaterialTheme.typography.headlineMedium,
                    color = HerbIvory,
                    modifier = Modifier
                        .testTag("hermes_title")
                        .semantics { contentDescription = "pincher" },
                )
                Spacer(Modifier.height(8.dp))
                ConnectionChip(status = connectionStatus)
            }
            FilledIconButton(
                onClick = onOpenSettings,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = HerbGlass,
                    contentColor = HerbIvory,
                ),
            ) {
                Icon(Icons.Filled.Settings, contentDescription = "Settings")
            }
        }

        if (pttState.listeningVisible) {
            Text(
                text = "listening…",
                style = MaterialTheme.typography.titleMedium,
                color = HerbMint,
                modifier = Modifier.testTag("ptt_listening"),
            )
        } else if (pttState.statusMessage.isNotBlank()) {
            Text(
                text = pttState.statusMessage,
                style = MaterialTheme.typography.bodySmall,
                color = HerbIvoryMuted,
                modifier = Modifier.testTag("ptt_status"),
            )
        }

        GlassPanel(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            Text(
                text = "RESPONSE",
                style = MaterialTheme.typography.titleSmall,
                color = HerbIvoryMuted,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = response.ifBlank { "Waiting quietly." },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .testTag("response_area"),
                style = MaterialTheme.typography.bodyLarge,
                color = HerbIvory,
            )
        }

        GlassPanel(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = input,
                onValueChange = onInputChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("agent_input"),
                label = { Text("Ask pincher") },
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = HerbIvory,
                    unfocusedTextColor = HerbIvory,
                    focusedBorderColor = HerbMint.copy(alpha = 0.7f),
                    unfocusedBorderColor = HerbStroke,
                    focusedLabelColor = HerbMint,
                    unfocusedLabelColor = HerbIvoryMuted,
                    cursorColor = HerbMint,
                    focusedContainerColor = Color.White.copy(alpha = 0.06f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.04f),
                ),
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(
                    onClick = onSend,
                    modifier = Modifier.testTag("send_button"),
                ) {
                    Text("Send", color = HerbMint)
                }
                TextButton(
                    onClick = onStop,
                    enabled = stopEnabled,
                    modifier = Modifier.testTag("stop_button"),
                ) {
                    Text(
                        "Stop",
                        color = if (stopEnabled) HerbIvory else HerbIvoryMuted.copy(alpha = 0.4f),
                    )
                }
                Spacer(Modifier.weight(1f))
                FilledIconButton(
                    onClick = onOpenDrawer,
                    modifier = Modifier
                        .testTag("app_drawer_button")
                        .semantics { contentDescription = "Apps" },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = HerbMint,
                        contentColor = Color(0xFF07110E),
                    ),
                ) {
                    Canvas(Modifier.size(18.dp)) {
                        val step = size.minDimension / 3f
                        val r = step * 0.18f
                        for (row in 0..2) {
                            for (col in 0..2) {
                                drawCircle(
                                    color = Color(0xFF07110E),
                                    radius = r,
                                    center = androidx.compose.ui.geometry.Offset(
                                        x = step * (col + 0.5f),
                                        y = step * (row + 0.5f),
                                    ),
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
            HoldToTalkButton(
                listening = pttState.listeningVisible,
                onPressDown = onPttPressDown,
                onPressUp = onPttPressUp,
            )
        }
    }
}

@Composable
private fun HoldToTalkButton(
    listening: Boolean,
    onPressDown: () -> Unit,
    onPressUp: () -> Unit,
) {
    val pulse = rememberInfiniteTransition(label = "ptt")
    val scale by pulse.animateFloat(
        initialValue = 1f,
        targetValue = if (listening) 1.04f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pttScale",
    )
    val fill by animateColorAsState(
        targetValue = if (listening) HerbMint.copy(alpha = 0.28f) else HerbGlass,
        label = "pttFill",
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(fill)
            .border(1.dp, if (listening) HerbMint else HerbStroke, CircleShape)
            .testTag("ptt_button")
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    onPressDown()
                    waitForUpOrCancellation()
                    onPressUp()
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (listening) HerbMint else HerbIvoryMuted),
            )
            Spacer(Modifier.size(10.dp))
            Text(
                text = if (listening) "Release to send" else "Hold to talk",
                style = MaterialTheme.typography.labelLarge,
                color = HerbIvory,
            )
        }
    }
}
