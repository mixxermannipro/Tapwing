// SPDX-License-Identifier: GPL-3.0-only
package helium314.keyboard.latin

import android.text.InputType
import android.view.inputmethod.EditorInfo
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class InputPrivacyTest {
    @Test
    fun numericEditorHonorsNoPersonalizedLearning() {
        val editor = EditorInfo().apply {
            inputType = InputType.TYPE_CLASS_NUMBER
            imeOptions = EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING
        }
        assertTrue(InputAttributes(editor, false, "").mNoLearning)
    }

    @Test
    fun passwordFieldsIgnoreRequestsForAutocorrection() {
        val variations = listOf(
            InputType.TYPE_TEXT_VARIATION_PASSWORD,
            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD,
            InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD,
        )
        for (variation in variations) {
            val editor = EditorInfo().apply {
                inputType = InputType.TYPE_CLASS_TEXT or variation or InputType.TYPE_TEXT_FLAG_AUTO_CORRECT
            }
            val attributes = InputAttributes(editor, false, "")
            assertTrue(attributes.mIsPasswordField)
            assertFalse(attributes.mInputTypeShouldAutoCorrect)
            assertFalse(attributes.mShouldShowSuggestions)
        }
    }

    @Test
    fun unprotectedEditorStillSupportsAutocorrection() {
        val editor = EditorInfo().apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_AUTO_CORRECT
        }
        val attributes = InputAttributes(editor, false, "")
        assertTrue(attributes.mInputTypeShouldAutoCorrect)
        assertTrue(attributes.mShouldShowSuggestions)
        assertFalse(attributes.mNoLearning)
    }
}
