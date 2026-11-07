// SPDX-License-Identifier: GPL-3.0-or-later
package helium314.keyboard.voice

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.coroutines.ContinuationInterceptor
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

class PcmCaptureTest {
    @Test fun repeatedCapturesReleaseEverySourceAndBoundTheDuration() = runBlocking {
        val sources = mutableListOf<FakeSource>()
        val recorder = PcmCapture(dispatcher, maxSamples = 500) {
            FakeSource(16_000) { buffer -> buffer.fill(123); buffer.size }.also(sources::add)
        }
        repeat(100) {
            val session = recorder.newSession()
            val chunks = session.audio.toList()
            assertEquals(500, chunks.sumOf { it.size })
            assertContentEquals(ShortArray(500) { 123 }, chunks.flatMap { it.asList() }.toShortArray())
            assertEquals(CaptureState.STOPPED, session.state.value)
        }
        assertEquals(100, sources.size)
        sources.forEach {
            assertEquals(1, it.starts)
            assertEquals(1, it.closes)
        }
    }

    @Test fun pauseClosesSourceAndStopReleasesTheResumedSource() = runBlocking {
        val sources = mutableListOf<FakeSource>()
        val recorder = PcmCapture(dispatcher) { FakeSource(16_000) { 0 }.also(sources::add) }
        val session = recorder.newSession()
        val job = launch { session.audio.collect {} }

        session.awaitState(CaptureState.LISTENING)
        session.pause()
        session.awaitState(CaptureState.PAUSED)
        assertEquals(1, sources.single().closes)
        session.resume()
        session.awaitState(CaptureState.LISTENING)
        session.stop()
        withTimeout(2_000) { job.join() }

        assertEquals(CaptureState.STOPPED, session.state.value)
        assertEquals(listOf(1, 1), sources.map { it.closes })
    }

    @Test fun anotherCollectorCannotOpenMicrophoneUntilCancellationReleasesIt() = runBlocking {
        val sources = mutableListOf<FakeSource>()
        val recorder = PcmCapture(dispatcher) { FakeSource(16_000) { 0 }.also(sources::add) }
        val first = recorder.newSession()
        val job = launch { first.audio.collect {} }
        first.awaitState(CaptureState.LISTENING)

        val error = assertFailsWith<CaptureFailure> { recorder.newSession().audio.toList() }
        assertEquals(DictationError.AUDIO_BUSY, error.reason)
        assertEquals(1, sources.size)

        job.cancelAndJoin()
        assertEquals(CaptureState.CANCELLED, first.state.value)
        assertEquals(1, sources.single().closes)

        val next = recorder.newSession()
        val nextJob = launch { next.audio.collect {} }
        next.awaitState(CaptureState.LISTENING)
        next.stop()
        withTimeout(2_000) { nextJob.join() }
        assertEquals(listOf(1, 1), sources.map { it.closes })
    }

    @Test fun stopBeforeCollectionDoesNotOpenSource() = runBlocking {
        val recorder = PcmCapture(dispatcher) { error("Source must not open") }
        val session = recorder.newSession()
        session.stop()
        assertEquals(emptyList(), session.audio.toList())
        assertEquals(CaptureState.STOPPED, session.state.value)
    }

    @Test fun failedReadAndOverflowReleaseMicrophone() = runBlocking {
        val interrupted = FakeSource(16_000) { throw CaptureFailure(DictationError.AUDIO_UNAVAILABLE) }
        val broken = PcmCapture(dispatcher) { interrupted }.newSession()
        assertEquals(DictationError.AUDIO_UNAVAILABLE,
            assertFailsWith<CaptureFailure> { broken.audio.toList() }.reason)
        assertEquals(CaptureState.FAILED, broken.state.value)
        assertEquals(1, interrupted.closes)

        val fast = FakeSource(16_000) { it.size }
        val overflow = PcmCapture(dispatcher, bufferChunks = 1) { fast }.newSession()
        assertEquals(DictationError.AUDIO_OVERFLOW,
            assertFailsWith<CaptureFailure> { overflow.audio.collect { delay(100) } }.reason)
        assertEquals(CaptureState.FAILED, overflow.state.value)
        assertEquals(1, fast.closes)
    }

    @Test fun consumerBufferCannotExpandRecorderQueue() = runBlocking {
        val reading = CompletableDeferred<Unit>()
        val proceed = CountDownLatch(1)
        val closed = CountDownLatch(1)
        val fast = FakeSource(16_000, onClose = closed::countDown) {
            reading.complete(Unit)
            check(proceed.await(2, TimeUnit.SECONDS))
            it.size
        }
        val recorder = PcmCapture(Dispatchers.Default, maxSamples = 1600, bufferChunks = 1) { fast }
        val result = async {
            runCatching { recorder.newSession().audio.buffer(Channel.UNLIMITED).toList() }
        }
        withTimeout(2_000) { reading.await() }
        proceed.countDown()
        // Freeze the consumer dispatcher so only the microphone worker can make progress.
        assertTrue(closed.await(2, TimeUnit.SECONDS))
        assertEquals(DictationError.AUDIO_OVERFLOW, assertIs<CaptureFailure>(result.await().exceptionOrNull()).reason)
    }

    @Test fun permissionLostDuringStartStillReleasesSource() = runBlocking {
        val source = FakeSource(16_000, onStart = { throw SecurityException() }) { 0 }
        val session = PcmCapture(dispatcher) { source }.newSession()
        assertEquals(DictationError.PERMISSION_DENIED,
            assertFailsWith<CaptureFailure> { session.audio.toList() }.reason)
        assertEquals(1, source.closes)
    }

    @Test fun resamplesFallbackRateAcrossChunkBoundaries() = runBlocking {
        var position = 0
        val recorder = PcmCapture(dispatcher, maxSamples = 16_000) {
            FakeSource(44_100) { buffer ->
                val count = minOf(buffer.size, 211)
                for (index in 0 until count) {
                    buffer[index] = if (position++ < 22_050) 1200 else -1200
                }
                count
            }
        }
        val samples = recorder.newSession().audio.toList().flatMap { it.asList() }.toShortArray()
        assertContentEquals(ShortArray(16_000) { if (it < 8_000) 1200 else -1200 }, samples)
    }

    private val CoroutineScope.dispatcher: CoroutineDispatcher
        get() = coroutineContext[ContinuationInterceptor] as CoroutineDispatcher

    private suspend fun PcmCapture.Session.awaitState(expected: CaptureState) {
        withTimeout(2_000) { state.first { it == expected } }
    }

    private class FakeSource(
        override val sampleRate: Int,
        private val onStart: () -> Unit = {},
        private val onClose: () -> Unit = {},
        private val readSamples: (ShortArray) -> Int,
    ) : PcmSource {
        var starts = 0
            private set
        var closes = 0
            private set

        override fun start() {
            starts++
            onStart()
        }
        override fun read(buffer: ShortArray) = readSamples(buffer)
        override fun close() {
            closes++
            onClose()
        }
    }
}
