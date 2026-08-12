package com.hermes.launcher.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.hermes.launcher.launcher.ConnectionStatus
import com.hermes.launcher.ui.components.GlassPanel
import com.hermes.launcher.ui.theme.HerbInk
import com.hermes.launcher.ui.theme.HerbIvory
import com.hermes.launcher.ui.theme.HerbIvoryMuted
import com.hermes.launcher.ui.theme.HerbMint
import com.hermes.launcher.ui.theme.HerbStroke

@Composable
fun SettingsScreen(
    packageName: String,
    isDefaultHome: Boolean,
    microphoneGranted: Boolean,
    accessibilityEnabled: Boolean,
    accessibilityConnected: Boolean,
    volumeDownObserved: Boolean,
    useSystemWallpaper: Boolean,
    onUseSystemWallpaperChange: (Boolean) -> Unit,
    gatewayUrl: String,
    onGatewayUrlChange: (String) -> Unit,
    gatewayUsername: String,
    onGatewayUsernameChange: (String) -> Unit,
    gatewayApiKey: String,
    onGatewayApiKeyChange: (String) -> Unit,
    gatewayPaired: Boolean,
    connectionStatus: ConnectionStatus,
    gatewayMessage: String,
    onSaveGateway: () -> Unit,
    onTestGateway: () -> Unit,
    onClearGateway: () -> Unit,
    onRequestMicrophone: () -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = HerbIvory,
        unfocusedTextColor = HerbIvory,
        focusedBorderColor = HerbMint.copy(alpha = 0.7f),
        unfocusedBorderColor = HerbStroke,
        focusedLabelColor = HerbMint,
        unfocusedLabelColor = HerbIvoryMuted,
        cursorColor = HerbMint,
        focusedContainerColor = Color.White.copy(alpha = 0.06f),
        unfocusedContainerColor = Color.White.copy(alpha = 0.04f),
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineSmall,
                color = HerbIvory,
            )
            TextButton(onClick = onBack) {
                Text("Back", color = HerbMint)
            }
        }

        GlassPanel(modifier = Modifier.fillMaxWidth()) {
            Text("LOOK", style = MaterialTheme.typography.titleSmall, color = HerbIvoryMuted)
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                    Text(
                        text = "System wallpaper",
                        style = MaterialTheme.typography.titleMedium,
                        color = HerbIvory,
                    )
                    Text(
                        text = "Show the device wallpaper behind pincher. Turn off for a solid backdrop.",
                        style = MaterialTheme.typography.bodySmall,
                        color = HerbIvoryMuted,
                    )
                }
                Switch(
                    checked = useSystemWallpaper,
                    onCheckedChange = onUseSystemWallpaperChange,
                    modifier = Modifier.testTag("wallpaper_toggle"),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = HerbInk,
                        checkedTrackColor = HerbMint,
                        uncheckedThumbColor = HerbIvory,
                        uncheckedTrackColor = Color.White.copy(alpha = 0.2f),
                    ),
                )
            }
        }

        GlassPanel(modifier = Modifier.fillMaxWidth()) {
            Text("ABOUT", style = MaterialTheme.typography.titleSmall, color = HerbIvoryMuted)
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Package: $packageName",
                color = HerbIvory,
                modifier = Modifier.testTag("package_name"),
            )
            Text(
                text = "Default home: ${if (isDefaultHome) "yes" else "no"}",
                color = HerbIvory,
                modifier = Modifier.testTag("default_home_status"),
            )
        }

        GlassPanel(modifier = Modifier.fillMaxWidth()) {
            Text("PI", style = MaterialTheme.typography.titleSmall, color = HerbIvoryMuted)
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Point at a Pi coding-agent HTTP gateway (session + message). http:// for LAN, https:// for Tailscale.",
                style = MaterialTheme.typography.bodySmall,
                color = HerbIvoryMuted,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Status: ${connectionStatus.name}" +
                    if (gatewayPaired) " (paired)" else " (not paired)",
                color = HerbIvory,
                modifier = Modifier.testTag("gateway_connection_status"),
            )
            if (gatewayMessage.isNotBlank()) {
                Text(
                    text = gatewayMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = HerbIvoryMuted,
                    modifier = Modifier.testTag("gateway_message"),
                )
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = gatewayUrl,
                onValueChange = onGatewayUrlChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("gateway_url_field"),
                label = { Text("Pi URL") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = fieldColors,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = gatewayUsername,
                onValueChange = onGatewayUsernameChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("gateway_username_field"),
                label = { Text("Username (optional)") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = fieldColors,
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = gatewayApiKey,
                onValueChange = onGatewayApiKeyChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("gateway_api_key_field"),
                label = { Text("Password (optional)") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = fieldColors,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onSaveGateway,
                    modifier = Modifier.testTag("gateway_save_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HerbMint,
                        contentColor = HerbInk,
                    ),
                ) {
                    Text("Save")
                }
                OutlinedButton(
                    onClick = onTestGateway,
                    modifier = Modifier.testTag("gateway_test_button"),
                ) {
                    Text("Test", color = HerbIvory)
                }
                OutlinedButton(
                    onClick = onClearGateway,
                    modifier = Modifier.testTag("gateway_clear_button"),
                ) {
                    Text("Clear", color = HerbIvory)
                }
            }
        }

        GlassPanel(modifier = Modifier.fillMaxWidth()) {
            Text("PUSH TO TALK", style = MaterialTheme.typography.titleSmall, color = HerbIvoryMuted)
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Microphone: ${if (microphoneGranted) "granted" else "not granted"}",
                color = HerbIvory,
                modifier = Modifier.testTag("mic_permission_status"),
            )
            if (!microphoneGranted) {
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onRequestMicrophone,
                    modifier = Modifier.testTag("request_mic_button"),
                ) {
                    Text("Request microphone", color = HerbIvory)
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Accessibility service enabled: ${if (accessibilityEnabled) "yes" else "no"}",
                color = HerbIvory,
                modifier = Modifier.testTag("accessibility_enabled_status"),
            )
            Text(
                text = "Accessibility service connected: ${if (accessibilityConnected) "yes" else "no"}",
                color = HerbIvory,
                modifier = Modifier.testTag("accessibility_connected_status"),
            )
            Text(
                text = "Volume Down events observed: ${if (volumeDownObserved) "yes" else "no"}",
                color = HerbIvory,
                modifier = Modifier.testTag("volume_down_observed_status"),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Volume Down filtering is best-effort. Nothing OS may not deliver key events to accessibility services. Use Hold to talk if needed.",
                style = MaterialTheme.typography.bodySmall,
                color = HerbIvoryMuted,
            )
            Spacer(Modifier.height(10.dp))
            OutlinedButton(
                onClick = onOpenAccessibilitySettings,
                modifier = Modifier.testTag("open_accessibility_settings"),
            ) {
                Text("Open accessibility settings", color = HerbIvory)
            }
        }
    }
}
