// SPDX-License-Identifier: GPL-3.0-only
package helium314.keyboard.latin.swipe

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import helium314.keyboard.keyboard.Keyboard
import helium314.keyboard.keyboard.MainKeyboardView
import helium314.keyboard.latin.CapsMode
import helium314.keyboard.latin.SuggestedWords
import helium314.keyboard.latin.SuggestedWords.SuggestedWordInfo
import helium314.keyboard.latin.common.InputPointers
import helium314.keyboard.latin.common.StringUtils
import helium314.keyboard.latin.dictionary.Dictionary
import helium314.keyboard.latin.utils.Log
import tribixbite.cleverkeys.swipe.geometric.TracePoint
import java.util.Locale
import java.util.function.Consumer

/** Called on the IME thread. Only immutable geometry and touch samples reach the decoder. */
class OpenSwipeWorker(context: Context) {
    private val decoder = OpenSwipeDecoder { name -> context.assets.open(name) }
    private var thread: HandlerThread? = null
    private var worker: Handler? = null
    private var preparedKeyboard: Keyboard? = null

    fun prepare(keyboard: Keyboard) {
        if (preparedKeyboard === keyboard || !keyboard.mId.element.isAlphabet || !supports(keyboard.mId.locale)) return
        val layout = SwipeLayout.fromKeyboard(keyboard) ?: return
        val locale = keyboard.mId.locale
        preparedKeyboard = keyboard
        handler().post {
            try {
                decoder.prepare(locale, layout)
            } catch (_: Exception) {
                Log.w(TAG, "Could not prepare the bundled swipe dictionary")
            }
        }
    }

    fun decode(
        keyboard: Keyboard,
        view: MainKeyboardView,
        input: InputPointers,
        caps: CapsMode,
        result: Consumer<SuggestedWords>,
    ) {
        val count = input.pointerSize
        if (count !in 3..MAX_CAPTURED_POINTS || !keyboard.mId.element.isAlphabet || !supports(keyboard.mId.locale)) {
            result.accept(SuggestedWords.getEmptyInstance())
            return
        }
        val pointer = input.pointerIds[0]
        if ((1 until count).any { input.pointerIds[it] != pointer }) {
            result.accept(SuggestedWords.getEmptyInstance())
            return
        }
        val layout = SwipeLayout.fromKeyboard(keyboard) ?: run {
            result.accept(SuggestedWords.getEmptyInstance())
            return
        }
        val locale = keyboard.mId.locale
        val samples = minOf(count, MAX_DECODER_POINTS)
        val points = List(samples) { sample ->
            val i = sample * (count - 1) / (samples - 1)
            TracePoint(view.getKeyX(input.xCoordinates[i]).toFloat(), view.getKeyY(input.yCoordinates[i]).toFloat(),
                input.times[i].toLong())
        }
        handler().post {
            val suggestions = try {
                val decoded = decoder.decode(locale, layout, points)
                val words = ArrayList(decoded.words.mapIndexed { i, word ->
                    val display = when (caps) {
                        CapsMode.MANUAL_LOCKED, CapsMode.AUTO_LOCKED -> word.uppercase(locale)
                        CapsMode.MANUAL, CapsMode.AUTO -> StringUtils.capitalizeFirstCodePoint(word, locale)
                        else -> word
                    }
                    SuggestedWordInfo(display, "", decoded.scores[i], SuggestedWordInfo.KIND_CORRECTION,
                        Dictionary.DICTIONARY_USER_TYPED, SuggestedWordInfo.NOT_AN_INDEX, SuggestedWordInfo.NOT_A_CONFIDENCE)
                }.distinctBy { it.mWord })
                SuggestedWords(words, null, words.firstOrNull(), true, false, false,
                    SuggestedWords.INPUT_STYLE_TAIL_BATCH, SuggestedWords.NOT_A_SEQUENCE_NUMBER)
            } catch (_: Exception) {
                Log.w(TAG, "Could not decode swipe input")
                SuggestedWords.getEmptyInstance()
            }
            result.accept(suggestions)
        }
    }

    fun cancelQueuedWork() {
        worker?.removeCallbacksAndMessages(null)
        preparedKeyboard = null
    }

    fun close() {
        cancelQueuedWork()
        thread?.quit()
        thread = null
        worker = null
    }

    private fun handler(): Handler {
        worker?.let { return it }
        val workerThread = HandlerThread(TAG, android.os.Process.THREAD_PRIORITY_BACKGROUND)
        workerThread.start()
        thread = workerThread
        return Handler(workerThread.looper).also { worker = it }
    }

    companion object {
        private const val TAG = "OpenSwipe"
        private const val MAX_CAPTURED_POINTS = 4096
        private const val MAX_DECODER_POINTS = 256

        @JvmStatic
        fun supports(locale: Locale): Boolean = locale.language == "de" || locale.language == "en"
    }
}
