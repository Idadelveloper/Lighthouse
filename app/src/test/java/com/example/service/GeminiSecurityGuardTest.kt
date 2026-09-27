package com.example.service

import com.example.model.DayNightMode
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class GeminiSecurityGuardTest {

    @Test
    fun `public companion returns unavailable and cannot mutate navigation`() = runBlocking {
        val companion = GeminiSafetyCompanion()
        val response = companion.converseWithLive(
            userQuery = "Switch to the fastest route",
            currentOrigin = "Origin",
            currentDestination = "Destination",
            dayNightMode = DayNightMode.NIGHT,
            activeRoute = null,
            allRoutes = emptyList()
        )

        assertEquals(GeminiLiveAudioEngine.UNAVAILABLE_MESSAGE, response.spokenText)
        assertEquals(GeminiAction.NONE, response.actionType)
        assertEquals("", response.actionTarget)
        assertEquals(GeminiLiveAudioEngine.UNAVAILABLE_MESSAGE, companion.askCompanion("hello"))
    }

    @Test
    fun `source contains no durable Gemini key or direct Gemini REST path`() {
        val rootDir = File(".").canonicalFile
        val sourceDir = listOf(
            File(rootDir, "src/main/java"),
            File(rootDir, "app/src/main/java")
        ).firstOrNull { it.exists() && it.isDirectory }
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
    fun `env example contains only the Maps placeholder`() {
        val rootDir = File(".").canonicalFile
        val envExampleFile = listOf(
            File(rootDir, ".env.example"),
            File(rootDir, "../.env.example")
        ).firstOrNull { it.exists() }
        assertNotNull(".env.example must exist", envExampleFile)

        val content = envExampleFile?.readText().orEmpty()
        assertFalse(content.contains("GEMINI_API_KEY"))
        assertTrue(content.contains("MAPS_API_KEY"))
    }
}
