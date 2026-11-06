// SPDX-License-Identifier: GPL-3.0-only
package helium314.keyboard.latin.swipe

import tribixbite.cleverkeys.PredictionResult
import tribixbite.cleverkeys.swipe.geometric.ArrayBackedDictionary
import tribixbite.cleverkeys.swipe.geometric.GeometricEngineConfig
import tribixbite.cleverkeys.swipe.geometric.GeometricSwipeEngine
import tribixbite.cleverkeys.swipe.geometric.GeometricSwipeRequest
import tribixbite.cleverkeys.swipe.geometric.TracePoint
import java.io.InputStream
import java.util.Locale
import java.util.zip.GZIPInputStream

/** One instance per worker. Only bundled, non-personal word lists are read. */
internal class OpenSwipeDecoder(private val openAsset: (String) -> InputStream) {
    private val engine = GeometricSwipeEngine(GeometricEngineConfig(
        maxResults = 6, templateMemoCapacity = 384, indexCacheCapacity = 1,
    ))
    private var cachedWords: ArrayBackedDictionary? = null

    fun prepare(locale: Locale, layout: SwipeLayout) {
        val dictionary = wordsFor(locale) ?: return
        engine.warmUp(layout.geometry, dictionary)
    }

    fun decode(locale: Locale, layout: SwipeLayout, points: List<TracePoint>): PredictionResult {
        val dictionary = wordsFor(locale) ?: return PredictionResult(emptyList(), emptyList())
        return engine.decode(GeometricSwipeRequest(points, layout.width, layout.height, layout.geometry, dictionary))
    }

    private fun wordsFor(locale: Locale): ArrayBackedDictionary? {
        val language = when (locale.language) {
            "de" -> "de"
            "en" -> if (locale.country == "GB") "en-GB" else "en-US"
            else -> return null
        }
        cachedWords?.takeIf { it.language == language }?.let { return it }
        val words = openAsset("swipe/$language.lex").use { compressed ->
            GZIPInputStream(compressed).bufferedReader(Charsets.UTF_8).useLines { it.toList().toTypedArray() }
        }
        return ArrayBackedDictionary(language, 1L, words).also { cachedWords = it }
    }
}
