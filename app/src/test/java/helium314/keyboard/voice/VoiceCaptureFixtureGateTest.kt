// SPDX-License-Identifier: GPL-3.0-or-later
package helium314.keyboard.voice

import android.text.InputType
import android.view.inputmethod.EditorInfo
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class VoiceCaptureFixtureGateTest {
    @Test fun onlyExplicitFixtureEditorIsEligible() {
        val editor = fixtureEditor()
        assertTrue(isVoiceCaptureFixtureEditor(editor))
        editor.packageName = "example.other"
        assertFalse(isVoiceCaptureFixtureEditor(editor))
        editor.packageName = "io.github.mixxermannipro.satzfink.smoke"
        editor.privateImeOptions = null
        assertFalse(isVoiceCaptureFixtureEditor(editor))
        assertFalse(isVoiceCaptureFixtureEditor(null))
    }

    @Test fun protectedAndUnclassifiedFieldsAreExcludedEvenWithMarker() {
        val editor = fixtureEditor()
        editor.imeOptions = EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING
        assertFalse(isVoiceCaptureFixtureEditor(editor))
        editor.imeOptions = 0
        for (type in intArrayOf(InputType.TYPE_NULL,
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD,
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD,
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD,
                InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD,
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)) {
            editor.inputType = type
            assertFalse(isVoiceCaptureFixtureEditor(editor))
        }
    }

    private fun fixtureEditor() = EditorInfo().apply {
        packageName = "io.github.mixxermannipro.satzfink.smoke"
        privateImeOptions = "io.github.mixxermannipro.satzfink.smoke.VOICE_CAPTURE"
        inputType = InputType.TYPE_CLASS_TEXT
    }
}
