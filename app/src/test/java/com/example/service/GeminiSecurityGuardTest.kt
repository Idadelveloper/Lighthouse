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

    private val companion = GeminiSafetyCompanion()

    @Test
    fun `guard - GeminiSafetyCompanion has no client api key field or remote url field`() {
        val fields = GeminiSafetyCompanion::class.java.declaredFields
        val fieldNames = fields.map { it.name.lowercase() }

        assertFalse("GeminiSafetyCompanion must not have an apiKey field", fieldNames.any { it.contains("apikey") })
        assertFalse("GeminiSafetyCompanion must not have an okhttp client field", fieldNames.any { it == "client" })
    }

    @Test
    fun `guard - fail-closed deterministic companion responds without network or keys`() = runBlocking {
        val response = companion.converseWithLive(
            userQuery = "Take me to Dolores Park",
            currentOrigin = "16th St Mission BART",
            currentDestination = "Mission Dolores Park",
            dayNightMode = DayNightMode.DAY,
            activeRoute = null,
            allRoutes = emptyList()
        )

        assertNotNull(response)
        assertTrue(response.spokenText.contains("Dolores Park", ignoreCase = true))
        assertEquals(GeminiAction.SET_DESTINATION, response.actionType)
        assertEquals("Mission Dolores Park", response.actionTarget)
    }

    @Test
    fun `guard - fail-closed companion handles safe havens and route actions`() = runBlocking {
        val tartineResponse = companion.converseWithLive(
            userQuery = "Where is Tartine Bakery?",
            currentOrigin = "16th St Mission BART",
            currentDestination = "Mission Dolores Park",
            dayNightMode = DayNightMode.DAY,
            activeRoute = null,
            allRoutes = emptyList()
        )
        assertEquals(GeminiAction.SET_DESTINATION, tartineResponse.actionType)
        assertEquals("Tartine Bakery", tartineResponse.actionTarget)

        val fastestResponse = companion.converseWithLive(
            userQuery = "Switch to the fastest route",
            currentOrigin = "16th St Mission BART",
            currentDestination = "Mission Dolores Park",
            dayNightMode = DayNightMode.NIGHT,
            activeRoute = null,
            allRoutes = emptyList()
        )
        assertEquals(GeminiAction.SWITCH_ROUTE, fastestResponse.actionType)
        assertEquals("night_fastest", fastestResponse.actionTarget)
    }

    @Test
    fun `guard - source files contain no durable Gemini API key or direct key url calls`() {
        val rootDir = File(".").canonicalFile
        val mainJavaDir = File(rootDir, "src/main/java")
        val candidateDirs = listOf(
            mainJavaDir,
            File(rootDir, "app/src/main/java")
        )
        val sourceDir = candidateDirs.firstOrNull { it.exists() && it.isDirectory }
        assertNotNull("Main Java directory must exist for security audit", sourceDir)

        val forbiddenTokens = listOf(
            "BuildConfig.GEMINI_API_KEY",
            "generativelanguage.googleapis.com",
            "?key="
        )

        sourceDir?.walkTopDown()?.filter { it.extension in listOf("kt", "java") }?.forEach { sourceFile ->
            val content = sourceFile.readText()
            forbiddenTokens.forEach { token ->
                assertFalse(
                    "Security violation: ${sourceFile.name} contains forbidden durable key token '$token'",
                    content.contains(token)
                )
            }
        }
    }

    @Test
    fun `guard - env example does not contain GEMINI_API_KEY`() {
        val rootDir = File(".").canonicalFile
        val candidateFiles = listOf(
            File(rootDir, ".env.example"),
            File(rootDir, "../.env.example")
        )
        val envExampleFile = candidateFiles.firstOrNull { it.exists() }
        assertNotNull(".env.example must exist", envExampleFile)

        val content = envExampleFile?.readText() ?: ""
        assertFalse(
            "Security violation: .env.example must not expose GEMINI_API_KEY",
            content.contains("GEMINI_API_KEY")
        )
        assertTrue(
            ".env.example should retain MAPS_API_KEY placeholder",
            content.contains("MAPS_API_KEY")
        )
    }
}
