// SPDX-License-Identifier: GPL-3.0-only
package io.github.mixxermannipro.satzfink.smoke;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.app.Instrumentation;
import android.app.UiAutomation;
import android.content.Intent;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import android.provider.Settings;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public final class VoiceCaptureInstrumentation extends Instrumentation {
    private static final String IME_PACKAGE = "io.github.mixxermannipro.satzfink.debug";
    private static final String IME_SERVICE = IME_PACKAGE + "/helium314.keyboard.latin.LatinIME";
    private static final Pattern FIRST_FRAME = Pattern.compile("First PCM frame: ([0-9]+) ms");
    private static final long TIMEOUT_MS = 6000;

    private Bundle arguments;
    private UiAutomation automation;

    @Override
    public void onCreate(Bundle arguments) {
        this.arguments = arguments;
        start();
    }

    @Override
    public void onStart() {
        Bundle result = new Bundle();
        FixtureActivity fixture = null;
        int resultCode = Activity.RESULT_CANCELED;
        int completed = 0;
        try {
            int cycles = Integer.parseInt(arguments.getString("cycles", "100"));
            if (cycles < 1 || cycles > 1000) throw new IllegalArgumentException("cycles must be 1–1000");
            String activeIme = Settings.Secure.getString(getTargetContext().getContentResolver(),
                    Settings.Secure.DEFAULT_INPUT_METHOD);
            if (!IME_SERVICE.equals(activeIme)) throw new IllegalStateException("Select Tapwing Debug first");

            automation = getUiAutomation();
            AccessibilityServiceInfo serviceInfo = automation.getServiceInfo();
            serviceInfo.flags |= AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS;
            automation.setServiceInfo(serviceInfo);

            fixture = (FixtureActivity) startActivitySync(new Intent(getTargetContext(), FixtureActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).putExtra("voiceCapture", true));
            EditText normal = fixture.getField(0);
            runOnMainSync(normal::requestFocus);
            for (int attempt = 0; attempt < 4 && findText("Mic test"::equals) == null; attempt++) {
                runOnMainSync(() -> normal.getContext().getSystemService(InputMethodManager.class)
                        .showSoftInput(normal, InputMethodManager.SHOW_IMPLICIT));
                SystemClock.sleep(750);
            }

            long[] latency = new long[cycles];
            awaitMicrophone(false);
            for (; completed < cycles; completed++) {
                click("Mic test");
                AccessibilityNodeInfo firstFrame = awaitText(
                        text -> FIRST_FRAME.matcher(text).matches(), "first audio frame");
                var measurement = FIRST_FRAME.matcher(firstFrame.getText().toString());
                if (!measurement.matches()) throw new AssertionError("Missing start latency");
                latency[completed] = Long.parseLong(measurement.group(1));
                awaitMicrophone(true);

                click("Stop");
                awaitText("Mic test"::equals, "idle microphone button");
                awaitMicrophone(false);
            }
            Arrays.sort(latency);
            result.putString("stream", "Voice capture: " + cycles + "/" + cycles
                    + " cycles passed; first PCM p50=" + percentile(latency, 50)
                    + " ms, p95=" + percentile(latency, 95) + " ms\n");
            resultCode = Activity.RESULT_OK;
        } catch (IOException | RuntimeException | AssertionError failure) {
            result.putString("stream", "Voice capture failed after " + completed
                    + " completed cycles: " + failure.getMessage() + "\n");
        } finally {
            if (fixture != null) runOnMainSync(fixture::finish);
            finish(resultCode, result);
        }
    }

    private static long percentile(long[] sorted, int percent) {
        return sorted[(sorted.length * percent + 99) / 100 - 1];
    }

    private void click(String label) {
        var node = awaitText(label::equals, label + " button");
        if (!node.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
            throw new AssertionError("Could not press " + label);
        }
    }

    private AccessibilityNodeInfo awaitText(Predicate<String> predicate, String description) {
        long deadline = SystemClock.uptimeMillis() + TIMEOUT_MS;
        do {
            var node = findText(predicate);
            if (node != null) return node;
            SystemClock.sleep(30);
        } while (SystemClock.uptimeMillis() < deadline);
        throw new AssertionError("Timed out waiting for " + description);
    }

    private AccessibilityNodeInfo findText(Predicate<String> predicate) {
        for (AccessibilityWindowInfo window : automation.getWindows()) {
            if (window.getType() != AccessibilityWindowInfo.TYPE_INPUT_METHOD) continue;
            var root = window.getRoot();
            if (root == null || !IME_PACKAGE.contentEquals(root.getPackageName())) continue;
            var match = findText(root, predicate);
            if (match != null) return match;
        }
        return null;
    }

    private AccessibilityNodeInfo findText(AccessibilityNodeInfo node, Predicate<String> predicate) {
        CharSequence text = node.getText();
        if (node.isVisibleToUser() && text != null && predicate.test(text.toString())) return node;
        for (int index = 0; index < node.getChildCount(); index++) {
            var child = node.getChild(index);
            if (child == null) continue;
            var match = findText(child, predicate);
            if (match != null) return match;
        }
        return null;
    }

    private void awaitMicrophone(boolean expectedRunning) throws IOException {
        long deadline = SystemClock.uptimeMillis() + TIMEOUT_MS;
        do {
            if (microphoneRunning() == expectedRunning) return;
            SystemClock.sleep(30);
        } while (SystemClock.uptimeMillis() < deadline);
        throw new AssertionError(expectedRunning ? "Microphone did not start" : "Microphone did not release");
    }

    private boolean microphoneRunning() throws IOException {
        var output = automation.executeShellCommand("cmd appops get " + IME_PACKAGE + " RECORD_AUDIO");
        try (var reader = new BufferedReader(new InputStreamReader(
                new ParcelFileDescriptor.AutoCloseInputStream(output), StandardCharsets.UTF_8))) {
            String line;
            boolean hasRecordAudio = false;
            while ((line = reader.readLine()) != null) {
                if (!line.contains("RECORD_AUDIO:")) continue;
                hasRecordAudio = true;
                if (line.contains("(running)")) return true;
            }
            if (!hasRecordAudio) throw new IOException("AppOps did not report microphone access. Grant permission first.");
            return false;
        }
    }
}
