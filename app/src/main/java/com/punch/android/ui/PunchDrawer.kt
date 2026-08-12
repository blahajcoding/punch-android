package com.punch.android.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.punch.android.data.Bundle
import com.punch.android.data.ChatThread
import com.punch.android.ui.theme.PunchCharcoal
import com.punch.android.ui.theme.PunchIvory
import com.punch.android.ui.theme.PunchMint
import com.punch.android.ui.theme.PunchMuted

@Composable
fun PunchDrawer(
    bundles: List<Bundle>,
    recents: List<ChatThread>,
    activeChatId: String?,
    onClose: () -> Unit,
    onNewChat: () -> Unit,
    onSearch: () -> Unit,
    onNewBundle: () -> Unit,
    onOpenBundle: (Bundle) -> Unit,
    onOpenChat: (ChatThread) -> Unit,
    onDeleteBundle: (Bundle) -> Unit,
    onDeleteChat: (ChatThread) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(320.dp)
            .background(Color.Black)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 18.dp, vertical = 8.dp)
            .testTag("punch_drawer"),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Punch",
                style = MaterialTheme.typography.headlineSmall,
                color = PunchIvory,
            )
            IconButton(
                onClick = onClose,
                modifier = Modifier.semantics { contentDescription = "Close menu" },
            ) {
                Icon(Icons.Filled.Close, contentDescription = "Close menu", tint = PunchIvory)
            }
        }
        Spacer(Modifier.height(12.dp))
        DrawerItem(
            icon = { Icon(Icons.Filled.Edit, contentDescription = null, tint = PunchIvory) },
            label = "New chat",
            onClick = onNewChat,
            testTag = "drawer_new_chat",
        )
        DrawerItem(
            icon = { Icon(Icons.Filled.Search, contentDescription = null, tint = PunchIvory) },
            label = "Search chats",
            onClick = onSearch,
        )
        Spacer(Modifier.height(20.dp))
        Text("Bundles", style = MaterialTheme.typography.bodySmall, color = PunchMuted)
        Spacer(Modifier.height(8.dp))
        DrawerItem(
            icon = { Icon(Icons.Filled.Add, contentDescription = null, tint = PunchIvory) },
            label = "New bundle",
            onClick = onNewBundle,
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            bundles.forEach { bundle ->
                DrawerItem(
                    icon = { Icon(Icons.Filled.List, contentDescription = null, tint = PunchIvory) },
                    label = bundle.name,
                    onClick = { onOpenBundle(bundle) },
                    onDelete = { onDeleteBundle(bundle) },
                )
            }
            Spacer(Modifier.height(20.dp))
            Text("Recents", style = MaterialTheme.typography.bodySmall, color = PunchMuted)
            Spacer(Modifier.height(8.dp))
            recents.forEach { chat ->
                val selected = chat.id == activeChatId
                DeleteMenuBox(
                    onClick = { onOpenChat(chat) },
                    onDelete = { onDeleteChat(chat) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(50))
                        .background(if (selected) PunchCharcoal else Color.Transparent)
                        .testTag("recent_${chat.id}"),
                ) {
                    Text(
                        text = chat.title,
                        color = PunchIvory,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(PunchMint.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center,
            ) {
                Text("P", color = PunchMint, style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.width(12.dp))
            Text("Local", color = PunchIvory, modifier = Modifier.weight(1f))
            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .testTag("settings_button")
                    .semantics { contentDescription = "Settings" },
            ) {
                Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = PunchIvory)
            }
        }
    }
}

@Composable
private fun DrawerItem(
    icon: @Composable () -> Unit,
    label: String,
    onClick: () -> Unit,
    onDelete: (() -> Unit)? = null,
    testTag: String? = null,
) {
    val body: @Composable () -> Unit = {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(24.dp), contentAlignment = Alignment.Center) { icon() }
            Spacer(Modifier.width(16.dp))
            Text(
                label,
                color = PunchIvory,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
    val shape = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(50))
        .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
    if (onDelete == null) {
        Box(shape.clickable(onClick = onClick)) { body() }
    } else {
        DeleteMenuBox(onClick = onClick, onDelete = onDelete, modifier = shape) { body() }
    }
}
