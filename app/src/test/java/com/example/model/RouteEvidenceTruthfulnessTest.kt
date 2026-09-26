package com.example.model

import com.example.service.RouteRanker
import com.google.android.gms.maps.model.LatLng
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class RouteEvidenceTruthfulnessTest {

    @Test
    fun `known corridor evidence retains official source identifiers`() {
        assertEquals(
            "https://services.arcgis.com/Zs2aNLFN00jrS4gG/arcgis/rest/services/SFPUC_Streetlights/FeatureServer/0",
            KnownCorridorEvidence.SFPUC_URL
        )
        assertEquals("https://data.sf.gov/resource/vw6y-z8j6.json", KnownCorridorEvidence.SF311_URL)
        assertEquals("vw6y-z8j6", KnownCorridorEvidence.SF311_DATASET_ID)
    }

    @Test
    fun `evidence limitations preserve unknown and non exhaustive semantics`() {
        assertTrue(KnownCorridorEvidence.SFPUC_LIMITATION.contains("inventory only"))
        assertTrue(KnownCorridorEvidence.SFPUC_LIMITATION.contains("does not prove the lamp currently works"))
        assertTrue(KnownCorridorEvidence.SF311_LIMITATION.contains("Reports are not exhaustive"))
        assertTrue(KnownCorridorEvidence.SF311_LIMITATION.contains("no report does not mean no problem"))
        assertTrue(KnownCorridorEvidence.SHADE_LIMITATION.contains("No verified live official shade feed is connected yet"))
        assertTrue(KnownCorridorEvidence.SHADE_LIMITATION.contains("Status UNKNOWN"))
    }

    @Test
    fun `day and night evidence do not convert unavailable feeds into conditions`() {
        val dayEvidence = KnownCorridorEvidence.getEvidence(DayNightMode.DAY)
        val nightEvidence = KnownCorridorEvidence.getEvidence(DayNightMode.NIGHT)

        val dayShade = dayEvidence.find { it.category == "SHADE" }
        assertNotNull(dayShade)
        assertEquals(EvidenceStatus.UNKNOWN, dayShade?.status)
        assertFalse(dayShade?.summary?.contains("fully shaded", ignoreCase = true) == true)

        val night311 = nightEvidence.find { it.category == "311_REPORTS" }
        assertNotNull(night311)
        assertEquals(EvidenceStatus.UNKNOWN, night311?.status)
        assertEquals(KnownCorridorEvidence.SF311_URL, night311?.sourceUrl)

        val nightLighting = nightEvidence.find { it.category == "LIGHTING" }
        assertNotNull(nightLighting)
        assertEquals(EvidenceStatus.AVAILABLE, nightLighting?.status)
        assertEquals(KnownCorridorEvidence.SFPUC_URL, nightLighting?.sourceUrl)
    }

    @Test
    fun `production copy excludes fabricated capability phrases`() {
        val rootDir = File(".").canonicalFile
        val sourceDir = listOf(
            File(rootDir, "src/main/java"),
            File(rootDir, "app/src/main/java")
        ).firstOrNull { it.isDirectory }
        assertNotNull("Main source directory must exist", sourceDir)

        val forbiddenPhrases = listOf(
            "AMBIENT LUX", "LIVE TELEMETRY", "MESH LAYER", "GPS HOT", "CAD-CERTIFIED",
            "SF Emergency CAD API", "pre-authenticated", "path locked on illuminated", "Offline Mesh",
            "105 dB", "high-candela", "dispatched!", "Maya & Sarah notified", "96% lit", "61% Lit",
            "68% shaded", "fully shaded", "fully illuminated", "smart poles", "smart pole",
            "Luminescence Stream", "0 Open Tickets", "Gemini Street Assessment", "Walk Comfort Scores"
        )

        sourceDir?.walkTopDown()?.filter { it.extension in listOf("kt", "java") }?.forEach { file ->
            val content = file.readText()
            forbiddenPhrases.forEach { phrase ->
                assertFalse("${file.name} contains '$phrase'", content.contains(phrase, ignoreCase = true))
            }
        }

        val assetFile = listOf(
            File(rootDir, "src/main/assets/lighthouse_data.json"),
            File(rootDir, "app/src/main/assets/lighthouse_data.json")
        ).firstOrNull { it.exists() }
        assertNotNull("lighthouse_data.json must exist", assetFile)
        val assetContent = assetFile?.readText().orEmpty()
        forbiddenPhrases.forEach { phrase ->
            assertFalse("lighthouse_data.json contains '$phrase'", assetContent.contains(phrase, ignoreCase = true))
        }
    }

    @Test
    fun `route ranker uses duration distance and explicit step constraint`() {
        fun route(id: String, duration: Int, distance: Int, hasSteps: Boolean) = RouteData(
            id = id,
            via = id,
            durationS = duration,
            distanceM = distance,
            path = listOf(LatLng(37.76, -122.42)),
            hasSteps = hasSteps,
            samples = emptyList()
        )

        val slow = route("slow", 900, 1000, false)
        val fastWithSteps = route("fast_steps", 500, 600, true)
        val medium = route("medium", 700, 800, false)

        val normal = RouteRanker.rank(listOf(slow, fastWithSteps, medium))
        assertEquals("fast_steps", normal.fastestId)
        assertEquals("fast_steps", normal.recommendedId)

        val stepFree = RouteRanker.rank(listOf(slow, fastWithSteps, medium), stepFree = true)
        assertEquals("fast_steps", stepFree.fastestId)
        assertEquals("medium", stepFree.recommendedId)
    }
}
