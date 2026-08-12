package com.punch.android.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

class PromptComposerTest {
    @Test
    fun withoutBundlesReturnsUserText() {
        assertEquals("hello", PromptComposer.compose("hello", emptyList()))
    }

    @Test
    fun includesBundleInstructionsAndFiles() {
        val composed = PromptComposer.compose(
            userText = "summarize this",
            bundles = listOf(
                BundleMemory(
                    name = "Munich",
                    instructions = "Prefer primary sources.",
                    files = listOf(NamedText("notes.md", "Treaty notes")),
                ),
            ),
        )
        assertTrue(composed.contains("Bundle: Munich"))
        assertTrue(composed.contains("Prefer primary sources."))
        assertTrue(composed.contains("Treaty notes"))
        assertTrue(composed.contains("summarize this"))
        assertTrue(composed.contains("cross-references"))
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PunchStoreCodecTest {
    @Test
    fun roundTripsState() {
        val original = PunchState(
            activeChatId = "c1",
            chats = listOf(
                ChatThread(
                    id = "c1",
                    title = "Hello",
                    bundleId = "b1",
                    crossRefBundleIds = listOf("b2"),
                    sessionId = "sess",
                    createdAt = 1,
                    updatedAt = 2,
                    messages = listOf(ChatMessage("m1", "user", "hi")),
                ),
            ),
            bundles = listOf(
                Bundle(id = "b1", name = "Project", instructions = "Be brief", createdAt = 3),
            ),
        )
        val restored = PunchStore.decode(PunchStore.encode(original))
        assertEquals("c1", restored.activeChatId)
        assertEquals("Hello", restored.chats.single().title)
        assertEquals(listOf("b2"), restored.chats.single().crossRefBundleIds)
        assertEquals("Project", restored.bundles.single().name)
        assertFalse(restored.chatsInBundle("missing").isNotEmpty())
        assertEquals(1, restored.chatsInBundle("b1").size)
    }
}

class PunchStateDeleteTest {
    private fun chat(id: String, bundleId: String? = null, refs: List<String> = emptyList(), updatedAt: Long = 1) =
        ChatThread(
            id = id,
            title = id,
            bundleId = bundleId,
            crossRefBundleIds = refs,
            createdAt = 1,
            updatedAt = updatedAt,
        )

    @Test
    fun deletingActiveChatSelectsNewestRemaining() {
        val state = PunchState(
            activeChatId = "a",
            chats = listOf(chat("a", updatedAt = 1), chat("b", updatedAt = 3), chat("c", updatedAt = 2)),
        ).withoutChat("a")
        assertEquals("b", state.activeChatId)
        assertEquals(listOf("b", "c"), state.chats.map { it.id })
    }

    @Test
    fun deletingBundleUnlinksChatsAndCrossRefs() {
        val state = PunchState(
            activeChatId = "a",
            chats = listOf(chat("a", bundleId = "b1", refs = listOf("b1", "b2"))),
            bundles = listOf(
                Bundle(id = "b1", name = "One", createdAt = 1),
                Bundle(id = "b2", name = "Two", createdAt = 2),
            ),
        ).withoutBundle("b1")
        assertTrue(state.bundles.none { it.id == "b1" })
        assertEquals(null, state.chats.single().bundleId)
        assertEquals(listOf("b2"), state.chats.single().crossRefBundleIds)
    }
}
