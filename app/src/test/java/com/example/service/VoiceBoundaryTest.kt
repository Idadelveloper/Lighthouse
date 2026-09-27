package com.example.service

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.TestScope
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class VoiceBoundaryTest {

    @Test
    fun `disabled voice engine fails closed without invoking callbacks`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val engine = GeminiLiveAudioEngine(context, TestScope())
        var recognized = false
        var wakeWord = false
        engine.onUserQueryRecognized = { recognized = true }
        engine.onWakeWordDetected = { wakeWord = true }

        engine.startListening()

        assertEquals(AudioSessionState.ERROR, engine.sessionState.value)
        assertEquals(GeminiLiveAudioEngine.UNAVAILABLE_MESSAGE, engine.liveTranscript.value)
        assertFalse(engine.isContinuousSessionActive())
        assertFalse(recognized)
        assertFalse(wakeWord)

        engine.stopListening()
        assertEquals(AudioSessionState.IDLE, engine.sessionState.value)
        assertEquals("", engine.liveTranscript.value)
    }

    @Test
    fun `public build has no microphone permission or active Live implementation`() {
        val rootDir = File(".").canonicalFile
        val manifest = listOf(
            File(rootDir, "src/main/AndroidManifest.xml"),
            File(rootDir, "app/src/main/AndroidManifest.xml")
        ).firstOrNull { it.exists() }
        val engine = listOf(
            File(rootDir, "src/main/java/com/example/service/GeminiLiveAudioEngine.kt"),
            File(rootDir, "app/src/main/java/com/example/service/GeminiLiveAudioEngine.kt")
        ).firstOrNull { it.exists() }
        val modal = listOf(
            File(rootDir, "src/main/java/com/example/ui/components/GeminiLiveVoiceModal.kt"),
            File(rootDir, "app/src/main/java/com/example/ui/components/GeminiLiveVoiceModal.kt")
        ).firstOrNull { it.exists() }

        assertNotNull("AndroidManifest.xml must exist", manifest)
        assertNotNull("GeminiLiveAudioEngine.kt must exist", engine)
        assertNotNull("GeminiLiveVoiceModal.kt must exist", modal)

        val manifestText = manifest?.readText().orEmpty()
        val engineText = engine?.readText().orEmpty()
        val modalText = modal?.readText().orEmpty()

        assertFalse(manifestText.contains("android.permission.RECORD_AUDIO"))
        assertFalse(manifestText.contains("android.permission.MODIFY_AUDIO_SETTINGS"))
        assertTrue(manifestText.contains("android:allowBackup=\"false\""))

        listOf(
            "Firebase.ai",
            ".liveModel(",
            ".connect()",
            ".startAudioConversation(",
            "SpeechRecognizer",
            "TextToSpeech("
        ).forEach { token ->
            assertFalse("Active voice token must be absent: $token", engineText.contains(token))
        }
        assertFalse(modalText.contains("ActivityResultContracts.RequestPermission"))
        assertTrue(modalText.contains("does not request microphone access"))
    }
}
