// SPDX-License-Identifier: GPL-3.0-or-later
package helium314.keyboard.settings

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import java.io.File
import helium314.keyboard.settings.screens.PreferencesScreen
import helium314.keyboard.settings.screens.TextCorrectionScreen
import helium314.keyboard.settings.screens.ClipboardSettingsScreen
import helium314.keyboard.settings.screens.AdvancedSettingsScreen
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.performScrollTo
import kotlin.test.assertEquals
import helium314.keyboard.settings.screens.SettingsHomeContent
import helium314.keyboard.settings.screens.PlannedFeatureScreen
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import helium314.keyboard.settings.preferences.SliderPreference
import kotlin.test.assertFalse
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithContentDescription
import helium314.keyboard.latin.R
import helium314.keyboard.latin.settings.Settings
import helium314.keyboard.keyboard.KeyboardTheme
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import helium314.keyboard.latin.utils.Theme
import helium314.keyboard.latin.utils.prefs
import helium314.keyboard.settings.preferences.SwitchPreference
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SettingsUiTest {
    @get:Rule val compose = createComposeRule()
    private val context get() = ApplicationProvider.getApplicationContext<Application>()

    @Before fun reset() { context.prefs().edit().clear().commit() }

    @Test fun rowTogglePersistsAndReflectsExternalChanges() {
        compose.setContent {
            Theme(dark = true) {
                SwitchPreference(name = "Zifferntastenzeile", key = "ui_test_number_row", default = false)
            }
        }
        compose.onNode(isToggleable()).assertIsOff()
        compose.onNodeWithText("Zifferntastenzeile").performClick()
        compose.waitForIdle()
        assertTrue(context.prefs().getBoolean("ui_test_number_row", false))
        compose.onNode(isToggleable()).assertIsOn()
        compose.runOnIdle { context.prefs().edit().putBoolean("ui_test_number_row", false).commit() }
        compose.onNode(isToggleable()).assertIsOff()
    }
    @Test fun homeLinksOpenTheirOwnDestinations() {
        var target = ""
        compose.setContent {
            Theme(dark = true) {
                SettingsHomeContent(languageSummary = "Deutsch (Deutschland) (QWERTZ)", onNavigate = { target = it })
            }
        }
        compose.onNodeWithText("Clipboard").performScrollTo().performClick()
        compose.runOnIdle { assertEquals("clipboard", target) }
        compose.onNodeWithText("Privacy").performScrollTo().performClick()
        compose.runOnIdle { assertEquals("privacy", target) }
        compose.onNodeWithText("Languages").performScrollTo().performClick()
        compose.runOnIdle { assertEquals("languages", target) }
    }

    @Test fun plannedFeaturesAreClearlyDisabled() {
        compose.setContent {
            Theme(dark = true) {
                PlannedFeatureScreen(title = "Speech input", description = "Local dictation", onClickBack = {})
            }
        }
        compose.onNode(isToggleable()).assertIsOff().assertIsNotEnabled()
        compose.onNodeWithText("Not available yet", substring = true).assertExists()
    }

    @Test fun inlineSliderPersistsAndCanRestoreDefault() {
        compose.setContent {
            Theme(dark = true) {
                SliderPreference(
                    name = "Font size", key = "ui_test_font_scale", default = 1f,
                    description = { "${(it * 100).toInt()}%" }, range = 0.5f..1.5f, inline = true,
                )
            }
        }
        compose.onNode(SemanticsMatcher.keyIsDefined(SemanticsProperties.ProgressBarRangeInfo)).performTouchInput { swipe(center, centerRight, 200) }
        compose.runOnIdle { assertTrue(context.prefs().getFloat("ui_test_font_scale", 1f) > 1f) }
        compose.onNodeWithText("Use default").performClick()
        compose.runOnIdle { assertFalse(context.prefs().contains("ui_test_font_scale")) }
    }

    private fun saveCapture(name: String) {
        compose.waitForIdle()
        val target = File("build/outputs/settings-screenshots/$name.png")
        target.parentFile!!.mkdirs()
        // Robolectric has no device compositor for PixelCopy; draw the real Compose host
        // into a native bitmap after layout, on the UI thread.
        compose.runOnIdle {
            val activity = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED).single()
            val content = activity.findViewById<View>(android.R.id.content)
            val bitmap = Bitmap.createBitmap(content.width, content.height, Bitmap.Config.ARGB_8888)
            content.draw(Canvas(bitmap))
            target.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            bitmap.recycle()
        }
    }

    @Test
    @Config(qualifiers = "de-rDE-w400dp-h1280dp-mdpi")
    fun captureHomeReference() {
        compose.setContent {
            Theme(dark = true) { SettingsHomeContent("Deutsch (Deutschland) (QWERTZ)") {} }
        }
        saveCapture("home")
    }

    @Test
    @Config(qualifiers = "de-rDE-w400dp-h900dp-mdpi")
    fun capturePreferencesReference() {
        initPreview(context)
        compose.setContent { Theme(dark = true) { PreferencesScreen {} } }
        saveCapture("preferences")
    }

    @Test
    @Config(qualifiers = "de-rDE-w400dp-h1400dp-mdpi")
    fun captureCorrectionReference() {
        initPreview(context)
        compose.setContent { Theme(dark = true) { TextCorrectionScreen {} } }
        saveCapture("correction")
    }

    @Test
    @Config(qualifiers = "de-rDE-w400dp-h1000dp-mdpi")
    fun captureClipboardReference() {
        initPreview(context)
        compose.setContent { Theme(dark = true) { ClipboardSettingsScreen {} } }
        saveCapture("clipboard")
    }

    @Test
    @Config(qualifiers = "de-rDE-w400dp-h1100dp-mdpi")
    fun captureAdvancedReference() {
        compose.setContent { Theme(dark = true) { AdvancedSettingsScreen {} } }
        saveCapture("advanced")
    }

    @Test fun clipboardPageIsReachableAndBackReturnsToTheMenu() {
        initPreview(context)
        compose.setContent { Theme(dark = true) { SettingsNavHost(onClickBack = {}) } }
        compose.onNodeWithText("Clipboard").performScrollTo().performClick()
        compose.waitForIdle()
        compose.onNodeWithText(context.getString(R.string.enable_clipboard_history)).assertExists()
        compose.onNodeWithContentDescription(context.getString(R.string.spoken_description_action_previous)).performClick()
        compose.waitForIdle()
        compose.onNodeWithText("Languages").assertExists()
    }

    @Test fun roundedKeysUseTheExistingStylePreference() {
        initPreview(context)
        compose.setContent { Theme(dark = true) { AdvancedSettingsScreen {} } }
        val toggle = compose.onNode(isToggleable() and hasText("Rounded keys"))
        toggle.performScrollTo().assertIsOn().performClick()
        compose.runOnIdle { assertEquals("Material", context.prefs().getString(Settings.PREF_THEME_STYLE, null)) }
        toggle.assertIsOff()
        compose.runOnIdle { context.prefs().edit().putString(Settings.PREF_THEME_STYLE, KeyboardTheme.STYLE_ROUNDED).commit() }
        toggle.assertIsOn()
    }

    @Test
    @Config(qualifiers = "de-rDE-w400dp-h3300dp-mdpi")
    fun captureLongPreferencesReference() {
        initPreview(context)
        compose.setContent { Theme(dark = true) { PreferencesScreen {} } }
        saveCapture("preferences-long")
    }

}
