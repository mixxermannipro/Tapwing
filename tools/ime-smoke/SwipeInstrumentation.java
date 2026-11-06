// SPDX-License-Identifier: GPL-3.0-only
package io.github.mixxermannipro.satzfink.smoke;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.io.FileOutputStream;
import java.io.IOException;

public final class SwipeInstrumentation extends Instrumentation {
    private Bundle arguments;

    @Override
    public void onCreate(Bundle arguments) {
        this.arguments = arguments;
        start();
    }

    @Override
    public void onStart() {
        Bundle result = new Bundle();
        try {
            String expected = arguments.getString("expected", "");
            String[] points = arguments.getString("path", "").split(";");
            if (points.length < 3) throw new IllegalArgumentException("Provide at least three x,y points");

            Intent intent = new Intent(getTargetContext(), FixtureActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            FixtureActivity activity = (FixtureActivity) startActivitySync(intent);
            EditText editor = activity.getField(Integer.parseInt(arguments.getString("field", "0")));
            runOnMainSync(() -> {
                editor.requestFocus();
                activity.getSystemService(InputMethodManager.class).showSoftInput(editor, InputMethodManager.SHOW_IMPLICIT);
            });
            SystemClock.sleep(1200);

            long downTime = SystemClock.uptimeMillis();
            for (int i = 0; i < points.length; i++) {
                String[] coordinate = points[i].split(",");
                int action = i == 0 ? MotionEvent.ACTION_DOWN
                        : i == points.length - 1 ? finalAction() : MotionEvent.ACTION_MOVE;
                MotionEvent event = MotionEvent.obtain(downTime, SystemClock.uptimeMillis(), action,
                        Float.parseFloat(coordinate[0]), Float.parseFloat(coordinate[1]), 0);
                event.setSource(InputDevice.SOURCE_TOUCHSCREEN);
                try {
                    if (!getUiAutomation().injectInputEvent(event, true)) {
                        throw new IllegalStateException("Touch injection failed");
                    }
                } finally {
                    event.recycle();
                }
                SystemClock.sleep(18);
            }
            SystemClock.sleep(850);
            String[] text = new String[1];
            runOnMainSync(() -> text[0] = editor.getText().toString());
            String maxCharacters = arguments.getString("maxChars");
            boolean matches = maxCharacters == null
                    ? expected.equals(text[0]) : text[0].length() <= Integer.parseInt(maxCharacters);
            if (matches && "true".equals(arguments.getString("screenshot"))) captureScreenshot();
            result.putString("stream", matches ? "Swipe matched the expected text\n"
                    : "Swipe text differs from expected\n");
            finish(matches ? Activity.RESULT_OK : Activity.RESULT_CANCELED, result);
        } catch (RuntimeException | IOException error) {
            result.putString("stream", "Swipe test failed: " + error + "\n");
            finish(Activity.RESULT_CANCELED, result);
        }
    }

    private void captureScreenshot() throws IOException {
        Bitmap bitmap = getUiAutomation().takeScreenshot();
        if (bitmap == null) throw new IOException("No screenshot returned");
        try (FileOutputStream stream = getTargetContext().openFileOutput("swipe.png", 0)) {
            if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)) {
                throw new IOException("Could not encode the screenshot");
            }
        } finally {
            bitmap.recycle();
        }
    }

    private int finalAction() {
        return "true".equals(arguments.getString("cancel"))
                ? MotionEvent.ACTION_CANCEL : MotionEvent.ACTION_UP;
    }
}
