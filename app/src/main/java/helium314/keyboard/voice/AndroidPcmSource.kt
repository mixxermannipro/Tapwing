// SPDX-License-Identifier: GPL-3.0-or-later
package helium314.keyboard.voice

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.SystemClock

/** Opens the platform recorder only when the caller already has permission. */
internal class AndroidPcmSource private constructor(
    private val record: AudioRecord,
    private val audioManager: AudioManager,
) : PcmSource {
    private var routeId: Int? = null
    private var nextHealthCheck = 0L
    override val sampleRate: Int get() = record.sampleRate

    override fun start() {
        checkHealth()
        record.startRecording()
        if (record.recordingState != AudioRecord.RECORDSTATE_RECORDING) {
            throw CaptureFailure(DictationError.AUDIO_UNAVAILABLE)
        }
        routeId = record.routedDevice?.id
        checkHealth()
    }

    override fun read(buffer: ShortArray): Int {
        val now = SystemClock.elapsedRealtime()
        if (now >= nextHealthCheck) {
            checkHealth()
            nextHealthCheck = now + 100
        }
        return record.read(buffer, 0, buffer.size, AudioRecord.READ_NON_BLOCKING)
    }

    override fun close() {
        try {
            if (record.recordingState == AudioRecord.RECORDSTATE_RECORDING) record.stop()
        } finally {
            record.release()
        }
    }

    private fun checkHealth() {
        // Playback audio focus does not grant or arbitrate microphone access.
        val mode = audioManager.mode
        if (mode == AudioManager.MODE_IN_CALL || mode == AudioManager.MODE_IN_COMMUNICATION
            || record.activeRecordingConfiguration?.isClientSilenced == true
        ) {
            throw CaptureFailure(DictationError.AUDIO_UNAVAILABLE)
        }
        val currentRoute = record.routedDevice?.id
        if (routeId != null && currentRoute != routeId) {
            throw CaptureFailure(DictationError.AUDIO_UNAVAILABLE)
        }
        routeId = currentRoute
    }

    companion object {
        @SuppressLint("MissingPermission") // Explicit check immediately before constructing AudioRecord.
        fun open(context: Context): AndroidPcmSource {
            if (context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                throw CaptureFailure(DictationError.PERMISSION_DENIED)
            }
            val audioManager = context.getSystemService(AudioManager::class.java)
                ?: throw CaptureFailure(DictationError.AUDIO_UNAVAILABLE)
            for (rate in intArrayOf(PcmCapture.SAMPLE_RATE, 44_100)) {
                val channel = AudioFormat.CHANNEL_IN_MONO
                val encoding = AudioFormat.ENCODING_PCM_16BIT
                val minimum = AudioRecord.getMinBufferSize(rate, channel, encoding)
                if (minimum <= 0) continue
                val record = try {
                    AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION,
                        rate, channel, encoding, maxOf(minimum, rate / 5))
                } catch (_: IllegalArgumentException) {
                    continue
                } catch (_: SecurityException) {
                    throw CaptureFailure(DictationError.PERMISSION_DENIED)
                }
                if (record.state == AudioRecord.STATE_INITIALIZED) return AndroidPcmSource(record, audioManager)
                record.release()
            }
            throw CaptureFailure(DictationError.AUDIO_UNAVAILABLE)
        }
    }
}
