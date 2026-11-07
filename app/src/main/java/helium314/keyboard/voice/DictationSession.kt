// SPDX-License-Identifier: GPL-3.0-or-later
package helium314.keyboard.voice

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

internal sealed interface DictationState {
    data object Idle : DictationState
    class Listening(val partial: Transcript? = null) : DictationState
    data object Processing : DictationState
    class Preview(val original: Transcript, val text: String, val warning: DictationError? = null) : DictationState
    data class Failed(val error: DictationError) : DictationState
    data object Cancelled : DictationState
}

/**
 * Call from the scope's single owner thread, later the IME main thread.
 * Providers must move blocking work off that thread. The IME remains responsible for
 * revalidating the live editor, selection and text at the point of insertion.
 */
internal class DictationSession(private val scope: CoroutineScope) {
    private class Run(val editor: EditorSessionGuard.Token) {
        var job: Job? = null
    }

    private val guard = EditorSessionGuard()
    private val mutableState = MutableStateFlow<DictationState>(DictationState.Idle)
    val state = mutableState.asStateFlow()
    private var active: Run? = null

    fun editorChanged(eligible: Boolean) {
        guard.update(eligible)
        cancel()
    }

    fun start(
        provider: SpeechProvider,
        request: SpeechRequest,
        cleanup: CleanupProvider? = null,
        consent: ProviderConsent = ProviderConsent(),
    ): Boolean {
        if (active != null || !scope.isActive) return false
        val editor = guard.current ?: return reject(DictationError.EDITOR_UNAVAILABLE)
        val capabilities = provider.capabilities
        if (capabilities.audioInput != request.input.mode) return reject(DictationError.UNSUPPORTED_INPUT)
        if (capabilities.processing != Processing.ON_DEVICE && consent.speech != provider.id) {
            return reject(DictationError.CONSENT_REQUIRED)
        }
        if (cleanup != null && cleanup.processing != Processing.ON_DEVICE && consent.cleanup != cleanup.id) {
            return reject(DictationError.CONSENT_REQUIRED)
        }

        val run = Run(editor)
        val approvedRequest = if (capabilities.supportsDictionaryHints) request else request.withHints(DictionaryHints())
        val job = scope.launch(start = CoroutineStart.LAZY) { recognize(run, provider, approvedRequest, cleanup) }
        run.job = job
        active = run
        mutableState.value = DictationState.Listening()
        job.start()
        return true
    }

    fun cancel() {
        val run = active ?: return
        active = null
        run.job?.cancel()
        mutableState.value = DictationState.Cancelled
    }

    /** Return the approved preview at most once. This does not write to the editor. */
    fun takePreview(): DictationState.Preview? {
        val run = active ?: return null
        val preview = mutableState.value as? DictationState.Preview ?: return null
        if (!isCurrent(run) || !scope.isActive) return null
        active = null
        mutableState.value = DictationState.Idle
        return preview.takeIf { active == null && guard.isCurrent(run.editor) && scope.isActive }
    }

    private suspend fun recognize(
        run: Run,
        provider: SpeechProvider,
        request: SpeechRequest,
        cleanup: CleanupProvider?,
    ) {
        try {
            currentCoroutineContext().ensureActive()
            val terminal = provider.recognize(request).firstOrNull { event ->
                currentCoroutineContext().ensureActive()
                if (event is SpeechEvent.Partial) {
                    if (isCurrent(run)) mutableState.value = DictationState.Listening(event.transcript)
                    false
                } else {
                    true
                }
            }
            currentCoroutineContext().ensureActive()
            if (!isCurrent(run)) return
            when (terminal) {
                is SpeechEvent.Final -> {
                    if (terminal.transcript.text.isBlank()) fail(run, DictationError.NO_FINAL_TRANSCRIPT)
                    else preview(run, request, terminal.transcript, cleanup)
                }
                is SpeechEvent.Failure -> fail(run, terminal.error)
                else -> fail(run, DictationError.NO_FINAL_TRANSCRIPT)
            }
        } catch (cancelled: CancellationException) {
            if (isCurrent(run)) cancel()
            throw cancelled
        } catch (failure: CaptureFailure) {
            fail(run, failure.reason)
        } catch (_: Exception) {
            fail(run, DictationError.PROVIDER_FAILURE)
        }
    }

    private suspend fun preview(
        run: Run,
        request: SpeechRequest,
        original: Transcript,
        cleanup: CleanupProvider?,
    ) {
        val preview = if (cleanup == null) {
            DictationState.Preview(original, original.text)
        } else {
            mutableState.value = DictationState.Processing
            currentCoroutineContext().ensureActive()
            if (!isCurrent(run)) return
            val cleaned = try {
                cleanup.clean(original, request.locale).takeIf { it.isNotBlank() }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                null
            }
            if (cleaned == null) DictationState.Preview(original, original.text, DictationError.CLEANUP_FAILURE)
            else DictationState.Preview(original, cleaned)
        }
        currentCoroutineContext().ensureActive()
        if (isCurrent(run)) mutableState.value = preview
    }

    private fun isCurrent(run: Run): Boolean = active === run && guard.isCurrent(run.editor)

    private fun fail(run: Run, error: DictationError) {
        if (!isCurrent(run)) return
        active = null
        mutableState.value = DictationState.Failed(error)
    }

    private fun reject(error: DictationError): Boolean {
        mutableState.value = DictationState.Failed(error)
        return false
    }
}
