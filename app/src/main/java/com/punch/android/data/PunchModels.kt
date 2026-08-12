package com.punch.android.data

data class PunchState(
    val chats: List<ChatThread> = emptyList(),
    val bundles: List<Bundle> = emptyList(),
    val activeChatId: String? = null,
) {
    fun activeChat(): ChatThread? = chats.firstOrNull { it.id == activeChatId }

    fun chatsInBundle(bundleId: String): List<ChatThread> =
        chats.filter { it.bundleId == bundleId }.sortedByDescending { it.updatedAt }

    fun recents(): List<ChatThread> = chats.sortedByDescending { it.updatedAt }
}

data class ChatThread(
    val id: String,
    val title: String = "New chat",
    val bundleId: String? = null,
    val crossRefBundleIds: List<String> = emptyList(),
    val sessionId: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
    val messages: List<ChatMessage> = emptyList(),
    val pendingAttachments: List<StoredFile> = emptyList(),
    val workingLabel: String = "",
)

data class ChatMessage(
    val id: String,
    val role: String,
    val text: String,
    val attachmentNames: List<String> = emptyList(),
)

data class Bundle(
    val id: String,
    val name: String,
    val instructions: String = "",
    val files: List<StoredFile> = emptyList(),
    val createdAt: Long,
)

data class StoredFile(
    val id: String,
    val name: String,
    val mime: String,
    val path: String,
)

data class BundleMemory(
    val name: String,
    val instructions: String,
    val files: List<NamedText>,
)

data class NamedText(
    val name: String,
    val text: String,
)

data class OutboundPart(
    val type: String,
    val text: String? = null,
    val mimeType: String? = null,
    val filename: String? = null,
    val data: String? = null,
)
