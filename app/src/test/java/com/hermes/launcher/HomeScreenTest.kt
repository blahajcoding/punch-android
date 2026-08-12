package com.hermes.launcher

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class HomeScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun homeScreenRendersTitle() {
        composeRule.onNodeWithTag("hermes_title").assertIsDisplayed()
        composeRule.onNodeWithText("pincher").assertIsDisplayed()
    }

    @Test
    fun textInputUpdates() {
        composeRule.onNodeWithTag("agent_input").performTextInput("hello agent")
        composeRule.onNodeWithTag("agent_input").assertTextEquals("Ask pincher", "hello agent")
    }

    @Test
    fun appDrawerOpens() {
        composeRule.onNodeWithTag("app_drawer_button").performClick()
        composeRule.onNodeWithTag("apps_header").assertIsDisplayed()
        composeRule.onNodeWithText("Apps").assertIsDisplayed()
    }

    @Test
    fun wallpaperToggleIsVisibleInSettings() {
        composeRule.onNodeWithContentDescription("Settings").performClick()
        composeRule.onNodeWithTag("wallpaper_toggle").assertIsDisplayed()
        composeRule.onNodeWithText("System wallpaper").assertIsDisplayed()
    }
}
