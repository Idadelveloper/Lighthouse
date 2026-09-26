package com.example

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class AppCheckSecurityBootstrapTest {

    @Test
    fun `guard - manifest registers LighthouseApplication`() {
        val rootDir = File(".").canonicalFile
        val candidateManifests = listOf(
            File(rootDir, "src/main/AndroidManifest.xml"),
            File(rootDir, "app/src/main/AndroidManifest.xml")
        )
        val manifestFile = candidateManifests.firstOrNull { it.exists() }
        assertNotNull("AndroidManifest.xml must exist", manifestFile)

        val manifestText = manifestFile?.readText() ?: ""
        assertTrue(
            "AndroidManifest.xml must register android:name='.LighthouseApplication'",
            manifestText.contains("android:name=\".LighthouseApplication\"")
        )
    }

    @Test
    fun `guard - build gradle configures playintegrity and debug-scoped appcheck`() {
        val rootDir = File(".").canonicalFile
        val candidateGradleFiles = listOf(
            File(rootDir, "build.gradle.kts"),
            File(rootDir, "app/build.gradle.kts")
        )
        val buildGradleFile = candidateGradleFiles.firstOrNull { it.exists() && it.readText().contains("dependencies") }
        assertNotNull("app/build.gradle.kts must exist", buildGradleFile)

        val buildGradleText = buildGradleFile?.readText() ?: ""

        // Verify playintegrity is used instead of recaptcha
        assertTrue(
            "build.gradle.kts must include firebase.appcheck.playintegrity",
            buildGradleText.contains("libs.firebase.appcheck.playintegrity")
        )
        assertFalse(
            "build.gradle.kts must NOT include firebase.appcheck.recaptcha",
            buildGradleText.contains("libs.firebase.appcheck.recaptcha")
        )

        // Verify debugImplementation is used for appcheck.debug
        assertTrue(
            "build.gradle.kts must scope firebase.appcheck.debug to debugImplementation",
            buildGradleText.contains("debugImplementation(libs.firebase.appcheck.debug)")
        )

        // Verify Firebase BoM and AI dependencies are preserved
        assertTrue(
            "build.gradle.kts must preserve firebase.bom",
            buildGradleText.contains("platform(libs.firebase.bom)")
        )
        assertTrue(
            "build.gradle.kts must preserve firebase.ai",
            buildGradleText.contains("libs.firebase.ai")
        )
    }

    @Test
    fun `guard - LighthouseApplication exists and has no hardcoded secrets`() {
        val rootDir = File(".").canonicalFile
        val candidateAppFiles = listOf(
            File(rootDir, "src/main/java/com/example/LighthouseApplication.kt"),
            File(rootDir, "app/src/main/java/com/example/LighthouseApplication.kt")
        )
        val appFile = candidateAppFiles.firstOrNull { it.exists() }
        assertNotNull("LighthouseApplication.kt must exist", appFile)

        val appText = appFile?.readText() ?: ""

        // Must initialize PlayIntegrity for release and DebugAppCheckProviderFactory for debug
        assertTrue(
            "LighthouseApplication must reference PlayIntegrityAppCheckProviderFactory",
            appText.contains("PlayIntegrityAppCheckProviderFactory")
        )
        assertTrue(
            "LighthouseApplication must reference DebugAppCheckProviderFactory for debug builds",
            appText.contains("DebugAppCheckProviderFactory")
        )

        // Must never hardcode, print or commit tokens or direct Gemini calls
        assertFalse("LighthouseApplication must not reference GEMINI_API_KEY", appText.contains("GEMINI_API_KEY"))
        assertFalse("LighthouseApplication must not reference generativelanguage.googleapis.com", appText.contains("generativelanguage"))
        assertFalse("LighthouseApplication must not hardcode tokens", appText.contains("FIREBASE_APPCHECK_DEBUG_TOKEN"))
    }
}
