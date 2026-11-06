/*
 * Copyright (C) 2012 The Android Open Source Project
 * modified
 * SPDX-License-Identifier: Apache-2.0 AND GPL-3.0-only
 */

package helium314.keyboard.latin.utils;

public final class JniUtils {
    private static final String TAG = JniUtils.class.getSimpleName();
    public static final String JNI_LIB_NAME = "jni_latinime";
    public static final boolean sHaveGestureLib = false;

    static {
        try {
            System.loadLibrary(JNI_LIB_NAME);
        } catch (UnsatisfiedLinkError error) {
            Log.w(TAG, "Could not load bundled typing library " + JNI_LIB_NAME, error);
        }
    }

    private JniUtils() {}

    public static void loadNativeLibrary() {
        // Ensures the static initializer is called.
    }
}
