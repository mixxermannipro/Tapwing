// SPDX-License-Identifier: GPL-3.0-only
package helium314.keyboard.latin.swipe

import helium314.keyboard.keyboard.Keyboard
import tribixbite.cleverkeys.swipe.geometric.LayoutGeometry
import tribixbite.cleverkeys.swipe.geometric.SwipeKey

internal data class SwipeKeyBounds(val code: Int, val x: Int, val y: Int, val width: Int, val height: Int)

internal class SwipeLayout private constructor(
    val width: Float,
    val height: Float,
    val geometry: LayoutGeometry,
) {
    companion object {
        fun fromKeyboard(keyboard: Keyboard): SwipeLayout? =
            create(keyboard.mOccupiedWidth, keyboard.mOccupiedHeight, keyboard.sortedKeys.map {
                SwipeKeyBounds(it.code, it.x, it.y, it.width, it.height)
            })

        fun create(width: Int, height: Int, bounds: List<SwipeKeyBounds>): SwipeLayout? {
            if (width <= 0 || height <= 0) return null
            val letters = bounds.filter { Character.isLetter(it.code) && it.width > 0 && it.height > 0 }
                .sortedWith(compareBy(SwipeKeyBounds::y, SwipeKeyBounds::x))
            if (letters.size < 2) return null
            val rows = letters.map(SwipeKeyBounds::y).distinct()
            val nextColumn = IntArray(rows.size)
            val chars = mutableMapOf<Int, MutableList<Int>>()
            val keys = letters.mapIndexed { id, key ->
                val code = Character.toLowerCase(key.code)
                chars.getOrPut(code) { mutableListOf() }.add(id)
                val row = rows.indexOf(key.y)
                SwipeKey(id, String(Character.toChars(code)), (key.x + key.width / 2f) / width,
                    (key.y + key.height / 2f) / height, key.width.toFloat() / width,
                    key.height.toFloat() / height, row, nextColumn[row]++, true)
            }
            // Standard German layouts expose ß as a popup on S.
            val sharpSAlias = chars['s'.code]?.singleOrNull()
            val aliases = if ('ß'.code !in chars && sharpSAlias != null) mapOf('ß'.code to sharpSAlias) else emptyMap()
            val geometry = LayoutGeometry(keys, chars.mapValues { it.value.toIntArray() },
                aliases, width.toFloat() / height, keys.map(SwipeKey::w).average().toFloat())
            return SwipeLayout(width.toFloat(), height.toFloat(), geometry)
        }
    }
}
