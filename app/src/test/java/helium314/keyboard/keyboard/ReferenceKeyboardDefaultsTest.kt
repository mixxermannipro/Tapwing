// SPDX-License-Identifier: GPL-3.0-only
package helium314.keyboard.keyboard

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import helium314.keyboard.latin.common.ColorType
import helium314.keyboard.latin.settings.Defaults
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class ReferenceKeyboardDefaultsTest {
    @Test
    fun defaultPaletteAndKeyTreatmentMatchTheReference() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val colors = KeyboardTheme.getColorsForCurrentTheme(context)
        assertEquals(KeyboardTheme.STYLE_ROUNDED, colors.themeStyle)
        assertTrue(colors.hasKeyBorders)
        assertEquals(0xff2b2b2d.toInt(), colors.get(ColorType.MAIN_BACKGROUND))
        assertEquals(0xff46454b.toInt(), colors.get(ColorType.KEY_BACKGROUND))
        assertEquals(0xff3f4660.toInt(), colors.get(ColorType.ACTION_KEY_BACKGROUND))
    }

    @Test
    fun defaultRowsAndToolbarStillAllowSuggestions() {
        assertTrue(Defaults.PREF_SHOW_NUMBER_ROW)
        assertFalse(Defaults.PREF_SHOW_HINTS)
        assertEquals("EXPANDABLE", Defaults.PREF_TOOLBAR_MODE)
        assertTrue(Defaults.PREF_AUTO_SHOW_TOOLBAR)
        assertTrue(Defaults.PREF_AUTO_HIDE_TOOLBAR)
    }
}
