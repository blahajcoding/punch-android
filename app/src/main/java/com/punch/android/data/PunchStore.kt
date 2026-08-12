package com.punch.android.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

class PunchStore(context: Context) {
    private val file = File(context.applicationContext.filesDir, "punch_state.json")
    val attachmentsDir = File(context.applicationContext.filesDir, "attachments").apply { mkdirs() }

    fun load(): PunchState {
        if (!file.exists()) return PunchState()
        return runCatching { decode(JSONObject(file.readText())) }.getOrDefault(PunchState())
    }

    fun save(state: PunchState) {
        file.writeText(encode(state).toString())
    }

    fun newId(): String = UUID.randomUUID().toString()

    fun attachmentFile(id: String): File = File(attachmentsDir, id)

    companion object {
        fun decode(root: JSONObject): PunchState {
            return PunchState(
                chats = root.optJSONArray("chats").objects().map { chat ->
                    ChatThread(
                        id = chat.getString("id"),
                        title = chat.optString("title").ifBlank { "New chat" },
                        bundleId = chat.optString("bundleId").takeIf { it.isNotBlank() },
                        crossRefBundleIds = chat.optJSONArray("crossRefBundleIds").strings(),
                        sessionId = chat.optString("sessionId").takeIf { it.isNotBlank() },
                        createdAt = chat.optLong("createdAt"),
                        updatedAt = chat.optLong("updatedAt"),
                        messages = chat.optJSONArray("messages").objects().map { message ->
                            ChatMessage(
                                id = message.getString("id"),
                                role = message.optString("role"),
                                text = message.optString("text"),
                                attachmentNames = message.optJSONArray("attachmentNames").strings(),
                            )
                        },
                        pendingAttachments = chat.optJSONArray("pendingAttachments").objects().map { storedFile(it) },
                        workingLabel = chat.optString("workingLabel"),
                    )
                },
                bundles = root.optJSONArray("bundles").objects().map { bundle ->
                    Bundle(
                        id = bundle.getString("id"),
                        name = bundle.optString("name").ifBlank { "Untitled bundle" },
                        instructions = bundle.optString("instructions"),
                        files = bundle.optJSONArray("files").objects().map { storedFile(it) },
                        createdAt = bundle.optLong("createdAt"),
                    )
                },
                activeChatId = root.optString("activeChatId").takeIf { it.isNotBlank() },
            )
        }

        fun encode(state: PunchState): JSONObject {
            return JSONObject()
                .put("activeChatId", state.activeChatId.orEmpty())
                .put(
                    "chats",
                    JSONArray().also { array ->
                        state.chats.forEach { chat ->
                            array.put(
                                JSONObject()
                                    .put("id", chat.id)
                                    .put("title", chat.title)
                                    .put("bundleId", chat.bundleId.orEmpty())
                                    .put("crossRefBundleIds", JSONArray(chat.crossRefBundleIds))
                                    .put("sessionId", chat.sessionId.orEmpty())
                                    .put("createdAt", chat.createdAt)
                                    .put("updatedAt", chat.updatedAt)
                                    .put("workingLabel", chat.workingLabel)
                                    .put(
                                        "messages",
                                        JSONArray().also { messages ->
                                            chat.messages.forEach { message ->
                                                messages.put(
                                                    JSONObject()
                                                        .put("id", message.id)
                                                        .put("role", message.role)
                                                        .put("text", message.text)
                                                        .put("attachmentNames", JSONArray(message.attachmentNames)),
                                                )
                                            }
                                        },
                                    )
                                    .put(
                                        "pendingAttachments",
                                        JSONArray().also { pending ->
                                            chat.pendingAttachments.forEach { pending.put(storedFileJson(it)) }
                                        },
                                    ),
                            )
                        }
                    },
                )
                .put(
                    "bundles",
                    JSONArray().also { array ->
                        state.bundles.forEach { bundle ->
                            array.put(
                                JSONObject()
                                    .put("id", bundle.id)
                                    .put("name", bundle.name)
                                    .put("instructions", bundle.instructions)
                                    .put("createdAt", bundle.createdAt)
                                    .put(
                                        "files",
                                        JSONArray().also { files ->
                                            bundle.files.forEach { files.put(storedFileJson(it)) }
                                        },
                                    ),
                            )
                        }
                    },
                )
        }

        private fun storedFile(obj: JSONObject) = StoredFile(
            id = obj.getString("id"),
            name = obj.optString("name"),
            mime = obj.optString("mime"),
            path = obj.optString("path"),
        )

        private fun storedFileJson(file: StoredFile) = JSONObject()
            .put("id", file.id)
            .put("name", file.name)
            .put("mime", file.mime)
            .put("path", file.path)

        private fun JSONArray?.objects(): List<JSONObject> {
            if (this == null) return emptyList()
            return (0 until length()).mapNotNull { optJSONObject(it) }
        }

        private fun JSONArray?.strings(): List<String> {
            if (this == null) return emptyList()
            return (0 until length()).mapNotNull { optString(it).takeIf { value -> value.isNotBlank() } }
        }
    }
}
