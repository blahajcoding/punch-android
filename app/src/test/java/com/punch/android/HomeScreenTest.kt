package com.punch.android

import androidx.compose.ui.test.assertIsDisplayed
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
    fun homeScreenRenders() {
        composeRule.onNodeWithTag("agent_input").assertIsDisplayed()
        composeRule.onNodeWithText("The mic is yours").assertIsDisplayed()
        composeRule.onNodeWithTag("app_title").assertIsDisplayed()
    }

    @Test
    fun textInputUpdates() {
        composeRule.onNodeWithTag("agent_input").performTextInput("hello agent")
        composeRule.onNodeWithText("hello agent").assertIsDisplayed()
    }

    @Test
    fun drawerOpensFromMenu() {
        composeRule.onNodeWithContentDescription("Menu").performClick()
        composeRule.onNodeWithTag("punch_drawer").assertIsDisplayed()
        composeRule.onNodeWithTag("drawer_new_chat").assertIsDisplayed()
        composeRule.onNodeWithText("Bundles").assertIsDisplayed()
        composeRule.onNodeWithText("Recents").assertIsDisplayed()
    }

    @Test
    fun settingsOpensFromMenu() {
        composeRule.onNodeWithContentDescription("More").performClick()
        composeRule.onNodeWithText("Settings").performClick()
        composeRule.onNodeWithTag("gateway_url_field").assertIsDisplayed()
        composeRule.onNodeWithTag("package_name").assertIsDisplayed()
    }
}
