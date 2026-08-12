package com.punch.android.data

object PromptComposer {
    fun compose(userText: String, bundles: List<BundleMemory>): String {
        if (bundles.isEmpty()) return userText
        val blocks = bundles.joinToString("\n\n") { bundle ->
            buildString {
                append("Bundle: ${bundle.name}")
                if (bundle.instructions.isNotBlank()) {
                    append("\nInstructions:\n")
                    append(bundle.instructions.trim())
                }
                bundle.files.forEach { file ->
                    append("\nFile: ${file.name}")
                    if (file.text.isNotBlank()) {
                        append("\n")
                        append(file.text.trim())
                    }
                }
            }
        }
        return buildString {
            appendLine("The following bundle memory is shared project context.")
            appendLine("Use it in this chat, and when another chat cross-references these bundles.")
            appendLine()
            appendLine(blocks)
            appendLine()
            appendLine("User:")
            append(userText)
        }.trim()
    }
}
