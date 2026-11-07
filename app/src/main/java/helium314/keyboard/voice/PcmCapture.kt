// SPDX-License-Identifier: GPL-3.0-or-later
package helium314.keyboard.voice

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.yield
import java.util.concurrent.atomic.AtomicBoolean

/** One microphone instance. read returns zero when the non-blocking source has no samples yet. */
internal interface PcmSource : AutoCloseable {
    val sampleRate: Int
    fun start()
    fun read(buffer: ShortArray): Int
}

internal enum class CaptureState { READY, STARTING, LISTENING, PAUSED, STOPPED, CANCELLED, FAILED }
internal class CaptureFailure(val reason: DictationError) : Exception()

/** Single-consumer PCM stream. The microphone is acquired and released on the worker dispatcher. */
internal class PcmCapture(
    private val worker: CoroutineDispatcher = Dispatchers.IO,
    private val maxSamples: Int = SAMPLE_RATE * 120,
    private val bufferChunks: Int = 16,
    private val open: () -> PcmSource,
) {
    private enum class Command { RECORD, PAUSE, STOP }

    private val microphone = Mutex()

    init {
        require(maxSamples > 0 && bufferChunks in 1..32)
    }

    fun newSession(): Session = Session()

    inner class Session internal constructor() {
        private val command = MutableStateFlow(Command.RECORD)
        private val mutableState = MutableStateFlow(CaptureState.READY)
        private val collected = AtomicBoolean()
        val state = mutableState.asStateFlow()

        private val captureFlow = channelFlow {
            if (!collected.compareAndSet(false, true)) throw CaptureFailure(DictationError.AUDIO_BUSY)
            try {
                if (command.value != Command.STOP) {
                    if (!microphone.tryLock()) throw CaptureFailure(DictationError.AUDIO_BUSY)
                    try {
                        capture()
                    } finally {
                        microphone.unlock()
                    }
                }
                mutableState.value = CaptureState.STOPPED
            } catch (cancelled: CancellationException) {
                mutableState.value = CaptureState.CANCELLED
                throw cancelled
            } catch (failure: CaptureFailure) {
                mutableState.value = CaptureState.FAILED
                throw failure
            } catch (_: SecurityException) {
                mutableState.value = CaptureState.FAILED
                throw CaptureFailure(DictationError.PERMISSION_DENIED)
            } catch (_: Exception) {
                mutableState.value = CaptureState.FAILED
                throw CaptureFailure(DictationError.AUDIO_UNAVAILABLE)
            }
        }.flowOn(worker).buffer(bufferChunks)

        // An adapter's buffer must never fuse with and change the capture queue.
        val audio = flow { emitAll(captureFlow) }

        fun pause() {
            if (state.value == CaptureState.LISTENING) command.compareAndSet(Command.RECORD, Command.PAUSE)
        }

        fun resume() {
            if (state.value == CaptureState.PAUSED) command.compareAndSet(Command.PAUSE, Command.RECORD)
        }

        fun stop() { command.value = Command.STOP }

        private suspend fun ProducerScope<ShortArray>.capture() {
            var remaining = maxSamples
            while (command.value != Command.STOP && remaining > 0) {
                if (command.value == Command.PAUSE) {
                    mutableState.value = CaptureState.PAUSED
                    command.first { it != Command.PAUSE }
                    continue
                }
                mutableState.value = CaptureState.STARTING
                ensureActive()
                if (command.value == Command.STOP) return
                remaining = open().use { source ->
                    ensureActive()
                    if (command.value == Command.STOP) return
                    source.start()
                    record(source, remaining)
                }
            }
        }

        private suspend fun ProducerScope<ShortArray>.record(source: PcmSource, limit: Int): Int {
            val rate = source.sampleRate
            if (rate !in SAMPLE_RATE..48_000) throw CaptureFailure(DictationError.AUDIO_UNAVAILABLE)
            val buffer = ShortArray(rate / 50) // 20 ms
            val resampler = if (rate == SAMPLE_RATE) null else PcmDownsampler(rate)
            var remaining = limit
            var emptyReads = 0
            ensureActive()
            if (command.value == Command.RECORD) mutableState.value = CaptureState.LISTENING
            while (command.value == Command.RECORD && remaining > 0) {
                ensureActive()
                val count = source.read(buffer)
                if (count !in 0..buffer.size) throw CaptureFailure(DictationError.AUDIO_UNAVAILABLE)
                if (count == 0) {
                    if (++emptyReads >= 200) throw CaptureFailure(DictationError.AUDIO_UNAVAILABLE)
                    delay(10)
                    continue
                }
                emptyReads = 0
                val chunk = if (resampler == null) buffer.copyOf(count)
                    else resampler.convert(buffer, count)
                if (chunk.isEmpty()) continue
                ensureActive()
                if (command.value != Command.RECORD) break
                val output = if (chunk.size <= remaining) chunk else chunk.copyOf(remaining)
                val send = trySend(output)
                if (send.isClosed) throw CancellationException()
                if (send.isFailure) throw CaptureFailure(DictationError.AUDIO_OVERFLOW)
                remaining -= output.size
                yield()
            }
            return remaining
        }
    }

    /** Average the source samples that overlap each output interval. Keeps phase between reads. */
    private class PcmDownsampler(private val sourceRate: Int) {
        private var used = 0
        private var weightedSample = 0L

        fun convert(buffer: ShortArray, count: Int): ShortArray {
            val result = ShortArray((used + count * SAMPLE_RATE) / sourceRate)
            var resultIndex = 0
            for (index in 0 until count) {
                var available = SAMPLE_RATE
                while (available > 0) {
                    val weight = minOf(available, sourceRate - used)
                    weightedSample += buffer[index].toLong() * weight
                    available -= weight
                    used += weight
                    if (used == sourceRate) {
                        result[resultIndex++] = (weightedSample / sourceRate).toShort()
                        weightedSample = 0
                        used = 0
                    }
                }
            }
            return result
        }
    }

    companion object {
        const val SAMPLE_RATE = 16_000
    }
}
