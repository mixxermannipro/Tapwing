// SPDX-License-Identifier: GPL-3.0-or-later
package helium314.keyboard.voice

import android.app.Application
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [29, 33, 34])
class AndroidPcmSourceTest {
    @Test fun microphonePermissionDenialDoesNotCreateRecorder() {
        val app = RuntimeEnvironment.getApplication()
        val failure = assertFailsWith<CaptureFailure> { AndroidPcmSource.open(app) }
        assertEquals(DictationError.PERMISSION_DENIED, failure.reason)
    }
}
