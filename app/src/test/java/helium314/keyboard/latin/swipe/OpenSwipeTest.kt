// SPDX-License-Identifier: GPL-3.0-only
package helium314.keyboard.latin.swipe

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import tribixbite.cleverkeys.swipe.geometric.TracePoint
import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class OpenSwipeTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val decoder = OpenSwipeDecoder { context.assets.open(it) }

    @Test
    fun englishWordUsesBundledFrequencyList() {
        val layout = layout("qwertyuiop", "asdfghjkl", "zxcvbnm")
        val results = decoder.decode(Locale.US, layout, trace(layout, "world"))
        assertEquals("world", results.words.first())
        assertTrue(results.words.size > 1)
    }

    @Test
    fun germanWordRetainsItsUmlaut() {
        val layout = layout("qwertzuiop", "asdfghjkl", "yxcvbnm")
        val results = decoder.decode(Locale.GERMANY, layout, trace(layout, "strase"))
        assertEquals("Straße", results.words.first())
        assertTrue("fünf" in decoder.decode(Locale.GERMANY, layout, trace(layout, "funf")).words)
    }

    @Test
    fun unsupportedLocaleAndStationaryTraceReturnNoSuggestions() {
        val layout = layout("qwertyuiop", "asdfghjkl", "zxcvbnm")
        assertTrue(decoder.decode(Locale.FRANCE, layout, trace(layout, "hello")).words.isEmpty())
        assertTrue(decoder.decode(Locale.US, layout, List(8) { TracePoint(50f, 50f, it.toLong()) }).words.isEmpty())
    }

    private fun layout(vararg rows: String): SwipeLayout = SwipeLayout.create(1000, 400, rows.flatMapIndexed { row, keys ->
        keys.mapIndexed { column, key ->
            SwipeKeyBounds(key.code, column * 100 + row * 25, row * 100, 100, 100)
        }
    })!!

    private fun trace(layout: SwipeLayout, word: String): List<TracePoint> {
        val centers = word.map { letter -> layout.geometry.keys.first { it.label == letter.toString() } }
        return centers.zipWithNext().flatMapIndexed { segment, (a, b) ->
            (0..9).map { sample ->
                val fraction = sample / 9f
                TracePoint((a.cx + (b.cx - a.cx) * fraction) * layout.width,
                    (a.cy + (b.cy - a.cy) * fraction) * layout.height, segment * 100L + sample * 10L)
            }
        }
    }
}
