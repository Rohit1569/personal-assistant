package com.example.myapplication

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SchedulingVoiceTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testVoiceCommandParsingAndListUpdate() {
        // 1. Check if the screen loaded
        composeTestRule.onNodeWithText("Scheduling Assistant").assertIsDisplayed()

        // 2. Simulate a voice result being injected into the app
        composeTestRule.runOnUiThread {
            composeTestRule.activity.injectVoiceText("book a haircut")
        }

        // 3. Verify that the UI updated with the success message
        composeTestRule.onNodeWithText("Scheduled: Voice Booking: Haircut").assertIsDisplayed()

        // 4. Verify that the appointment appears in the list
        composeTestRule.onNodeWithText("Voice Booking: Haircut").assertIsDisplayed()
        composeTestRule.onNodeWithText("Type: Haircut").assertIsDisplayed()
    }

    @Test
    fun testUnrecognizedVoiceCommand() {
        // Simulate a command the system doesn't know
        composeTestRule.runOnUiThread {
            composeTestRule.activity.injectVoiceText("hello assistant")
        }

        // Verify the error message appears
        composeTestRule.onNodeWithText("Could not understand: hello assistant").assertIsDisplayed()
    }
}
