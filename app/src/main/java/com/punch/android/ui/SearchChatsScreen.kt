package com.punch.android.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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
import com.punch.android.data.ChatThread
import com.punch.android.ui.theme.PunchIvory
import com.punch.android.ui.theme.PunchMint
import com.punch.android.ui.theme.PunchMuted
import com.punch.android.ui.theme.PunchStroke

@Composable
fun SearchChatsScreen(
    query: String,
    onQueryChange: (String) -> Unit,
    chats: List<ChatThread>,
    onOpenChat: (ChatThread) -> Unit,
    onDeleteChat: (ChatThread) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val matches = chats.filter {
        query.isBlank() || it.title.contains(query, ignoreCase = true) ||
            it.messages.any { message -> message.text.contains(query, ignoreCase = true) }
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Search chats",
                style = MaterialTheme.typography.headlineSmall,
                color = PunchIvory,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onBack) { Text("Back", color = PunchMint) }
        }
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_chats_field"),
            placeholder = { Text("Search") },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = PunchIvory,
                unfocusedTextColor = PunchIvory,
                focusedBorderColor = PunchMint.copy(alpha = 0.7f),
                unfocusedBorderColor = PunchStroke,
                cursorColor = PunchMint,
                focusedContainerColor = Color.White.copy(alpha = 0.06f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.04f),
            ),
        )
        LazyColumn(Modifier.padding(top = 12.dp)) {
            items(matches, key = { it.id }) { chat ->
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
    }
}
