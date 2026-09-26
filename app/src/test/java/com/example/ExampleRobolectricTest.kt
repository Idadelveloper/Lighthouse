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
    fun `verify search bar query filtering, categories and recent searches`() {
        val context = ApplicationProvider.getApplicationContext<android.app.Application>()
        val viewModel = com.example.viewmodel.LighthouseViewModel(context)

        // 1. Search query filters locations
        viewModel.setSearchQuery("Tartine")
        val results = viewModel.searchResults.value
        assert(results.any { it.title.contains("Tartine", ignoreCase = true) })

        // 2. Category filtering
        viewModel.setSearchCategoryFilter("TRANSIT")
        assert(viewModel.searchCategoryFilter.value == "TRANSIT")

        // 3. Select search place updates destination and recent searches
        val tartine = com.example.model.MapDataDefaults.searchSuggestions.first { it.title.contains("Tartine") }
        viewModel.selectSearchPlace(tartine)
        assertEquals("Tartine Bakery", viewModel.destinationName.value)
        assertEquals(true, viewModel.hasActiveRoute.value)
        assert(viewModel.recentSearches.value.any { it.title == "Tartine Bakery" })

        // 4. Clear recent searches
        viewModel.clearRecentSearches()
        assertEquals(0, viewModel.recentSearches.value.size)
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
    fun `verify plain map default state and route selection`() {
        val context = ApplicationProvider.getApplicationContext<android.app.Application>()
        val viewModel = com.example.viewmodel.LighthouseViewModel(context)

        // Plain map by default
        assertEquals(false, viewModel.hasActiveRoute.value)
        assertEquals(false, viewModel.isRouteSheetOpen.value)

        // Select a destination option after search
        val place = com.example.model.MapDataDefaults.searchSuggestions[0]
        viewModel.selectSearchPlace(place)
        assertEquals(true, viewModel.hasActiveRoute.value)
        assertEquals(true, viewModel.isRouteSheetOpen.value)

        // Clear route returns to plain map
        viewModel.clearActiveRoute()
        assertEquals(false, viewModel.hasActiveRoute.value)
        assertEquals(false, viewModel.isRouteSheetOpen.value)
    }

    @Test
    fun `verify gemini live persona confirmation and crisis detection`() = kotlinx.coroutines.runBlocking {
        val companion = com.example.service.GeminiSafetyCompanion()

        // 1. Destination request asks for confirmation before navigation
        val destResponse = companion.converseWithLive(
            userQuery = "Lighthouse, find me a safe walk back to the train station",
            currentOrigin = "Valencia St",
            currentDestination = "Mission Dolores Park",
            dayNightMode = DayNightMode.DAY,
            activeRoute = null,
            allRoutes = emptyList()
        )
        assert(destResponse.spokenText.contains("Should I start navigation?", ignoreCase = true))
        assertEquals(com.example.service.GeminiAction.SET_DESTINATION, destResponse.actionType)

        // 2. User confirms navigation starts walk
        val confirmResponse = companion.converseWithLive(
            userQuery = "Yes, start navigation",
            currentOrigin = "Valencia St",
            currentDestination = "Mission Dolores Park",
            dayNightMode = DayNightMode.DAY,
            activeRoute = null,
            allRoutes = emptyList()
        )
        assertEquals(com.example.service.GeminiAction.START_WALK, confirmResponse.actionType)

        // 3. Crisis detector triggers crisis protocol and keeps line open
        val crisisResponse = companion.converseWithLive(
            userQuery = "Someone is following me, I feel unsafe",
            currentOrigin = "Valencia St",
            currentDestination = "Mission Dolores Park",
            dayNightMode = DayNightMode.DAY,
            activeRoute = null,
            allRoutes = emptyList()
        )
        assertEquals(com.example.service.GeminiAction.CRISIS_PROTOCOL, crisisResponse.actionType)
        assert(crisisResponse.spokenText.contains("staying on the line", ignoreCase = true) ||
                crisisResponse.spokenText.contains("safe", ignoreCase = true))
    }
}
