// SPDX-License-Identifier: GPL-3.0-or-later
package helium314.keyboard.voice

import kotlinx.coroutines.flow.Flow
import java.util.Collections
import java.util.Locale

internal enum class Processing { ON_DEVICE, EXTERNAL_OR_UNKNOWN }
internal enum class AudioInputMode { CAPTURED_PCM, RECOGNIZER_MANAGED }

internal data class ProviderCapabilities(
    val processing: Processing,
    val audioInput: AudioInputMode,
    val supportsPartials: Boolean = false,
    val supportsDictionaryHints: Boolean = false,
)

internal sealed interface SpeechInput {
    val mode: AudioInputMode

    /** Signed 16-bit mono PCM at 16 kHz. A producer must transfer ownership of each chunk. */
    class CapturedAudio(val chunks: Flow<ShortArray>) : SpeechInput {
        override val mode = AudioInputMode.CAPTURED_PCM
    }

    /** The recognizer manages the microphone after the caller grants permission. */
    data object RecognizerManaged : SpeechInput {
        override val mode = AudioInputMode.RECOGNIZER_MANAGED
    }
}

/** Explicitly approved hints. A caller must not source them from private editor or learning data. */
internal class DictionaryHints(words: List<String> = emptyList()) {
    val words: List<String> = Collections.unmodifiableList(words.toList())
}

internal class SpeechRequest(
    val locale: Locale,
    val input: SpeechInput,
    val hints: DictionaryHints = DictionaryHints(),
) {
    fun withHints(hints: DictionaryHints) = SpeechRequest(locale, input, hints)
}

/** Offsets are relative to the beginning of the request's audio, in milliseconds. */
internal data class AudioRange(val startMillis: Long, val endMillis: Long)

internal class Transcript(
    val text: String,
    val confidence: Float? = null,
    val audioRange: AudioRange? = null,
)

internal sealed interface SpeechEvent {
    class Partial(val transcript: Transcript) : SpeechEvent
    /** Final for the entire request. The adapter assembles any per-segment finals. */
    class Final(val transcript: Transcript) : SpeechEvent
    data class Failure(val error: DictationError) : SpeechEvent
}

internal interface SpeechProvider {
    val id: String
    val capabilities: ProviderCapabilities

    /** Release audio and inference resources when collection ends or is cancelled. */
    fun recognize(request: SpeechRequest): Flow<SpeechEvent>
}

internal interface CleanupProvider {
    val id: String
    val processing: Processing

    suspend fun clean(transcript: Transcript, locale: Locale): String
}

/** Per-request approval of exact recipients, issued by the user-facing controller. */
internal data class ProviderConsent(val speech: String? = null, val cleanup: String? = null)

internal enum class DictationError {
    EDITOR_UNAVAILABLE,
    CONSENT_REQUIRED,
    UNSUPPORTED_INPUT,
    PERMISSION_DENIED,
    AUDIO_UNAVAILABLE,
    AUDIO_BUSY,
    AUDIO_OVERFLOW,
    MODEL_UNAVAILABLE,
    NO_FINAL_TRANSCRIPT,
    PROVIDER_FAILURE,
    CLEANUP_FAILURE,
}
