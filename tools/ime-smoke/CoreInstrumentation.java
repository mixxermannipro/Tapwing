// SPDX-License-Identifier: GPL-3.0-only
package io.github.mixxermannipro.satzfink.smoke;

import android.accessibilityservice.AccessibilityService;
import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.WindowInsets;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public final class CoreInstrumentation extends Instrumentation {
    private Bundle arguments;
    private FixtureActivity activity;

    @Override
    public void onCreate(Bundle arguments) {
        this.arguments = arguments;
        start();
    }

    @Override
    public void onStart() {
        Bundle result = new Bundle();
        try {
            String activeIme = Settings.Secure.getString(getTargetContext().getContentResolver(),
                    Settings.Secure.DEFAULT_INPUT_METHOD);
            if (!"io.github.mixxermannipro.satzfink.debug/helium314.keyboard.latin.LatinIME".equals(activeIme)) {
                throw new IllegalStateException("Select the Tapwing debug keyboard first");
            }
            Intent intent = new Intent(getTargetContext(), FixtureActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity = (FixtureActivity) startActivitySync(intent);
            EditText normal = focusField(0);
            tap("layoutKey");
            String layoutLetter = requiredArgument("layoutLetter");
            expect(normal, layoutLetter, 1, "layout key");

            select(normal, "abcd", 3, 1);
            runOnMainSync(() -> normal.setSelection(2));
            SystemClock.sleep(350);
            tap("delete");
            expect(normal, "acd", 1, "delete after collapsing backwards selection");

            select(normal, "a🌍c", 1, 3);
            tap("a");
            expect(normal, "aac", 2, "replace selection");

            select(normal, "a🌍c", 3, 1);
            tap("delete");
            expect(normal, "ac", 1, "delete backwards selection");

            tap("shift");
            tap("a");
            expect(normal, "aAc", 2, "shift and insert at cursor");

            EditText privateField = activity.getField(1);
            tap("enter");
            await(privateField::hasFocus, "Next editor action");
            tap("a");
            expect(privateField, "a", 1, "private field");
            expect(normal, "aAc", 2, "original field after focus switch");

            EditText password = focusField(2);
            tap("passwordA");
            expect(password, "a", 1, "password input");
            tap("passwordDelete");
            expect(password, "", 0, "password delete");
            tap("passwordDelete");
            expect(password, "", 0, "empty password delete");
            tap("passwordA");
            expect(password, "a", 1, "password input after empty delete");
            tap("passwordDelete");
            expect(password, "", 0, "clear password test value");

            EditText pin = focusField(3);
            tap("pinOne");
            expect(pin, "1", 1, "PIN input");

            if (!getUiAutomation().performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME)) {
                throw new IllegalStateException("Could not background fixture");
            }
            await(() -> !activity.hasWindowFocus(), "fixture did not lose focus");
            getTargetContext().startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
            await(() -> activity.hasWindowFocus(), "fixture did not regain focus");
            focusField(0);
            tap("delete");
            expect(normal, "ac", 1, "delete after reopening");
            expect(pin, "1", 1, "PIN unchanged after reopening");

            EditText multiline = focusField(4);
            tap("a");
            tap("enter");
            tap("a");
            expect(multiline, "a\na", 3, "multiline Enter");

            if ("true".equals(arguments.getString("checkAutocorrect"))) {
                focusField(0);
                select(normal, "", 0, 0);
                tap("t");
                tap("e");
                tap("h");
                expect(normal, "teh", 3, "English typing");
                // Local suggestions are computed asynchronously.
                SystemClock.sleep(850);
                tap("space");
                expect(normal, "the ", 4, "English autocorrect");
                tap("delete");
                expect(normal, "teh", 3, "undo autocorrect");
            }

            result.putString("stream", "Core IME checks passed\n");
            finish(Activity.RESULT_OK, result);
        } catch (RuntimeException | AssertionError error) {
            result.putString("stream", "Core IME check failed: " + error.getMessage() + "\n");
            finish(Activity.RESULT_CANCELED, result);
        }
    }

    private EditText focusField(int index) {
        EditText field = activity.getField(index);
        runOnMainSync(field::requestFocus);
        await(() -> activity.hasWindowFocus() && field.hasFocus(), "editor did not gain focus");
        for (int attempt = 0; attempt < 4; attempt++) {
            runOnMainSync(() -> activity.getSystemService(InputMethodManager.class)
                    .showSoftInput(field, InputMethodManager.SHOW_IMPLICIT));
            // A cold IME may not accept the first request. Also allow its window to animate.
            SystemClock.sleep(750);
            if (Build.VERSION.SDK_INT < 30) return field;
            boolean[] visible = new boolean[1];
            runOnMainSync(() -> {
                WindowInsets insets = field.getRootWindowInsets();
                visible[0] = insets != null && insets.isVisible(WindowInsets.Type.ime());
            });
            if (visible[0]) return field;
        }
        throw new AssertionError("keyboard did not open for field " + index);
    }

    private void select(EditText field, String text, int start, int end) {
        runOnMainSync(() -> field.setText(text));
        // Let setText's input restart reach the IME before sending an editor selection update.
        SystemClock.sleep(350);
        runOnMainSync(() -> field.setSelection(start, end));
        SystemClock.sleep(350);
    }

    private void expect(EditText field, String text, int cursor, String step) {
        await(() -> text.contentEquals(field.getText()) && field.getSelectionStart() == cursor
                && field.getSelectionEnd() == cursor, step);
    }

    private void await(java.util.function.BooleanSupplier condition, String message) {
        long deadline = SystemClock.uptimeMillis() + 4000;
        do {
            boolean[] matches = new boolean[1];
            runOnMainSync(() -> matches[0] = condition.getAsBoolean());
            if (matches[0]) return;
            SystemClock.sleep(50);
        } while (SystemClock.uptimeMillis() < deadline);
        // Never include editor contents in test output.
        throw new AssertionError(message);
    }

    private String requiredArgument(String name) {
        String value = arguments.getString(name);
        if (value == null) throw new IllegalArgumentException("Missing argument: " + name);
        return value;
    }

    private void tap(String name) {
        String[] coordinates = requiredArgument(name).split(",");
        if (coordinates.length != 2) throw new IllegalArgumentException("Expected x,y for " + name);
        float x = Float.parseFloat(coordinates[0]);
        float y = Float.parseFloat(coordinates[1]);
        long downTime = SystemClock.uptimeMillis();
        injectTap(downTime, MotionEvent.ACTION_DOWN, x, y);
        injectTap(downTime, MotionEvent.ACTION_UP, x, y);
    }

    private void injectTap(long downTime, int action, float x, float y) {
        MotionEvent event = MotionEvent.obtain(downTime, SystemClock.uptimeMillis(), action, x, y, 0);
        event.setSource(InputDevice.SOURCE_TOUCHSCREEN);
        try {
            if (!getUiAutomation().injectInputEvent(event, true)) {
                throw new IllegalStateException("Touch injection failed");
            }
        } finally {
            event.recycle();
        }
    }
}
