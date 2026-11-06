// SPDX-License-Identifier: GPL-3.0-or-later
package helium314.keyboard.latin.inputlogic;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import android.app.Application;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import helium314.keyboard.keyboard.Keyboard;
import helium314.keyboard.keyboard.KeyboardSwitcher;
import helium314.keyboard.keyboard.MainKeyboardView;
import helium314.keyboard.latin.CapsMode;
import helium314.keyboard.latin.LatinIME;
import helium314.keyboard.latin.SuggestedWords;
import helium314.keyboard.latin.common.InputPointers;
import helium314.keyboard.latin.swipe.OpenSwipeWorker;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/** Exercises the real lifecycle gate; decoder replies and Android editor boundaries are controlled. */
@RunWith(RobolectricTestRunner.class)
@Config(application = Application.class)
public class OpenSwipeLifecycleTest {
    private final LatinIME ime = mock(LatinIME.class);
    private final LatinIME.UIHandler ui = mock(LatinIME.UIHandler.class);
    private final InputLogic logic = mock(InputLogic.class);
    private final KeyboardSwitcher switcher = mock(KeyboardSwitcher.class);
    private final Keyboard keyboard = mock(Keyboard.class);
    private final MainKeyboardView view = mock(MainKeyboardView.class);
    private final InputConnection connection = mock(InputConnection.class);
    private final EditorInfo editor = new EditorInfo();
    private final SuggestedWords suggestions = mock(SuggestedWords.class);
    private final List<Consumer<SuggestedWords>> replies = new ArrayList<>();
    private final List<Runnable> uiTasks = new ArrayList<>();
    private MockedStatic<KeyboardSwitcher> keyboardSingleton;
    private MockedConstruction<OpenSwipeWorker> workers;
    private InputLogicHandler handler;

    @Before
    public void setUp() throws Exception {
        // Avoid creating a whole IME: its only role here is the external Android editor boundary.
        Field parent = InputLogic.class.getDeclaredField("mLatinIME");
        parent.setAccessible(true);
        parent.set(logic, ime);
        keyboardSingleton = mockStatic(KeyboardSwitcher.class);
        keyboardSingleton.when(KeyboardSwitcher::getInstance).thenReturn(switcher);
        when(switcher.getKeyboard()).thenReturn(keyboard);
        when(switcher.getMainKeyboardView()).thenReturn(view);
        when(ime.getCurrentInputConnection()).thenReturn(connection);
        when(ime.getCurrentInputEditorInfo()).thenReturn(editor);
        when(ime.isInputViewShown()).thenReturn(true);
        when(logic.canApplyOpenSwipeAt(0)).thenReturn(true);
        doAnswer(call -> { uiTasks.add(call.getArgument(0)); return true; })
                .when(ui).post(any(Runnable.class));
        // The real decoder runs on a worker thread. Hold its replies to exercise race orderings.
        workers = mockConstruction(OpenSwipeWorker.class, (worker, context) ->
                doAnswer(call -> { replies.add(call.getArgument(4)); return null; })
                        .when(worker).decode(any(), any(), any(), any(), any()));
        handler = new InputLogicHandler(ui, logic);
    }

    @After
    public void tearDown() {
        if (handler != null) {
            handler.closeSwipeWorker();
            handler.mNonUIThreadHandler.getLooper().quit();
        }
        if (workers != null) workers.close();
        if (keyboardSingleton != null) keyboardSingleton.close();
    }

    private void finish() {
        InputPointers points = new InputPointers(8);
        points.addPointer(100, 100, 0, 0);
        points.addPointer(200, 100, 0, 50);
        points.addPointer(300, 200, 0, 100);
        handler.finishSwipe(points, CapsMode.OFF, 0);
    }

    private void deliver(int index) {
        replies.get(index).accept(suggestions);
        uiTasks.remove(0).run();
    }

    private void assertNoInsertion() {
        verify(ime, never()).applyOpenSwipeSuggestions(any());
    }

    @Test
    public void unchangedEditorReceivesResultOnce() {
        handler.onStartBatchInput();
        finish();
        deliver(0);
        deliver(0);
        verify(ime, times(1)).applyOpenSwipeSuggestions(suggestions);
        assertFalse(handler.isInBatchInput());
    }

