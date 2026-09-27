package com.example.service

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.TestScope
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Keeps the former Live-integration test boundary fail closed in the public build.
 * A real streaming implementation and its device tests belong in the private product repository.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class GeminiLiveAudioIntegrationTest {

    @Test
    fun `public build exposes an unavailable voice session`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val engine = GeminiLiveAudioEngine(context, TestScope())

        engine.startListening()

        assertEquals(AudioSessionState.ERROR, engine.sessionState.value)
        assertEquals(GeminiLiveAudioEngine.UNAVAILABLE_MESSAGE, engine.liveTranscript.value)
        assertFalse(engine.isContinuousSessionActive())
    }
}
