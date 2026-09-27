package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.DayNightMode
import com.example.model.NavTab
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Remix Lighthouse", appName)
    }

    @Test
    fun `verify navigation tabs and default modes`() {
        assertEquals("Map", NavTab.MAP.title)
        assertEquals("Walk", NavTab.WALK.title)
        assertEquals("Community", NavTab.COMMUNITY.title)
        assertEquals("Safety", NavTab.SAFETY.title)
        assertEquals(DayNightMode.DAY, DayNightMode.valueOf("DAY"))
        assertEquals(DayNightMode.NIGHT, DayNightMode.valueOf("NIGHT"))
    }

    @Test
    fun `verify safe havens and map suggestions data`() {
        val havens = com.example.model.MapDataDefaults.safeHavens
        assertEquals(3, havens.size)
        val biRite = havens.find { it.id == "haven_birite" }
        assertNotNull(biRite)
        assertEquals("Bi-Rite Creamery & Grocery", biRite?.name)

        val suggestions = com.example.model.MapDataDefaults.searchSuggestions
        assert(suggestions.size >= 5)
        assertEquals("Mission Dolores Park", suggestions[0].title)

        val dayPolyline = com.example.model.MapDataDefaults.dayBestPolyline
        assert(dayPolyline.size >= 2)
    }

    @Test
    fun `verify destination selection opens route review and can be cleared`() {
        val context = ApplicationProvider.getApplicationContext<android.app.Application>()
        val viewModel = com.example.viewmodel.LighthouseViewModel(context)

        viewModel.setSearchQuery("Tartine")
        assertEquals("Tartine", viewModel.searchQuery.value)

        val tartine = com.example.model.MapDataDefaults.searchSuggestions.first { it.title.contains("Tartine") }
        viewModel.selectSearchPlace(tartine)
        assertEquals("Tartine Bakery", viewModel.destinationName.value)
        assertEquals(true, viewModel.isDestinationSelected.value)
        assertEquals(true, viewModel.isRouteSheetOpen.value)

        viewModel.clearDestination()
        assertEquals(false, viewModel.isDestinationSelected.value)
        assertEquals(false, viewModel.isRouteSheetOpen.value)
    }

    @Test
    fun `verify route estimated steps and calories calculation`() {
        val route = com.example.model.RouteOption(
            id = "test_route",
            title = "Valencia St",
            badge = "Safe",
            badgeColorType = "RECOMMENDED",
            durationMinutes = 14,
            distanceMiles = 0.6f,
            elevationFt = 20,
            clarityOrLitScore = "98% Clarity",
            bulletPoints = emptyList(),
            contextNote = "Test"
        )
        // 0.6 mi * 2150 ≈ 1290 steps
        assertEquals(1290, route.estimatedSteps)
        // 0.6 mi * 68 ≈ 40 kcal
        assertEquals(40, route.estimatedCalories)
    }

    @Test
    fun `verify plain map default state and route option selection`() {
        val context = ApplicationProvider.getApplicationContext<android.app.Application>()
        val viewModel = com.example.viewmodel.LighthouseViewModel(context)

        // Plain map by default
        assertEquals(false, viewModel.isDestinationSelected.value)
        assertEquals(false, viewModel.isRouteSheetOpen.value)

        viewModel.selectRouteById("night_illuminated")
        assertEquals("night_illuminated", viewModel.selectedRouteId.value)
        assertEquals(true, viewModel.isDestinationSelected.value)
    }

    @Test
    fun `verify public voice companion fails closed for navigation and crisis phrases`() = kotlinx.coroutines.runBlocking {
        val companion = com.example.service.GeminiSafetyCompanion()

        listOf(
            "Lighthouse, find me a safe walk back to the train station",
            "Yes, start navigation",
            "Someone is following me, I feel unsafe"
        ).forEach { query ->
            val response = companion.converseWithLive(
                userQuery = query,
                currentOrigin = "Valencia St",
                currentDestination = "Mission Dolores Park",
                dayNightMode = DayNightMode.DAY,
                activeRoute = null,
                allRoutes = emptyList()
            )
            assertEquals(com.example.service.GeminiLiveAudioEngine.UNAVAILABLE_MESSAGE, response.spokenText)
            assertEquals(com.example.service.GeminiAction.NONE, response.actionType)
        }
    }
}
