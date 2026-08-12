package com.punch.android.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MarkdownTextTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun stripsInlineMarkers() {
        composeRule.setContent {
            MarkdownText("Hello **world** with `code` and *italic*")
        }
        composeRule.onNodeWithText("Hello world with code and italic").assertExists()
    }

    @Test
    fun rendersHeading() {
        composeRule.setContent {
            MarkdownText("# Title\nplain")
        }
        composeRule.onNodeWithText("Title").assertExists()
        composeRule.onNodeWithText("plain").assertExists()
    }

    @Test
    fun rendersListAndCodeBlock() {
        composeRule.setContent {
            MarkdownText("- one\n- two\n\n```kotlin\nval x = 1\n```")
        }
        composeRule.onNodeWithText("•  one").assertExists()
        composeRule.onNodeWithText("•  two").assertExists()
        composeRule.onNodeWithText("val x = 1").assertExists()
    }
}
