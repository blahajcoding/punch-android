package com.punch.android.ui

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.punch.android.data.Bundle
import com.punch.android.data.ChatThread
import com.punch.android.ui.theme.PunchInk
import com.punch.android.ui.theme.PunchIvory
import com.punch.android.ui.theme.PunchMint
import com.punch.android.ui.theme.PunchMuted
import com.punch.android.ui.theme.PunchStroke

@Composable
fun BundleScreen(
    bundle: Bundle,
    chats: List<ChatThread>,
    onNameChange: (String) -> Unit,
    onInstructionsChange: (String) -> Unit,
    onAddFile: () -> Unit,
    onNewChat: () -> Unit,
    onOpenChat: (ChatThread) -> Unit,
    onDeleteChat: (ChatThread) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showSettings by rememberSaveable { mutableStateOf(false) }
    if (showSettings) {
        BundleSettings(
            bundle = bundle,
            onNameChange = onNameChange,
            onInstructionsChange = onInstructionsChange,
            onDone = { showSettings = false },
            modifier = modifier,
        )
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onBack) { Text("Back", color = PunchMint) }
            Spacer(Modifier.weight(1f))
            IconButton(
                onClick = { showSettings = true },
                modifier = Modifier
                    .testTag("bundle_settings")
                    .semantics { contentDescription = "Bundle settings" },
            ) {
                Icon(Icons.Filled.Settings, contentDescription = "Bundle settings", tint = PunchIvory)
            }
        }
        Text(
            text = bundle.name,
            style = MaterialTheme.typography.headlineSmall,
            color = PunchIvory,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
        )
        OutlinedButton(
            onClick = onAddFile,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("bundle_add_file"),
            shape = RoundedCornerShape(50),
        ) {
            Icon(Icons.Filled.Add, contentDescription = null, tint = PunchIvory)
            Spacer(Modifier.width(8.dp))
            Text("Add files", color = PunchIvory)
        }
        if (bundle.files.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            bundle.files.forEach { file ->
                Text(
                    text = file.name,
                    color = PunchIvory,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 6.dp),
                )
            }
        }
        Spacer(Modifier.height(20.dp))
        Text("Chats", color = PunchMuted, style = MaterialTheme.typography.bodySmall)
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(top = 8.dp),
        ) {
            items(chats, key = { it.id }) { chat ->
                DeleteMenuBox(
                    onClick = { onOpenChat(chat) },
                    onDelete = { onDeleteChat(chat) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = chat.title,
                        color = PunchIvory,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp),
                    )
                }
            }
        }
        Button(
            onClick = onNewChat,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("bundle_new_chat"),
            colors = ButtonDefaults.buttonColors(containerColor = PunchMint, contentColor = PunchInk),
            shape = RoundedCornerShape(50),
        ) {
            Text("New chat in bundle")
        }
    }
}

@Composable
private fun BundleSettings(
    bundle: Bundle,
    onNameChange: (String) -> Unit,
    onInstructionsChange: (String) -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = PunchIvory,
        unfocusedTextColor = PunchIvory,
        focusedBorderColor = PunchMint.copy(alpha = 0.7f),
        unfocusedBorderColor = PunchStroke,
        focusedLabelColor = PunchMint,
        unfocusedLabelColor = PunchMuted,
        cursorColor = PunchMint,
        focusedContainerColor = Color.White.copy(alpha = 0.06f),
        unfocusedContainerColor = Color.White.copy(alpha = 0.04f),
    )
    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Bundle settings", style = MaterialTheme.typography.headlineSmall, color = PunchIvory)
            TextButton(onClick = onDone) { Text("Done", color = PunchMint) }
        }
        OutlinedTextField(
            value = bundle.name,
            onValueChange = onNameChange,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("bundle_name"),
            label = { Text("Name") },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = fieldColors,
        )
        OutlinedTextField(
            value = bundle.instructions,
            onValueChange = onInstructionsChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .testTag("bundle_instructions"),
            label = { Text("Custom instructions") },
            shape = RoundedCornerShape(16.dp),
            colors = fieldColors,
        )
        Text(
            text = "These instructions and files are shared with every chat in this bundle, and with any other chat that uses it.",
            style = MaterialTheme.typography.bodySmall,
            color = PunchMuted,
        )
    }
}
