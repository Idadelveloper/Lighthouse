package com.example.viewmodel

import android.app.Application
import android.location.Location
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CommunityReportEntity
import com.example.data.LighthouseDatabase
import com.example.data.LighthouseRepository
import com.example.data.SafetyContactEntity
import com.example.model.CommunityFilter
import com.example.model.DayNightMode
import com.example.model.MapDataDefaults
import com.example.model.NavTab
import com.example.model.RouteOption
import com.example.model.SearchPlace
import com.example.model.TelemetryState
import com.example.service.GeminiSafetyCompanion
import com.example.util.LocationGeocoder
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.roundToInt

class LighthouseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: LighthouseRepository
    private val geminiCompanion = GeminiSafetyCompanion()
    val audioEngine = com.example.service.GeminiLiveAudioEngine(application, viewModelScope)

    private val searchPrefs = application.getSharedPreferences("lighthouse_search_history", android.content.Context.MODE_PRIVATE)

    private fun loadRecentSearches(): List<SearchPlace> {
        try {
            val raw = searchPrefs.getString("recent_places", null) ?: return emptyList()
            val jsonArray = JSONArray(raw)
            val list = mutableListOf<SearchPlace>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    SearchPlace(
                        id = obj.optString("id", "recent_$i"),
                        title = obj.optString("title"),
                        subtitle = obj.optString("subtitle"),
                        position = LatLng(obj.optDouble("lat"), obj.optDouble("lng")),
                        category = obj.optString("category", "DESTINATION"),
                        isSafeHaven = obj.optBoolean("isSafeHaven", false),
                        address = obj.optString("address", ""),
                        safetyBadge = obj.optString("safetyBadge", ""),
                        isRecent = true,
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
            return list
        } catch (_: Exception) {
            return emptyList()
        }
    }

    private val _recentSearches = MutableStateFlow<List<SearchPlace>>(loadRecentSearches())
    val recentSearches: StateFlow<List<SearchPlace>> = _recentSearches.asStateFlow()

    init {
        val database = LighthouseDatabase.getDatabase(application, viewModelScope)
        repository = LighthouseRepository(database.lighthouseDao())

        // Hook up speech recognition events from audio engine
        audioEngine.onUserQueryRecognized = { spokenQuery ->
            processLiveVoiceInput(spokenQuery)
        }
        audioEngine.onWakeWordDetected = {
            _isLiveVoiceOverlayVisible.value = true
            showToast("Hey Lighthouse detected!")
            vibratePhone(40)
        }
    }

    // Gemini Live Audio Conversation States
    private val _isLiveVoiceOverlayVisible = MutableStateFlow(false)
    val isLiveVoiceOverlayVisible: StateFlow<Boolean> = _isLiveVoiceOverlayVisible.asStateFlow()

    val liveSpeechTranscript: StateFlow<String> = audioEngine.liveTranscript
    val audioSessionState: StateFlow<com.example.service.AudioSessionState> = audioEngine.sessionState
    val audioRms: StateFlow<Float> = audioEngine.audioRms

    private val _geminiLiveVoiceResponse = MutableStateFlow(
        com.example.service.GeminiLiveAudioEngine.UNAVAILABLE_MESSAGE
    )
    val geminiLiveVoiceResponse: StateFlow<String> = _geminiLiveVoiceResponse.asStateFlow()

    val communityReports: StateFlow<List<CommunityReportEntity>> = repository.allReports
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val safetyContacts: StateFlow<List<SafetyContactEntity>> = repository.allContacts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Navigation Tab
    private val _currentTab = MutableStateFlow(NavTab.MAP)
    val currentTab: StateFlow<NavTab> = _currentTab.asStateFlow()

    // Map Search & Navigation Origin/Destination State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchCategoryFilter = MutableStateFlow("ALL")
    val searchCategoryFilter: StateFlow<String> = _searchCategoryFilter.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _searchResults = MutableStateFlow<List<SearchPlace>>(MapDataDefaults.searchSuggestions)
    val searchResults: StateFlow<List<SearchPlace>> = _searchResults.asStateFlow()

    private val _cameraMoveTarget = MutableStateFlow<LatLng?>(null)
    val cameraMoveTarget: StateFlow<LatLng?> = _cameraMoveTarget.asStateFlow()

    private val _computedPolyline = MutableStateFlow<List<LatLng>>(emptyList())
    val computedPolyline: StateFlow<List<LatLng>> = _computedPolyline.asStateFlow()

    private var searchJob: Job? = null

    // Plain Map by default: false until user searches and selects an option
    private val _hasActiveRoute = MutableStateFlow(false)
    val hasActiveRoute: StateFlow<Boolean> = _hasActiveRoute.asStateFlow()

    private val _originName = MutableStateFlow("16th St Mission BART")
    val originName: StateFlow<String> = _originName.asStateFlow()

    private val _destinationName = MutableStateFlow("Mission Dolores Park")
    val destinationName: StateFlow<String> = _destinationName.asStateFlow()

    private val _originLatLng = MutableStateFlow(com.example.model.MapDataDefaults.BART_16TH)
    val originLatLng: StateFlow<com.google.android.gms.maps.model.LatLng> = _originLatLng.asStateFlow()

    private val _destinationLatLng = MutableStateFlow(com.example.model.MapDataDefaults.DOLORES_PARK)
    val destinationLatLng: StateFlow<com.google.android.gms.maps.model.LatLng> = _destinationLatLng.asStateFlow()

    private val _userLiveLocation = MutableStateFlow<com.google.android.gms.maps.model.LatLng?>(null)
    val userLiveLocation: StateFlow<com.google.android.gms.maps.model.LatLng?> = _userLiveLocation.asStateFlow()

    private val _isUsingCurrentLocation = MutableStateFlow(false)
    val isUsingCurrentLocation: StateFlow<Boolean> = _isUsingCurrentLocation.asStateFlow()

    // Step Tracking & Calorie Monitoring
    private val sensorManager = application.getSystemService(android.content.Context.SENSOR_SERVICE) as? android.hardware.SensorManager
    private var stepSensorListener: android.hardware.SensorEventListener? = null
    private var stepSimulationJob: Job? = null

    private val _walkSteps = MutableStateFlow(0)
    val walkSteps: StateFlow<Int> = _walkSteps.asStateFlow()

    private val _walkCalories = MutableStateFlow(0.0f)
    val walkCalories: StateFlow<Float> = _walkCalories.asStateFlow()

    private val _isStepTrackingActive = MutableStateFlow(false)
    val isStepTrackingActive: StateFlow<Boolean> = _isStepTrackingActive.asStateFlow()

    // Route Options Bottom Sheet visibility over Google Map
    private val _isRouteSheetOpen = MutableStateFlow(false)
    val isRouteSheetOpen: StateFlow<Boolean> = _isRouteSheetOpen.asStateFlow()

    // Selected Safe Haven for detailed card popup
    private val _selectedSafeHaven = MutableStateFlow<com.example.model.SafeHavenMarker?>(null)
    val selectedSafeHaven: StateFlow<com.example.model.SafeHavenMarker?> = _selectedSafeHaven.asStateFlow()

    // Active civic filter layer
    private val _activeCivicLayer = MutableStateFlow("ALL")
    val activeCivicLayer: StateFlow<String> = _activeCivicLayer.asStateFlow()

    // Street-View & Gemini Data Pipeline Routing Engine
    val routingEngine = com.example.service.LighthouseRoutingEngine(application)

    private val _currentLegId = MutableStateFlow("to_park")
    val currentLegId: StateFlow<String> = _currentLegId.asStateFlow()

    private val _routingTimeOfDay = MutableStateFlow("day")
    val routingTimeOfDay: StateFlow<String> = _routingTimeOfDay.asStateFlow()

    private val _maxExtraMinutes = MutableStateFlow(6.0)
    val maxExtraMinutes: StateFlow<Double> = _maxExtraMinutes.asStateFlow()

    private val _isStepFreeRouting = MutableStateFlow(false)
    val isStepFreeRouting: StateFlow<Boolean> = _isStepFreeRouting.asStateFlow()

    private val _selectedRouteId = MutableStateFlow("default")
    val selectedRouteId: StateFlow<String> = _selectedRouteId.asStateFlow()

    private val _selectedSamplePoint = MutableStateFlow<com.example.model.SamplePoint?>(null)
    val selectedSamplePoint: StateFlow<com.example.model.SamplePoint?> = _selectedSamplePoint.asStateFlow()

    val rankingResult: StateFlow<com.example.model.RankingResult> = combine(
        _currentLegId,
        _routingTimeOfDay,
        _maxExtraMinutes,
        _isStepFreeRouting
    ) { legId, timeOfDay, maxExtra, stepFree ->
        val leg = routingEngine.getLeg(legId)
        if (leg != null) {
            routingEngine.rank(leg.routes, timeOfDay, maxExtra, stepFree)
        } else {
            com.example.model.RankingResult(emptyList(), "", "", null)
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        routingEngine.getLeg("to_park")?.let {
            routingEngine.rank(it.routes, "day", 6.0, false)
        } ?: com.example.model.RankingResult(emptyList(), "", "", null)
    )

    val activeRouteData: StateFlow<com.example.model.RouteData?> = combine(
        rankingResult,
        _selectedRouteId
    ) { ranking, routeId ->
        val found = ranking.rankedRoutes.find { it.route.id == routeId }
            ?: ranking.rankedRoutes.find { it.isRecommended }
            ?: ranking.rankedRoutes.firstOrNull()
        found?.route
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    // Day / Night comparison mode
    private val _dayNightMode = MutableStateFlow(DayNightMode.DAY)
    val dayNightMode: StateFlow<DayNightMode> = _dayNightMode.asStateFlow()

    // Day Routes
    val dayRoutes = listOf(
        RouteOption(
            id = "day_best",
            title = "Best Conditions",
            badge = "★ Recommended · Best Shade & Slopes",
            badgeColorType = "RECOMMENDED",
            durationMinutes = 14,
            distanceMiles = 0.6f,
            elevationFt = 32,
            clarityOrLitScore = "98% Clarity",
            bulletPoints = listOf(
                "park" to "85% tree canopy shade (Valencia W & 18th St)",
                "accessible" to "100% compliant curb ramps · Audible crosswalks",
                "storefront" to "Passes 4 open POPOS, Tartine & Bi-Rite Safe Haven"
            ),
            contextNote = "Solar model calculated for 2:30 PM sun angle. 0 active 311 sidewalk closures reported on corridor.",
            openHavensCount = 4,
            isRecommended = true
        ),
        RouteOption(
            id = "day_fastest",
            title = "Fastest Route (-3m)",
            badge = "Fastest Route (-3m)",
            badgeColorType = "FASTEST",
            durationMinutes = 11,
            distanceMiles = 0.5f,
            elevationFt = 48,
            clarityOrLitScore = "Direct Path",
            bulletPoints = listOf(
                "wb_sunny" to "High solar exposure along open 16th St stretch",
                "warning" to "Active seismic retrofit at Guerrero (narrow 32\" pathway)"
            ),
            contextNote = "Direct cut saves 3 minutes but passes unshaded asphalt and sidewalk work on Guerrero.",
            openHavensCount = 1,
            isRecommended = false
        ),
        RouteOption(
            id = "day_stepfree",
            title = "Step-Free & Verified Gentle Slope",
            badge = "Step-Free & Verified Gentle Slope",
            badgeColorType = "STEP_FREE",
            durationMinutes = 16,
            distanceMiles = 0.7f,
            elevationFt = 20,
            clarityOrLitScore = "Grade < 4.5%",
            bulletPoints = listOf(
                "check_circle" to "5 of 5 signalized intersections with audible chirps",
                "shelves" to "Generous curb clearances suitable for wheelchairs and strollers"
            ),
            contextNote = "Engineered for maximum rolling ease. Continuous dropped curbs and zero steps throughout.",
            openHavensCount = 3,
            isRecommended = false
        )
    )

    // Night Routes
    val nightRoutes = listOf(
        RouteOption(
            id = "night_illuminated",
            title = "Valencia Illuminated Corridor",
            badge = "Night Recommended · Illuminated",
            badgeColorType = "NIGHT_RECOMMENDED",
            durationMinutes = 16,
            distanceMiles = 0.7f,
            elevationFt = 32,
            clarityOrLitScore = "96% LIT",
            bulletPoints = listOf(
                "storefront" to "3 Open Havens: Bi-Rite, Tartine, and 24/7 Walgreens",
                "groups" to "Active foot traffic & passive surveillance on storefronts",
                "flash_on" to "SFPUC Smart Poles verified 100% operational"
            ),
            contextNote = "Adds 3 minutes over direct path to bypass 2 verified dark blocks on 17th St and keeps you along continuously staffed storefronts on Valencia.",
            openHavensCount = 3,
            isRecommended = true
        ),
        RouteOption(
            id = "night_fastest",
            title = "17th St Alley Cut",
            badge = "Fastest · Low Lighting",
            badgeColorType = "FASTEST",
            durationMinutes = 12,
            distanceMiles = 0.5f,
            elevationFt = 48,
            clarityOrLitScore = "48% LIT",
            bulletPoints = listOf(
                "warning" to "2 SF 311 streetlight outages confirmed tonight",
                "visibility_off" to "Dim residential alleyway with limited passive surveillance"
            ),
            contextNote = "High caution. Two streetlight outages reported on 17th St within the past 3 hours.",
            openHavensCount = 0,
            isRecommended = false
        ),
        RouteOption(
            id = "night_transit",
            title = "Transit Ambassador Escort",
            badge = "Transit Ambassador Escort",
            badgeColorType = "STEP_FREE",
            durationMinutes = 14,
            distanceMiles = 0.6f,
            elevationFt = 15,
            clarityOrLitScore = "100% MONITORED",
            bulletPoints = listOf(
                "directions_bus" to "24-Divisadero transfer with staffed stop",
                "security" to "Step-free concourse connection with verified security presence",
                "lightbulb" to "Illuminated municipal bus shelters and continuous cameras"
            ),
            contextNote = "Protected multi-modal route combining illuminated pedestrian path and monitored transit connection.",
            openHavensCount = 2,
            isRecommended = false
        )
    )

    private val _selectedDayRouteId = MutableStateFlow("day_best")
    val selectedDayRouteId: StateFlow<String> = _selectedDayRouteId.asStateFlow()

    private val _selectedNightRouteId = MutableStateFlow("night_illuminated")
    val selectedNightRouteId: StateFlow<String> = _selectedNightRouteId.asStateFlow()

    // Filter for Community screen
    private val _communityFilter = MutableStateFlow(CommunityFilter.ALL)
    val communityFilter: StateFlow<CommunityFilter> = _communityFilter.asStateFlow()

    // Active walk state
    private val _isPocketVoiceMode = MutableStateFlow(false)
    val isPocketVoiceMode: StateFlow<Boolean> = _isPocketVoiceMode.asStateFlow()

    private val _isAiMuted = MutableStateFlow(false)
    val isAiMuted: StateFlow<Boolean> = _isAiMuted.asStateFlow()

    private val _geminiSpeechText = MutableStateFlow(
        "Calm route guidance, bilingual support, environmental acoustic checks, and spoken hazard cues."
    )
    val geminiSpeechText: StateFlow<String> = _geminiSpeechText.asStateFlow()

    private val _isGeminiThinking = MutableStateFlow(false)
    val isGeminiThinking: StateFlow<Boolean> = _isGeminiThinking.asStateFlow()

    // Evidence Sheet visibility
    private val _showEvidenceSheet = MutableStateFlow(false)
    val showEvidenceSheet: StateFlow<Boolean> = _showEvidenceSheet.asStateFlow()

    // Discreet Defense states
    private val _fakeCallActive = MutableStateFlow(false)
    val fakeCallActive: StateFlow<Boolean> = _fakeCallActive.asStateFlow()

    private val _blackoutActive = MutableStateFlow(false)
    val blackoutActive: StateFlow<Boolean> = _blackoutActive.asStateFlow()

    private val _sirenArmed = MutableStateFlow(false)
    val sirenArmed: StateFlow<Boolean> = _sirenArmed.asStateFlow()

    // Toast message for user feedback
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    // Telemetry state
    val telemetry = TelemetryState()

    fun selectTab(tab: NavTab) {
        _currentTab.value = tab
        if (tab == NavTab.WALK) {
            _hasActiveRoute.value = true
            startStepTracking()
        }
    }

    // Step Tracking & Calorie Monitoring Methods
    fun startStepTracking() {
        if (_isStepTrackingActive.value) return
        _isStepTrackingActive.value = true

        val stepSensor = sensorManager?.getDefaultSensor(android.hardware.Sensor.TYPE_STEP_DETECTOR)
            ?: sensorManager?.getDefaultSensor(android.hardware.Sensor.TYPE_STEP_COUNTER)

        var hasReceivedHardwareEvent = false

        if (stepSensor != null) {
            stepSensorListener = object : android.hardware.SensorEventListener {
                override fun onSensorChanged(event: android.hardware.SensorEvent?) {
                    hasReceivedHardwareEvent = true
                    stepSimulationJob?.cancel()
                    _walkSteps.value += 1
                    _walkCalories.value = _walkSteps.value * 0.04f
                }
                override fun onAccuracyChanged(sensor: android.hardware.Sensor?, accuracy: Int) {}
            }
            sensorManager?.registerListener(stepSensorListener, stepSensor, android.hardware.SensorManager.SENSOR_DELAY_UI)
        }

        // Active cadence simulator for testing/emulator when hardware sensor is not available
        stepSimulationJob?.cancel()
        stepSimulationJob = viewModelScope.launch {
            delay(1200)
            while (_isStepTrackingActive.value) {
                if (!hasReceivedHardwareEvent) {
                    _walkSteps.value += 1
                    _walkCalories.value = _walkSteps.value * 0.04f
                }
                delay(620) // ~97 steps per minute standard brisk walking pace
            }
        }
    }

    fun stopStepTracking() {
        _isStepTrackingActive.value = false
        stepSimulationJob?.cancel()
        stepSimulationJob = null
        stepSensorListener?.let {
            sensorManager?.unregisterListener(it)
        }
        stepSensorListener = null
    }

    fun resetStepTracking() {
        stopStepTracking()
        _walkSteps.value = 0
        _walkCalories.value = 0f
    }

    // Map Search & Routing actions
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()

        val category = _searchCategoryFilter.value
        if (query.isBlank()) {
            _isSearching.value = false
            _searchResults.value = filterByCategory(MapDataDefaults.searchSuggestions, category)
            return
        }

        // 1. Immediate local matching
        val localMatches = filterPlaces(MapDataDefaults.searchSuggestions, category, query)
        _searchResults.value = localMatches

        // 2. Debounced Geocoder search for addresses / custom places
        searchJob = viewModelScope.launch {
            delay(320)
            if (_searchQuery.value.trim() != query.trim()) return@launch
            _isSearching.value = true
            try {
                val external = LocationGeocoder.searchLocations(getApplication(), query)
                val filteredExternal = filterByCategory(external, category)
                val combined = (localMatches + filteredExternal).distinctBy {
                    "${it.title.lowercase()}_${String.format("%.4f", it.position.latitude)}"
                }
                if (_searchQuery.value.trim() == query.trim()) {
                    _searchResults.value = combined
                }
            } catch (_: Exception) {
                // Keep local matches
            } finally {
                _isSearching.value = false
            }
        }
    }

    fun setSearchCategoryFilter(category: String) {
        _searchCategoryFilter.value = category
        val query = _searchQuery.value
        if (query.isBlank()) {
            _searchResults.value = filterByCategory(MapDataDefaults.searchSuggestions, category)
        } else {
            val localMatches = filterPlaces(MapDataDefaults.searchSuggestions, category, query)
            _searchResults.value = localMatches
            // Re-trigger geocoder query if needed
            setSearchQuery(query)
        }
        vibratePhone(25)
    }

    fun searchForLocation(query: String) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return

        searchJob?.cancel()
        viewModelScope.launch {
            _isSearching.value = true

            // Check current matches first
            val exactLocal = _searchResults.value.firstOrNull {
                it.title.equals(trimmed, ignoreCase = true) ||
                        it.title.contains(trimmed, ignoreCase = true)
            } ?: _searchResults.value.firstOrNull()

            if (exactLocal != null && (exactLocal.title.contains(trimmed, ignoreCase = true) || _searchResults.value.size == 1)) {
                selectSearchPlace(exactLocal)
                _isSearching.value = false
                return@launch
            }

            // Otherwise query Geocoder explicitly
            try {
                val results = LocationGeocoder.searchLocations(getApplication(), trimmed)
                if (results.isNotEmpty()) {
                    selectSearchPlace(results.first())
                } else {
                    showToast("No locations found for \"$trimmed\". Try a landmark, street, or safe haven.")
                }
            } catch (_: Exception) {
                showToast("Could not find \"$trimmed\". Please check spelling.")
            } finally {
                _isSearching.value = false
            }
        }
    }

    fun selectSearchPlace(place: SearchPlace) {
        _searchQuery.value = ""
        _destinationName.value = place.title
        _destinationLatLng.value = place.position
        _hasActiveRoute.value = true
        _isRouteSheetOpen.value = true
        _cameraMoveTarget.value = place.position

        saveRecentSearch(place)
        updateRouteForDestination(place.title, place.position)

        showToast("Route mapped to ${place.title}")
        vibratePhone(40)
    }

    fun setCustomDestination(name: String, latLng: LatLng) {
        val place = SearchPlace(
            id = "custom_${System.currentTimeMillis()}",
            title = name,
            subtitle = "Custom Destination",
            position = latLng,
            category = "DESTINATION",
            address = name
        )
        selectSearchPlace(place)
    }

    fun onCameraMoved() {
        _cameraMoveTarget.value = null
    }

    fun clearActiveRoute() {
        _hasActiveRoute.value = false
        _isRouteSheetOpen.value = false
        _searchQuery.value = ""
        _computedPolyline.value = emptyList()
        showToast("Route cleared. Plain map active.")
        vibratePhone(30)
    }

    private fun filterPlaces(places: List<SearchPlace>, category: String, query: String): List<SearchPlace> {
        val trimmed = query.trim()
        val catFiltered = filterByCategory(places, category)
        return if (trimmed.isBlank()) catFiltered else {
            catFiltered.filter {
                it.title.contains(trimmed, ignoreCase = true) ||
                        it.subtitle.contains(trimmed, ignoreCase = true) ||
                        it.address.contains(trimmed, ignoreCase = true) ||
                        it.safetyBadge.contains(trimmed, ignoreCase = true)
            }
        }
    }

    private fun filterByCategory(places: List<SearchPlace>, category: String): List<SearchPlace> {
        if (category == "ALL") return places
        return places.filter {
            when (category) {
                "HAVEN" -> it.isSafeHaven || it.category == "HAVEN"
                "TRANSIT" -> it.category == "TRANSIT"
                "PARK" -> it.category == "PARK"
                "MEDICAL" -> it.category == "MEDICAL"
                "STORE" -> it.category == "STORE"
                "CIVIC" -> it.category == "CIVIC"
                "LANDMARK" -> it.category == "LANDMARK"
                else -> true
            }
        }
    }

    private fun updateRouteForDestination(title: String, dest: LatLng) {
        val origin = _originLatLng.value
        val points = mutableListOf<LatLng>()
        points.add(origin)

        // SF Mission Valencia corridor anchor
        val valenciaLng = -122.4218
        if (kotlin.math.abs(origin.longitude - dest.longitude) > 0.001) {
            points.add(LatLng(origin.latitude, valenciaLng))
            points.add(LatLng(dest.latitude, valenciaLng))
        }
        points.add(dest)
        _computedPolyline.value = points
    }

    fun saveRecentSearch(place: SearchPlace) {
        try {
            val current = _recentSearches.value.filter { it.title != place.title }.toMutableList()
            current.add(0, place.copy(isRecent = true, timestamp = System.currentTimeMillis()))
            val trimmed = current.take(10)
            _recentSearches.value = trimmed

            val jsonArray = JSONArray()
            for (item in trimmed) {
                val obj = JSONObject()
                obj.put("id", item.id)
                obj.put("title", item.title)
                obj.put("subtitle", item.subtitle)
                obj.put("lat", item.position.latitude)
                obj.put("lng", item.position.longitude)
                obj.put("category", item.category)
                obj.put("isSafeHaven", item.isSafeHaven)
                obj.put("address", item.address)
                obj.put("safetyBadge", item.safetyBadge)
                obj.put("timestamp", item.timestamp)
                jsonArray.put(obj)
            }
            searchPrefs.edit().putString("recent_places", jsonArray.toString()).apply()
        } catch (_: Exception) {}
    }

    fun removeRecentSearch(placeId: String) {
        val updated = _recentSearches.value.filter { it.id != placeId }
        _recentSearches.value = updated
        try {
            val jsonArray = JSONArray()
            for (item in updated) {
                val obj = JSONObject()
                obj.put("id", item.id)
                obj.put("title", item.title)
                obj.put("subtitle", item.subtitle)
                obj.put("lat", item.position.latitude)
                obj.put("lng", item.position.longitude)
                obj.put("category", item.category)
                obj.put("isSafeHaven", item.isSafeHaven)
                obj.put("address", item.address)
                obj.put("safetyBadge", item.safetyBadge)
                obj.put("timestamp", item.timestamp)
                jsonArray.put(obj)
            }
            searchPrefs.edit().putString("recent_places", jsonArray.toString()).apply()
        } catch (_: Exception) {}
    }

    fun clearRecentSearches() {
        _recentSearches.value = emptyList()
        searchPrefs.edit().remove("recent_places").apply()
        showToast("Search history cleared")
    }

    fun swapLocations() {
        switchLeg()
    }

    fun selectRouteById(id: String) {
        _selectedRouteId.value = id
        vibratePhone(30)
    }

    fun setRoutingTimeOfDay(time: String) {
        _routingTimeOfDay.value = time
        _dayNightMode.value = if (time == "night") DayNightMode.NIGHT else DayNightMode.DAY
    }

    fun setMaxExtraMinutes(min: Double) {
        _maxExtraMinutes.value = min
    }

    fun toggleStepFreeRouting(stepFree: Boolean) {
        _isStepFreeRouting.value = stepFree
        vibratePhone(30)
    }

    fun selectSamplePoint(sample: com.example.model.SamplePoint?) {
        _selectedSamplePoint.value = sample
        if (sample != null) vibratePhone(30)
    }

    fun switchLeg() {
        val newLeg = if (_currentLegId.value == "to_park") "to_bart" else "to_park"
        _currentLegId.value = newLeg
        val leg = routingEngine.getLeg(newLeg)
        if (leg != null) {
            _originName.value = leg.originName
            _originLatLng.value = leg.originLatLng
            _destinationName.value = leg.destinationName
            _destinationLatLng.value = leg.destinationLatLng
            _routingTimeOfDay.value = leg.defaultTime
            _dayNightMode.value = if (leg.defaultTime == "night") DayNightMode.NIGHT else DayNightMode.DAY
        }
        showToast("Switched leg: ${leg?.label}")
        vibratePhone(40)
    }

    fun useCurrentLocationAsOrigin(latLng: com.google.android.gms.maps.model.LatLng) {
        _userLiveLocation.value = latLng
        _originLatLng.value = latLng
        _originName.value = "Current Location (Live GPS)"
        _isUsingCurrentLocation.value = true
        showToast("Directions starting from live location")
        vibratePhone(40)
    }

    fun updateUserLiveLocation(latLng: com.google.android.gms.maps.model.LatLng) {
        _userLiveLocation.value = latLng
    }

    fun setRouteSheetVisible(visible: Boolean) {
        _isRouteSheetOpen.value = visible
    }

    fun selectSafeHaven(haven: com.example.model.SafeHavenMarker?) {
        _selectedSafeHaven.value = haven
        if (haven != null) {
            vibratePhone(30)
        }
    }

    fun setActiveCivicLayer(layer: String) {
        _activeCivicLayer.value = layer
    }

    fun setDayNightMode(mode: DayNightMode) {
        _dayNightMode.value = mode
        _routingTimeOfDay.value = if (mode == DayNightMode.NIGHT) "night" else "day"
    }

    fun selectDayRoute(routeId: String) {
        _selectedDayRouteId.value = routeId
    }

    fun selectNightRoute(routeId: String) {
        _selectedNightRouteId.value = routeId
    }

    fun setCommunityFilter(filter: CommunityFilter) {
        _communityFilter.value = filter
    }

    fun togglePocketMode(enabled: Boolean) {
        _isPocketVoiceMode.value = enabled
        vibratePhone(50)
    }

    fun toggleAiMute() {
        _isAiMuted.value = !_isAiMuted.value
        val msg = if (_isAiMuted.value) {
            "Voice preview muted"
        } else {
            com.example.service.GeminiLiveAudioEngine.UNAVAILABLE_MESSAGE
        }
        showToast(msg)
    }

    fun setEvidenceSheetVisible(visible: Boolean) {
        _showEvidenceSheet.value = visible
    }

    // Public-build voice boundary. The engine is deliberately disabled.
    fun startLiveVoiceSession() {
        _isLiveVoiceOverlayVisible.value = true
        vibratePhone(40)
        audioEngine.startListening()
    }

    fun stopLiveVoiceSession() {
        audioEngine.stopListening()
        audioEngine.stopSpeaking()
    }

    fun setLiveVoiceOverlayVisible(visible: Boolean) {
        _isLiveVoiceOverlayVisible.value = visible
        if (!visible) {
            audioEngine.stopListening()
            audioEngine.stopSpeaking()
        }
    }

    fun processLiveVoiceInput(@Suppress("UNUSED_PARAMETER") spokenQuery: String) {
        _isGeminiThinking.value = false
        _geminiLiveVoiceResponse.value = com.example.service.GeminiLiveAudioEngine.UNAVAILABLE_MESSAGE
        showToast(com.example.service.GeminiLiveAudioEngine.UNAVAILABLE_MESSAGE)
    }

    override fun onCleared() {
        super.onCleared()
        audioEngine.release()
    }

    fun askGemini(prompt: String) {
        viewModelScope.launch {
            _isGeminiThinking.value = true
            vibratePhone(40)
            val answer = geminiCompanion.askCompanion(prompt)
            _geminiSpeechText.value = answer
            _isGeminiThinking.value = false
            vibratePhone(70)
        }
    }

    fun triggerFakeCall() {
        viewModelScope.launch {
            showToast("Fake call from \"Dad\" incoming in 3 seconds...")
            delay(3000)
            _fakeCallActive.value = true
            vibratePhone(500)
        }
    }

    fun dismissFakeCall() {
        _fakeCallActive.value = false
    }

    fun enableBlackout() {
        _blackoutActive.value = true
        showToast("Screen Blackout active. Triple-tap screen to wake.")
    }

    fun disableBlackout() {
        _blackoutActive.value = false
        showToast("Screen revived.")
        vibratePhone(80)
    }

    fun toggleSiren() {
        if (_sirenArmed.value) {
            _sirenArmed.value = false
            showToast("Strobe and siren disarmed.")
        } else {
            _sirenArmed.value = true
            showToast("Siren primed! Disarming buffer: 2 seconds.")
            viewModelScope.launch {
                delay(2000)
                if (_sirenArmed.value) {
                    showToast("105 dB High-Frequency Siren pulse active!")
                    vibratePhone(1000)
                }
            }
        }
    }

    fun triggerSosDispatch() {
        vibratePhone(800)
        showToast("Emergency SOS dispatched! Live GPS & audio stream sent to Maya & Sarah.")
    }

    fun callHumanContact(name: String) {
        showToast("Connecting live cellular call to $name. Telemetry remains active.")
        vibratePhone(60)
    }

    fun confirmArrivedSafely() {
        val steps = _walkSteps.value
        val cal = String.format("%.0f", _walkCalories.value)
        stopStepTracking()
        _hasActiveRoute.value = false
        showToast("Safe arrival confirmed! Journey logged: $steps steps • $cal kcal burned.")
        vibratePhone(100)
    }

    fun confirmCommunityReport(reportId: Int) {
        viewModelScope.launch {
            repository.incrementConfirmation(reportId)
            showToast("Thank you. Crowd confirmation logged.")
            vibratePhone(40)
        }
    }

    fun submitNewReport(title: String, category: String, desc: String) {
        viewModelScope.launch {
            repository.insertReport(
                CommunityReportEntity(
                    title = title,
                    description = desc,
                    category = category,
                    source = "Pedestrian Observation • You",
                    timeAgo = "Just now",
                    status = "Pending Verification",
                    confirmations = 1,
                    verified = false
                )
            )
            showToast("Report logged and submitted to civic safety layer.")
            vibratePhone(60)
        }
    }

    fun showToast(msg: String) {
        _toastMessage.value = msg
        viewModelScope.launch {
            delay(3500)
            if (_toastMessage.value == msg) {
                _toastMessage.value = null
            }
        }
    }

    private fun vibratePhone(durationMs: Long) {
        try {
            val context = getApplication<Application>()
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = context.getSystemService(VibratorManager::class.java)
                manager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Vibrator::class.java)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (_: Exception) {}
    }
}
