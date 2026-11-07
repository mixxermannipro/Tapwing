// SPDX-License-Identifier: GPL-3.0-or-later
package helium314.keyboard.voice

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import org.junit.Test
import java.util.Locale
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DictationSessionTest {
    private val request = SpeechRequest(Locale.GERMAN, SpeechInput.RecognizerManaged)

    @Test fun partialAndFinalBecomeAOneShotPreviewWithOriginal() = runBlocking {
        val allowFinal = CompletableDeferred<Unit>()
        val recognizer = FakeSpeech {
            flow {
                emit(SpeechEvent.Partial(Transcript("hallo")))
                allowFinal.await()
                emit(SpeechEvent.Final(Transcript("hallo welt", confidence = 0.8f)))
                error("A final result must end the request")
            }
        }
        val session = eligibleSession(this)
        assertTrue(session.start(recognizer, request, cleanup = FakeCleanup { "Hallo Welt." }))
        yield()
        assertEquals("hallo", assertIs<DictationState.Listening>(session.state.value).partial?.text)
        assertNull(session.takePreview())

        allowFinal.complete(Unit)
        yield()
        val preview = assertIs<DictationState.Preview>(session.state.value)
        assertEquals("hallo welt", preview.original.text)
        assertEquals(0.8f, preview.original.confidence)
        assertEquals("Hallo Welt.", preview.text)
        assertEquals(preview, session.takePreview())
        assertNull(session.takePreview())
        assertEquals(DictationState.Idle, session.state.value)
    }

    @Test fun unknownAndProtectedEditorsNeverStartAProvider() = runBlocking {
        val provider = FakeSpeech { flowOf(SpeechEvent.Final(Transcript("sample"))) }
        val session = DictationSession(this)
        assertFalse(session.start(provider, request))
        session.editorChanged(eligible = false)
        assertFalse(session.start(provider, request))
        assertEquals(DictationState.Failed(DictationError.EDITOR_UNAVAILABLE), session.state.value)
        assertEquals(0, provider.requests)
    }

    @Test fun externalConsentIsBoundToProviderAndStage() = runBlocking {
        val speech = FakeSpeech(processing = Processing.EXTERNAL_OR_UNKNOWN) {
            flowOf(SpeechEvent.Final(Transcript("sample")))
        }
        val cleanup = FakeCleanup(processing = Processing.EXTERNAL_OR_UNKNOWN) { "cleaned" }
        val session = eligibleSession(this)
        assertFalse(session.start(speech, request, consent = ProviderConsent(cleanup = speech.id)))
        assertFalse(session.start(speech, request, cleanup, ProviderConsent(speech = speech.id)))
        assertEquals(DictationState.Failed(DictationError.CONSENT_REQUIRED), session.state.value)
        assertEquals(0, speech.requests)
        assertEquals(0, cleanup.requests)

        assertTrue(session.start(speech, request, cleanup, ProviderConsent(speech.id, cleanup.id)))
        yield()
        assertEquals("cleaned", session.takePreview()?.text)
        assertEquals(1, speech.requests)
        assertEquals(1, cleanup.requests)
    }

    @Test fun incompatibleInputAndUnsupportedHintsAreHandledAtBoundary() = runBlocking {
        var received: SpeechRequest? = null
        val provider = FakeSpeech {
            received = it
            flowOf(SpeechEvent.Final(Transcript("sample")))
        }
        val session = eligibleSession(this)
        val captured = SpeechInput.CapturedAudio(flowOf(shortArrayOf(1, 2)))
        assertFalse(session.start(provider, SpeechRequest(Locale.GERMAN, captured)))
        assertEquals(DictationState.Failed(DictationError.UNSUPPORTED_INPUT), session.state.value)
        assertEquals(0, provider.requests)

        assertTrue(session.start(provider, request.withHints(DictionaryHints(listOf("Servus")))))
        yield()
        assertEquals(emptyList(), received?.hints?.words)
    }

    @Test fun fieldChangeCancelsWorkAndDiscardsPreview() = runBlocking {
        val released = CompletableDeferred<Unit>()
        val provider = FakeSpeech {
            flow {
                try {
                    emit(SpeechEvent.Partial(Transcript("old")))
                    CompletableDeferred<Unit>().await()
                } finally {
                    released.complete(Unit)
                }
            }
        }
        val session = eligibleSession(this)
        assertTrue(session.start(provider, request))
        yield()
        session.editorChanged(eligible = true)
        released.await()
        assertEquals(DictationState.Cancelled, session.state.value)
        assertNull(session.takePreview())

        val next = FakeSpeech { flowOf(SpeechEvent.Final(Transcript("new"))) }
        assertTrue(session.start(next, request))
        yield()
        assertIs<DictationState.Preview>(session.state.value)
        session.editorChanged(eligible = true)
        assertNull(session.takePreview())
    }

    @Test fun lateCleanupCannotReplaceNewSessionPreview() = runBlocking {
        val cleanupStarted = CompletableDeferred<Unit>()
        val releaseCleanup = CompletableDeferred<Unit>()
        val provider = FakeSpeech { flowOf(SpeechEvent.Final(Transcript("original"))) }
        val cleanup = FakeCleanup {
            withContext(NonCancellable) {
                cleanupStarted.complete(Unit)
                releaseCleanup.await()
                "old result"
            }
        }
        val session = eligibleSession(this)
        assertTrue(session.start(provider, request, cleanup))
        cleanupStarted.await()
        assertIs<DictationState.Processing>(session.state.value)
        assertFalse(session.start(provider, request))

        session.cancel()
        val next = FakeSpeech { flowOf(SpeechEvent.Final(Transcript("new result"))) }
        assertTrue(session.start(next, request))
        yield()
        releaseCleanup.complete(Unit)
        yield()
        assertEquals("new result", session.takePreview()?.text)
    }

    @Test fun editorChangeOnProcessingPreventsStartingCleanup() = runBlocking {
        val speech = FakeSpeech { flowOf(SpeechEvent.Final(Transcript("original"))) }
        val cleanup = FakeCleanup { "cleaned" }
        val session = eligibleSession(this)
        launch(Dispatchers.Unconfined) {
            session.state.first { it == DictationState.Processing }
            session.editorChanged(eligible = false)
        }

        assertTrue(session.start(speech, request, cleanup))
        yield()
        assertEquals(DictationState.Cancelled, session.state.value)
        assertEquals(0, cleanup.requests)
    }

    @Test fun editorChangeDuringPreviewConsumptionDiscardsProposal() = runBlocking {
        val speech = FakeSpeech { flowOf(SpeechEvent.Final(Transcript("original"))) }
        val session = eligibleSession(this)
        assertTrue(session.start(speech, request))
        yield()
        assertIs<DictationState.Preview>(session.state.value)
        launch(Dispatchers.Unconfined) {
            session.state.first { it == DictationState.Idle }
            session.editorChanged(eligible = false)
        }

        assertNull(session.takePreview())
    }

    @Test fun missingFinalAndProviderExceptionNeverProducePreview() = runBlocking {
        val session = eligibleSession(this)
        assertTrue(session.start(FakeSpeech { flowOf(SpeechEvent.Partial(Transcript("sample"))) }, request))
        yield()
        assertEquals(DictationState.Failed(DictationError.NO_FINAL_TRANSCRIPT), session.state.value)
        assertNull(session.takePreview())

        assertTrue(session.start(FakeSpeech { error("Provider detail") }, request))
        yield()
        assertEquals(DictationState.Failed(DictationError.PROVIDER_FAILURE), session.state.value)
        assertNull(session.takePreview())
    }

    @Test fun cleanupFailurePreservesOriginalForReview() = runBlocking {
        val session = eligibleSession(this)
        val speech = FakeSpeech { flowOf(SpeechEvent.Final(Transcript("original"))) }
        assertTrue(session.start(speech, request, FakeCleanup { error("Provider detail") }))
        yield()

        val preview = assertIs<DictationState.Preview>(session.state.value)
        assertEquals("original", preview.text)
        assertEquals(DictationError.CLEANUP_FAILURE, preview.warning)
    }

    private fun eligibleSession(scope: CoroutineScope) =
        DictationSession(scope).apply { editorChanged(eligible = true) }

    private class FakeSpeech(
        processing: Processing = Processing.ON_DEVICE,
        private val result: (SpeechRequest) -> Flow<SpeechEvent>,
    ) : SpeechProvider {
        override val id = "fake-speech"
        override val capabilities = ProviderCapabilities(
            processing, AudioInputMode.RECOGNIZER_MANAGED, supportsPartials = true,
        )
        var requests = 0
            private set

        override fun recognize(request: SpeechRequest): Flow<SpeechEvent> {
            requests++
            return result(request)
        }
    }

    private class FakeCleanup(
        override val processing: Processing = Processing.ON_DEVICE,
        private val result: suspend () -> String,
    ) : CleanupProvider {
        override val id = "fake-cleanup"
        var requests = 0
            private set

        override suspend fun clean(transcript: Transcript, locale: Locale): String {
            requests++
            return result()
        }
    }
}
