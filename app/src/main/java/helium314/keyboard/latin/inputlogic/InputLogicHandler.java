/*
 * Copyright (C) 2013 The Android Open Source Project
 * modified
 * SPDX-License-Identifier: Apache-2.0 AND GPL-3.0-only
 */

package helium314.keyboard.latin.inputlogic;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import helium314.keyboard.keyboard.Keyboard;
import helium314.keyboard.keyboard.KeyboardSwitcher;
import helium314.keyboard.keyboard.MainKeyboardView;
import helium314.keyboard.latin.CapsMode;
import helium314.keyboard.latin.swipe.OpenSwipeWorker;

import helium314.keyboard.latin.LatinIME;
import helium314.keyboard.latin.SuggestedWords;
import helium314.keyboard.latin.common.InputPointers;

/**
 * A helper to manage deferred tasks for the input logic.
 */
class InputLogicHandler implements Handler.Callback {
    final Handler mNonUIThreadHandler;
    final LatinIME.UIHandler mLatinIMEHandler;
    final InputLogic mInputLogic;
    private final OpenSwipeWorker mSwipeWorker;
    private boolean mInBatchInput;
    private long mSwipeGeneration;
    private Keyboard mSwipeKeyboardAtStart;

    private static final int MSG_GET_SUGGESTED_WORDS = 1;

    public InputLogicHandler(final LatinIME.UIHandler latinIMEHandler, final InputLogic inputLogic) {
        final HandlerThread handlerThread = new HandlerThread(
                InputLogicHandler.class.getSimpleName());
        handlerThread.start();
        mNonUIThreadHandler = new Handler(handlerThread.getLooper(), this);
        mLatinIMEHandler = latinIMEHandler;
        mInputLogic = inputLogic;
        mSwipeWorker = new OpenSwipeWorker(inputLogic.mLatinIME);
    }

    public void reset() {
        mNonUIThreadHandler.removeCallbacksAndMessages(null);
        cancelSwipe();
    }

    /**
     * Handle a message.
     * @see android.os.Handler.Callback#handleMessage(android.os.Message)
     */
    // Called on the Non-UI handler thread by the Handler code.
    @Override
    public boolean handleMessage(final Message msg) {
        if (msg.what == MSG_GET_SUGGESTED_WORDS)
            ((Runnable)msg.obj).run();
        return true;
    }

    // Swipe state belongs to the UI thread. Worker replies are checked after posting back.
    public void onStartBatchInput() {
        cancelSwipe();
        mSwipeKeyboardAtStart = KeyboardSwitcher.getInstance().getKeyboard();
        mInBatchInput = mSwipeKeyboardAtStart != null;
    }

    public boolean isInBatchInput() {
        return mInBatchInput;
    }

    public void cancelSwipe() {
        if (!mInBatchInput) return;
        mSwipeGeneration++;
        mSwipeWorker.cancelQueuedWork();
        mSwipeKeyboardAtStart = null;
        mInBatchInput = false;
    }

    public void prepareSwipe(final Keyboard keyboard) {
        mSwipeWorker.prepare(keyboard);
    }

    public void finishSwipe(final InputPointers points, final CapsMode caps, final int cursor) {
        final LatinIME ime = mInputLogic.mLatinIME;
        final KeyboardSwitcher switcher = KeyboardSwitcher.getInstance();
        final Keyboard keyboard = switcher.getKeyboard();
        final MainKeyboardView view = switcher.getMainKeyboardView();
        final InputConnection connection = ime.getCurrentInputConnection();
        final EditorInfo editor = ime.getCurrentInputEditorInfo();
        if (!mInBatchInput || keyboard == null || keyboard != mSwipeKeyboardAtStart
                || view == null || connection == null || editor == null
                || cursor < 0 || !mInputLogic.canApplyOpenSwipeAt(cursor)) {
            cancelSwipe();
            return;
        }
        final long generation = mSwipeGeneration;
        mSwipeWorker.decode(keyboard, view, points, caps, suggestions -> mLatinIMEHandler.post(() -> {
            if (generation != mSwipeGeneration) return;
            mInBatchInput = false;
            mSwipeKeyboardAtStart = null;
            mSwipeGeneration++;
            if (connection != ime.getCurrentInputConnection() || editor != ime.getCurrentInputEditorInfo()
                    || keyboard != switcher.getKeyboard() || view != switcher.getMainKeyboardView()
                    || !ime.isInputViewShown() || !mInputLogic.canApplyOpenSwipeAt(cursor)) return;
            ime.applyOpenSwipeSuggestions(suggestions);
        }));
    }

    public void closeSwipeWorker() {
        cancelSwipe();
        mSwipeWorker.close();
    }

    public void getSuggestedWords(final Runnable callback) {
        mNonUIThreadHandler.obtainMessage(MSG_GET_SUGGESTED_WORDS, callback).sendToTarget();
    }
}
