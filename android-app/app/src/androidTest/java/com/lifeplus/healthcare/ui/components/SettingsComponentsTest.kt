package com.lifeplus.healthcare.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Compose UI tests for [SettingsToggleItem] and [SettingsClickItem].
 *
 * Requirements: 5.5, 5.6
 */
@RunWith(AndroidJUnit4::class)
class SettingsComponentsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    /** Matcher that finds a node with [Role.Switch] semantics. */
    private val isSwitchRole = SemanticsMatcher.expectValue(
        SemanticsProperties.Role, Role.Switch
    )

    // ── SettingsToggleItem ────────────────────────────────────────────────────

    /**
     * Requirement 5.5: When a SettingsToggleItem switch is changed, the
     * SettingsToggleItem SHALL invoke the provided onCheckedChange callback
     * with the new boolean value.
     *
     * Renders the item with checked=false, performs a click on the Switch,
     * and asserts the callback was invoked with `true`.
     */
    @Test
    fun settingsToggleItem_whenSwitchClicked_invokesOnCheckedChangeWithTrue() {
        var callbackValue: Boolean? = null

        composeTestRule.setContent {
            SettingsToggleItem(
                label = "Test Toggle",
                checked = false,
                onCheckedChange = { newValue -> callbackValue = newValue }
            )
        }

        // The Switch role is used by Compose for the Switch component
        composeTestRule
            .onNodeWithText("Test Toggle")
            .assertIsDisplayed()

        // Click the Switch — Compose's Switch is a semantics node with Role.Switch
        composeTestRule
            .onNode(isSwitchRole)
            .performClick()

        assertEquals(
            "onCheckedChange should be called with true when switch is toggled from false",
            true,
            callbackValue
        )
    }

    /**
     * Verifies that toggling a checked SettingsToggleItem invokes the callback
     * with `false`.
     */
    @Test
    fun settingsToggleItem_whenCheckedSwitchClicked_invokesOnCheckedChangeWithFalse() {
        var callbackValue: Boolean? = null

        composeTestRule.setContent {
            SettingsToggleItem(
                label = "Checked Toggle",
                checked = true,
                onCheckedChange = { newValue -> callbackValue = newValue }
            )
        }

        composeTestRule
            .onNode(isSwitchRole)
            .performClick()

        assertEquals(
            "onCheckedChange should be called with false when switch is toggled from true",
            false,
            callbackValue
        )
    }

    /**
     * Verifies that the label text is displayed in SettingsToggleItem.
     */
    @Test
    fun settingsToggleItem_displaysLabel() {
        composeTestRule.setContent {
            SettingsToggleItem(
                label = "Notifications",
                checked = false,
                onCheckedChange = {}
            )
        }

        composeTestRule
            .onNodeWithText("Notifications")
            .assertIsDisplayed()
    }

    /**
     * Verifies that the optional subtitle is displayed when provided.
     */
    @Test
    fun settingsToggleItem_displaysSubtitleWhenProvided() {
        composeTestRule.setContent {
            SettingsToggleItem(
                label = "Push Notifications",
                subtitle = "Receive alerts for updates",
                checked = false,
                onCheckedChange = {}
            )
        }

        composeTestRule
            .onNodeWithText("Receive alerts for updates")
            .assertIsDisplayed()
    }

    // ── SettingsClickItem ─────────────────────────────────────────────────────

    /**
     * Requirement 5.6: When a SettingsClickItem is tapped, the SettingsClickItem
     * SHALL invoke the provided onClick callback.
     */
    @Test
    fun settingsClickItem_whenTapped_invokesOnClick() {
        var clickCount = 0

        composeTestRule.setContent {
            SettingsClickItem(
                label = "Privacy Policy",
                leadingIcon = Icons.Default.Settings,
                onClick = { clickCount++ }
            )
        }

        composeTestRule
            .onNodeWithText("Privacy Policy")
            .performClick()

        assertTrue(
            "onClick should be invoked when SettingsClickItem is tapped",
            clickCount > 0
        )
    }

    /**
     * Verifies that the label is displayed in SettingsClickItem.
     */
    @Test
    fun settingsClickItem_displaysLabel() {
        composeTestRule.setContent {
            SettingsClickItem(
                label = "Terms of Service",
                leadingIcon = Icons.Default.Settings,
                onClick = {}
            )
        }

        composeTestRule
            .onNodeWithText("Terms of Service")
            .assertIsDisplayed()
    }

    /**
     * Verifies that the optional subtitle is displayed when provided.
     */
    @Test
    fun settingsClickItem_displaysSubtitleWhenProvided() {
        composeTestRule.setContent {
            SettingsClickItem(
                label = "Contact Support",
                subtitle = "support@lifeplus.health",
                leadingIcon = Icons.Default.Settings,
                onClick = {}
            )
        }

        composeTestRule
            .onNodeWithText("support@lifeplus.health")
            .assertIsDisplayed()
    }

    /**
     * Verifies that the optional badge is displayed when provided.
     */
    @Test
    fun settingsClickItem_displaysBadgeWhenProvided() {
        composeTestRule.setContent {
            SettingsClickItem(
                label = "Dark Mode",
                leadingIcon = Icons.Default.Settings,
                onClick = {},
                badge = "Coming Soon"
            )
        }

        composeTestRule
            .onNodeWithText("Coming Soon")
            .assertIsDisplayed()
    }

    /**
     * Verifies that onClick is only invoked once per tap (not multiple times).
     */
    @Test
    fun settingsClickItem_singleTap_invokesOnClickExactlyOnce() {
        var clickCount = 0

        composeTestRule.setContent {
            SettingsClickItem(
                label = "Rate the App",
                leadingIcon = Icons.Default.Settings,
                onClick = { clickCount++ }
            )
        }

        composeTestRule
            .onNodeWithText("Rate the App")
            .performClick()

        assertEquals(
            "onClick should be invoked exactly once per tap",
            1,
            clickCount
        )
    }
}
