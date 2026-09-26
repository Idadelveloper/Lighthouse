package com.example.service

import com.example.model.DayNightMode
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class GeminiLiveAudioIntegrationTest {

    @Test
    fun `guard - exact live model and voice constants configured`() {
        assertEquals("gemini-3.1-flash-live-preview", GeminiLiveAudioEngine.LIVE_MODEL_NAME)
        assertEquals("Sulafat", GeminiLiveAudioEngine.LIVE_VOICE_NAME)
        assertFalse(
            "gemini-3.8-live is prohibited as a Live model name",
            GeminiLiveAudioEngine.LIVE_MODEL_NAME.contains("3.8")
        )
    }

    @Test
    fun `guard - system instruction meets ethical and navigation guidance standards`() {
        val instruction = GeminiLiveAudioEngine.SYSTEM_INSTRUCTION
        assertTrue("Must mention 16th St Mission BART", instruction.contains("16th St Mission BART"))
        assertTrue("Must mention Mission Dolores Park", instruction.contains("Mission Dolores Park"))
        assertTrue("Must never guarantee safety", instruction.contains("Never guarantee safety"))
        assertTrue("Must distinguish SFPUC inventory", instruction.contains("SFPUC light pole inventory is not proof"))
        assertTrue("Must note 311 reports not exhaustive", instruction.contains("311 municipal reports are not exhaustive"))
        assertTrue("Must ask before suggesting route change", instruction.contains("Always ask before suggesting a route change"))
        assertTrue("Must prohibit inferring danger from protected classes or wealth", instruction.contains("Never infer danger from protected class"))
        assertTrue("Must defer emergencies to 911", instruction.contains("defer immediately to 911"))
    }

    @Test
    fun `guard - no legacy Android SpeechRecognizer or TextToSpeech in audio engine`() {
        val rootDir = File(".").canonicalFile
        val candidateFiles = listOf(
            File(rootDir, "src/main/java/com/example/service/GeminiLiveAudioEngine.kt"),
            File(rootDir, "app/src/main/java/com/example/service/GeminiLiveAudioEngine.kt")
        )
        val file = candidateFiles.firstOrNull { it.exists() }
        assertNotNull("GeminiLiveAudioEngine.kt must exist", file)

        val code = file?.readText() ?: ""
        assertFalse("Must not import SpeechRecognizer", code.contains("import android.speech.SpeechRecognizer"))
        assertFalse("Must not import TextToSpeech", code.contains("import android.speech.tts.TextToSpeech"))
        assertFalse("Must not instantiate SpeechRecognizer", code.contains("createSpeechRecognizer"))
        assertFalse("Must not instantiate TextToSpeech", code.contains("TextToSpeech("))
    }

    @Test
    fun `guard - uses official Firebase AI Live API without parallel receive loop`() {
        val rootDir = File(".").canonicalFile
        val candidateFiles = listOf(
            File(rootDir, "src/main/java/com/example/service/GeminiLiveAudioEngine.kt"),
            File(rootDir, "app/src/main/java/com/example/service/GeminiLiveAudioEngine.kt")
        )
        val file = candidateFiles.firstOrNull { it.exists() }
        assertNotNull("GeminiLiveAudioEngine.kt must exist", file)

        val code = file?.readText() ?: ""
        assertTrue("Must use Firebase.ai", code.contains("Firebase.ai"))
        assertTrue("Must use GenerativeBackend.googleAI()", code.contains("GenerativeBackend.googleAI()"))
        assertTrue("Must call liveModel", code.contains(".liveModel("))
        assertTrue("Must call connect()", code.contains(".connect()"))
        assertTrue("Must call startAudioConversation()", code.contains(".startAudioConversation("))
        assertFalse("Must not invoke parallel receive loop", code.contains(".receive("))
    }

    @Test
    fun `guard - session cleanup is coroutine safe and calls LiveSession close API`() {
        val rootDir = File(".").canonicalFile
        val candidateFiles = listOf(
            File(rootDir, "src/main/java/com/example/service/GeminiLiveAudioEngine.kt"),
            File(rootDir, "app/src/main/java/com/example/service/GeminiLiveAudioEngine.kt")
        )
        val file = candidateFiles.firstOrNull { it.exists() }
        assertNotNull("GeminiLiveAudioEngine.kt must exist", file)

        val code = file?.readText() ?: ""
        // Mutex protected for coroutine-safety
        assertTrue("Must use Mutex to synchronize session lifecycle", code.contains("sessionMutex.withLock"))
        // Calls stopAudioConversation and session.close()
        assertTrue("Must call stopAudioConversation() on cleanup", code.contains("session.stopAudioConversation()"))
        assertTrue("Must call session.close() on cleanup", code.contains("session.close()"))
        // Cleans up prior session before starting new one
        assertTrue("Must clean up prior session before connect", code.contains("cleanUpSession()"))
    }

    @Test
    fun `guard - transcript accumulation, single debounce dispatch, and turn reset`() {
        val rootDir = File(".").canonicalFile
        val candidateFiles = listOf(
            File(rootDir, "src/main/java/com/example/service/GeminiLiveAudioEngine.kt"),
            File(rootDir, "app/src/main/java/com/example/service/GeminiLiveAudioEngine.kt")
        )
        val file = candidateFiles.firstOrNull { it.exists() }
        assertNotNull("GeminiLiveAudioEngine.kt must exist", file)

        val code = file?.readText() ?: ""
        // Input accumulation and reset
        assertTrue("Must use inputAccumulator", code.contains("inputAccumulator.append("))
        assertTrue("Must reset input buffer after debounce dispatch", code.contains("resetInputBuffer()"))
        // Output accumulation and reset
        assertTrue("Must use outputAccumulator", code.contains("outputAccumulator.append("))
        assertTrue("Must reset output buffer at turn boundary", code.contains("resetOutputBuffer()"))
        // Does not log or persist transcripts
        assertFalse("Must not log input transcript", code.contains("Log.d(") || code.contains("Log.i(") || code.contains("Log.v("))
    }

    @Test
    fun `guard - announceRouteSuggestion uses documented incremental content API with endOfTurn`() {
        val rootDir = File(".").canonicalFile
        val candidateFiles = listOf(
            File(rootDir, "src/main/java/com/example/service/GeminiLiveAudioEngine.kt"),
            File(rootDir, "app/src/main/java/com/example/service/GeminiLiveAudioEngine.kt")
        )
        val file = candidateFiles.firstOrNull { it.exists() }
        assertNotNull("GeminiLiveAudioEngine.kt must exist", file)

        val code = file?.readText() ?: ""
        // Must send Content with role = user and endOfTurn = true
        assertTrue("Must construct Content with role user", code.contains("Content("))
        assertTrue("Must specify role user", code.contains("role = \"user\""))
        assertTrue("Must call session.send(userContent, true)", code.contains("session.send(userContent, true)"))
    }

    @Test
    fun `guard - no secrets direct REST or url key parameters in codebase`() {
        val rootDir = File(".").canonicalFile
        val candidateDirs = listOf(
            File(rootDir, "src/main/java"),
            File(rootDir, "app/src/main/java")
        )
        val sourceDir = candidateDirs.firstOrNull { it.exists() && it.isDirectory }
        assertNotNull("Main Java directory must exist", sourceDir)

        val forbiddenTokens = listOf(
            "BuildConfig.GEMINI_API_KEY",
            "generativelanguage.googleapis.com",
            "?key="
        )

        sourceDir?.walkTopDown()?.filter { it.extension in listOf("kt", "java") }?.forEach { sourceFile ->
            val content = sourceFile.readText()
            forbiddenTokens.forEach { token ->
                assertFalse(
                    "Security violation: ${sourceFile.name} contains forbidden token '$token'",
                    content.contains(token)
                )
            }
        }
    }

    @Test
    fun `guard - deterministic companion handles explicit route commands without freeform mutation`() = runBlocking {
        val companion = GeminiSafetyCompanion()

        // Explicit command mutates
        val switchResponse = companion.converseWithLive(
            userQuery = "Switch to the fastest route",
            currentOrigin = "16th St Mission BART",
            currentDestination = "Mission Dolores Park",
            dayNightMode = DayNightMode.DAY,
            activeRoute = null,
            allRoutes = emptyList()
        )
        assertEquals(GeminiAction.SWITCH_ROUTE, switchResponse.actionType)
        assertEquals("day_fastest", switchResponse.actionTarget)

        // General conversational question does not mutate route
        val generalResponse = companion.converseWithLive(
            userQuery = "Is the sidewalk wide here?",
            currentOrigin = "16th St Mission BART",
            currentDestination = "Mission Dolores Park",
            dayNightMode = DayNightMode.DAY,
            activeRoute = null,
            allRoutes = emptyList()
        )
        assertEquals(GeminiAction.NONE, generalResponse.actionType)
    }
}
