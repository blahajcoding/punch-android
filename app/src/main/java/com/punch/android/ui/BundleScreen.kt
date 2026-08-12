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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.punch.android.data.Bundle
import com.punch.android.data.ChatThread
import com.punch.android.ui.theme.PunchIvory
import com.punch.android.ui.theme.PunchMint
import com.punch.android.ui.theme.PunchMuted
import com.punch.android.ui.theme.PunchInk
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
    onBack: () -> Unit,
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
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Bundle", style = MaterialTheme.typography.headlineSmall, color = PunchIvory)
            TextButton(onClick = onBack) { Text("Back", color = PunchMint) }
        }
        OutlinedTextField(
            value = bundle.name,
            onValueChange = onNameChange,
            modifier = Modifier.fillMaxWidth().testTag("bundle_name"),
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
                .height(160.dp)
                .testTag("bundle_instructions"),
            label = { Text("Shared instructions") },
            shape = RoundedCornerShape(16.dp),
            colors = fieldColors,
        )
        Text(
            text = "Files in this bundle are included in every chat here, and in any other chat that cross-references this bundle.",
            style = MaterialTheme.typography.bodySmall,
            color = PunchMuted,
        )
        bundle.files.forEach { file ->
            Text(file.name, color = PunchIvory, style = MaterialTheme.typography.bodyMedium)
        }
        OutlinedButton(onClick = onAddFile, modifier = Modifier.testTag("bundle_add_file")) {
            Text("Add file", color = PunchIvory)
        }
        Button(
            onClick = onNewChat,
            colors = ButtonDefaults.buttonColors(containerColor = PunchMint, contentColor = PunchInk),
        ) {
            Text("New chat in bundle")
        }
        if (chats.isNotEmpty()) {
            Text("Chats", color = PunchMuted, style = MaterialTheme.typography.bodySmall)
            chats.forEach { chat ->
                TextButton(onClick = { onOpenChat(chat) }) {
                    Text(chat.title, color = PunchIvory)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
    }
}