    @Test
    public void connectionSwitchDuringGestureDiscardsResult() {
        handler.onStartBatchInput();
        when(ime.getCurrentInputConnection()).thenReturn(mock(InputConnection.class));
        finish();
        if (!replies.isEmpty()) deliver(0);
        assertNoInsertion();
        assertFalse(handler.isInBatchInput());
    }

    @Test
    public void editorSwitchDuringGestureDiscardsResult() {
        handler.onStartBatchInput();
        when(ime.getCurrentInputEditorInfo()).thenReturn(new EditorInfo());
        finish();
        if (!replies.isEmpty()) deliver(0);
        assertNoInsertion();
    }

    @Test
    public void missingConnectionAtGestureStartDiscardsResult() {
        when(ime.getCurrentInputConnection()).thenReturn(null);
        handler.onStartBatchInput();
        when(ime.getCurrentInputConnection()).thenReturn(connection);
        finish();
        if (!replies.isEmpty()) deliver(0);
        assertNoInsertion();
    }

    @Test
    public void missingEditorAtGestureStartDiscardsResult() {
        when(ime.getCurrentInputEditorInfo()).thenReturn(null);
        handler.onStartBatchInput();
        when(ime.getCurrentInputEditorInfo()).thenReturn(editor);
        finish();
        if (!replies.isEmpty()) deliver(0);
        assertNoInsertion();
    }

    @Test
    public void layoutSwitchDuringGestureDiscardsResult() {
        handler.onStartBatchInput();
        when(switcher.getKeyboard()).thenReturn(mock(Keyboard.class));
        finish();
        if (!replies.isEmpty()) deliver(0);
        assertNoInsertion();
    }

    @Test
    public void cancellationDiscardsInFlightDecode() {
        handler.onStartBatchInput();
        finish();
        handler.cancelSwipe();
        deliver(0);
        assertNoInsertion();
    }

    @Test
    public void cancellationAfterWorkerReplyDiscardsPostedResult() {
        handler.onStartBatchInput();
        finish();
        replies.get(0).accept(suggestions);
        handler.cancelSwipe();
        uiTasks.remove(0).run();
        assertNoInsertion();
    }

    @Test
    public void staleReplyDoesNotEndNewGesture() {
        handler.onStartBatchInput();
        finish();
        handler.onStartBatchInput();
        deliver(0);
        assertNoInsertion();
        assertTrue(handler.isInBatchInput());
        finish();
        deliver(1);
        verify(ime, times(1)).applyOpenSwipeSuggestions(suggestions);
    }

    @Test
    public void connectionSwitchAfterDecodeDiscardsResult() {
        handler.onStartBatchInput();
        finish();
        when(ime.getCurrentInputConnection()).thenReturn(mock(InputConnection.class));
        deliver(0);
        assertNoInsertion();
    }

    @Test
    public void editorSwitchAfterDecodeDiscardsResult() {
        handler.onStartBatchInput();
        finish();
        when(ime.getCurrentInputEditorInfo()).thenReturn(new EditorInfo());
        deliver(0);
        assertNoInsertion();
    }

    @Test
    public void layoutSwitchAfterDecodeDiscardsResult() {
        handler.onStartBatchInput();
        finish();
        when(switcher.getKeyboard()).thenReturn(mock(Keyboard.class));
        deliver(0);
        assertNoInsertion();
    }

    @Test
    public void cursorOrPrivacyChangeAfterDecodeDiscardsResult() {
        handler.onStartBatchInput();
        finish();
        when(logic.canApplyOpenSwipeAt(0)).thenReturn(false);
        deliver(0);
        assertNoInsertion();
    }

    @Test
    public void hiddenKeyboardDiscardsResult() {
        handler.onStartBatchInput();
        finish();
        when(ime.isInputViewShown()).thenReturn(false);
        deliver(0);
        assertNoInsertion();
    }

    @Test
    public void detachedViewDiscardsResult() {
        handler.onStartBatchInput();
        finish();
        when(switcher.getMainKeyboardView()).thenReturn(mock(MainKeyboardView.class));
        deliver(0);
        assertNoInsertion();
    }
}
