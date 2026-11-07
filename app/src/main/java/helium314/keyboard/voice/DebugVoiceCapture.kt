// SPDX-License-Identifier: GPL-3.0-or-later
package helium314.keyboard.voice

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.PowerManager
import android.os.SystemClock
import android.text.InputType
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import helium314.keyboard.latin.LatinIME
import helium314.keyboard.latin.R
import helium314.keyboard.latin.settings.Settings
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

private const val FIXTURE_PACKAGE = "io.github.mixxermannipro.satzfink.smoke"

internal fun isVoiceCaptureFixtureEditor(editor: EditorInfo?): Boolean =
    editor != null &&
        editor.packageName == FIXTURE_PACKAGE &&
        editor.privateImeOptions == "$FIXTURE_PACKAGE.VOICE_CAPTURE" &&
        editor.inputType and (InputType.TYPE_MASK_CLASS or InputType.TYPE_MASK_VARIATION) == InputType.TYPE_CLASS_TEXT &&
        editor.imeOptions and EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING == 0

/** Debug-only IME entry point. Audio is discarded, and the connection is used only as an identity. */
@SuppressLint("SetTextI18n")
internal class DebugVoiceCapture(private val ime: LatinIME) {
    private class Run(val session: PcmCapture.Session) {
        lateinit var job: Job
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val recorder = PcmCapture { AndroidPcmSource.open(ime) }
    private val power = ime.getSystemService(PowerManager::class.java)
    private val keyguard = ime.getSystemService(KeyguardManager::class.java)
    private var active: Run? = null
    private var panel: LinearLayout? = null
    private var status: TextView? = null
    private var start: Button? = null
    private var pause: Button? = null
    private var cancel: Button? = null

    private val screenOff = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_SCREEN_OFF) hide()
        }
    }

    init {
        ContextCompat.registerReceiver(ime, screenOff, IntentFilter(Intent.ACTION_SCREEN_OFF),
            ContextCompat.RECEIVER_NOT_EXPORTED)
    }

    fun attach(root: View) {
        hide()
        panel?.let { (it.parent as? ViewGroup)?.removeView(it) }
        panel = null
        val frame = root.findViewById<LinearLayout>(R.id.main_keyboard_frame) ?: return
        val row = LinearLayout(frame.context)
        row.orientation = LinearLayout.HORIZONTAL
        val header = LinearLayout(frame.context)
        header.orientation = LinearLayout.VERTICAL
        header.visibility = View.GONE
        status = TextView(frame.context).also(header::addView)
        start = Button(frame.context).apply { setOnClickListener { toggleRecording() } }.also(row::addView)
        pause = Button(frame.context).apply {
            setOnClickListener {
                val session = active?.session ?: return@setOnClickListener
                if (session.state.value == CaptureState.PAUSED) session.resume() else session.pause()
            }
        }.also(row::addView)
        cancel = Button(frame.context).apply {
            text = "Cancel"
            setOnClickListener { cancel() }
        }.also(row::addView)
        header.addView(row)
        frame.addView(header, 0)
        panel = header
        render("Microphone check")
        refresh()
    }

    fun refresh() {
        if (allowsCapture()) panel?.visibility = View.VISIBLE else hide()
    }

    fun visibleHeight(): Int = panel?.takeIf { it.visibility == View.VISIBLE }?.height ?: 0

    fun hide() {
        cancel()
        panel?.visibility = View.GONE
    }

    fun cancel() {
        val run = active ?: return
        active = null
        run.job.cancel()
        render("Cancelled")
    }

    fun close() {
        hide()
        scope.cancel()
        ime.unregisterReceiver(screenOff)
    }

    private fun allowsCapture(): Boolean =
        ime.isInputViewShown && power?.isInteractive == true && keyguard?.isDeviceLocked == false &&
            Settings.getValues()?.mIncognitoModeEnabled == false && isVoiceCaptureFixtureEditor(ime.currentInputEditorInfo)

    private fun toggleRecording() {
        val run = active
        if (run != null) {
            run.session.stop()
            return
        }
        if (!allowsCapture()) {
            hide()
            return
        }
        if (ime.packageManager.checkSignatures(ime.packageName, FIXTURE_PACKAGE) != PackageManager.SIGNATURE_MATCH) {
            render("Fixture signature mismatch")
            return
        }
        val connection = ime.currentInputConnection ?: return
        val fieldId = ime.currentInputEditorInfo?.fieldId ?: return
        if (ime.checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            try {
                ime.startActivity(Intent(ime, DebugMicrophonePermissionActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            } catch (_: android.content.ActivityNotFoundException) {
                Toast.makeText(ime, "Microphone check unavailable", Toast.LENGTH_SHORT).show()
            }
            return
        }

        val startedAt = SystemClock.elapsedRealtime()
        val next = Run(recorder.newSession())
        next.job = scope.launch(start = CoroutineStart.LAZY) {
            var firstFrameMs: Long? = null
            var outcome = "Stopped"
            try {
                coroutineScope {
                    val observer = launch {
                        next.session.state.collect { state ->
                            if (active === next) render(state.name, state)
                        }
                    }
                    try {
                        next.session.audio.collect {
                            if (!allowsCapture() || ime.currentInputConnection !== connection ||
                                ime.currentInputEditorInfo?.fieldId != fieldId) throw CancellationException()
                            if (firstFrameMs == null) {
                                firstFrameMs = SystemClock.elapsedRealtime() - startedAt
                                if (active === next) render("First PCM frame: ${firstFrameMs} ms", CaptureState.LISTENING)
                            }
                        }
                    } finally {
                        observer.cancel()
                    }
                }
            } catch (failure: CaptureFailure) {
                outcome = failure.reason.name
            } catch (cancelled: CancellationException) {
                outcome = "Cancelled"
                throw cancelled
            } finally {
                if (active === next) {
                    active = null
                    render(firstFrameMs?.let { "$outcome · first PCM frame: $it ms" } ?: outcome)
                }
            }
        }
        active = next
        render("Starting", CaptureState.STARTING)
        next.job.start()
    }

    private fun render(message: String, state: CaptureState? = null) {
        status?.text = message
        start?.text = if (active == null) "Mic test" else "Stop"
        pause?.text = if (state == CaptureState.PAUSED) "Resume" else "Pause"
        pause?.isEnabled = state == CaptureState.LISTENING || state == CaptureState.PAUSED
        cancel?.isEnabled = active != null
    }
}

/** Manifest entry exists in the debug APK only. Granting permission never starts capture. */
class DebugMicrophonePermissionActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) finish()
        else if (savedInstanceState == null) requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 1)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        finish()
    }
}
