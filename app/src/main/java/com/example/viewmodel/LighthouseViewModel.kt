package com.example.viewmodel

import android.app.Application
import android.content.Intent
import android.net.Uri
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
import com.example.model.NavTab
import com.example.model.RouteOption
import com.example.service.GeminiSafetyCompanion
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LighthouseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: LighthouseRepository
    private val geminiCompanion = GeminiSafetyCompanion()
    val audioEngine = com.example.service.GeminiLiveAudioEngine(application, viewModelScope)

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

    // Destination selected flag (Home screen stays plain map until user selects destination)
    private val _isDestinationSelected = MutableStateFlow(false)
    val isDestinationSelected: StateFlow<Boolean> = _isDestinationSelected.asStateFlow()

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
            title = "Valencia Corridor",
            badge = "Route option · Suggested for review",
            badgeColorType = "RECOMMENDED",
            durationMinutes = 14,
            distanceMiles = 0.6f,
            elevationFt = 32,
            clarityOrLitScore = "Conditions unknown",
            bulletPoints = listOf(
                "park" to "Shade data unavailable · Compare sun exposure when feed connected",
                "accessible" to "Curb and grade conditions are not live verified",
                "storefront" to "Storefront availability is unknown"
            ),
            contextNote = "Shade data unavailable. Open the SF 311 source to check current sidewalk reports.",
            openHavensCount = 0,
            isRecommended = true
        ),
        RouteOption(
            id = "day_fastest",
            title = "Direct Route Option",
            badge = "Route option · Direct path",
            badgeColorType = "FASTEST",
            durationMinutes = 11,
            distanceMiles = 0.5f,
            elevationFt = 48,
            clarityOrLitScore = "Direct Path",
            bulletPoints = listOf(
                "wb_sunny" to "Shade data unavailable",
                "warning" to "Current sidewalk conditions have not been fetched"
            ),
            contextNote = "Open the SF 311 source to check current sidewalk reports.",
            openHavensCount = 0,
            isRecommended = false
        ),
        RouteOption(
            id = "day_stepfree",
            title = "Accessibility Preference",
            badge = "Route option · Verify accessibility",
            badgeColorType = "STEP_FREE",
            durationMinutes = 16,
            distanceMiles = 0.7f,
            elevationFt = 20,
            clarityOrLitScore = "Curb & grade unknown",
            bulletPoints = listOf(
                "check_circle" to "Accessibility preference applied",
                "shelves" to "Curb, elevator, and grade conditions are not live verified"
            ),
            contextNote = "Verify current curb and elevator conditions before travel.",
            openHavensCount = 0,
            isRecommended = false
        )
    )

    // Night Routes
    val nightRoutes = listOf(
        RouteOption(
            id = "night_illuminated",
            title = "Valencia Corridor",
            badge = "Route option · Suggested for review",
            badgeColorType = "NIGHT_RECOMMENDED",
            durationMinutes = 16,
            distanceMiles = 0.7f,
            elevationFt = 32,
            clarityOrLitScore = "Streetlight assets mapped",
            bulletPoints = listOf(
                "flash_on" to "Streetlight assets mapped · Working status unknown",
                "warning" to "Open the SF 311 source to check current reports",
                "storefront" to "Storefront availability is unknown"
            ),
            contextNote = "Streetlight assets mapped by SFPUC. Working status unknown. Current SF 311 reports have not been fetched.",
            openHavensCount = 0,
            isRecommended = true
        ),
        RouteOption(
            id = "night_fastest",
            title = "17th St Cut",
            badge = "Route option · Direct path",
            badgeColorType = "FASTEST",
            durationMinutes = 12,
            distanceMiles = 0.5f,
            elevationFt = 48,
            clarityOrLitScore = "Direct Path",
            bulletPoints = listOf(
                "warning" to "Current SF 311 reports have not been fetched",
                "visibility_off" to "Working streetlight status is unknown"
            ),
            contextNote = "Direct route option. Open the SF 311 source to check current streetlight reports.",
            openHavensCount = 0,
            isRecommended = false
        ),
        RouteOption(
            id = "night_transit",
            title = "Transit Connection via Mission",
            badge = "Route option · Transit Link",
            badgeColorType = "STEP_FREE",
            durationMinutes = 14,
            distanceMiles = 0.6f,
            elevationFt = 15,
            clarityOrLitScore = "Transit Connection",
            bulletPoints = listOf(
                "directions_bus" to "24-Divisadero / 14-Mission transfer connection",
                "security" to "Accessibility conditions are not live verified",
                "lightbulb" to "Streetlight assets mapped along transit stops"
            ),
            contextNote = "Multi-modal option. Check current transit and elevator status before travel.",
            openHavensCount = 0,
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
        "Calm route guidance, bilingual support, and spoken turn cues."
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

    fun selectTab(tab: NavTab) {
        _currentTab.value = tab
        if (tab == NavTab.WALK) {
            _isDestinationSelected.value = true
        }
    }

    // Map Search & Routing actions
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectSearchPlace(place: com.example.model.SearchPlace) {
        _searchQuery.value = ""
        _destinationName.value = place.title
        _destinationLatLng.value = place.position
        _isDestinationSelected.value = true
        _isRouteSheetOpen.value = true
        showToast("Route mapped to ${place.title}")
        vibratePhone(40)
    }

    fun setCustomDestination(name: String, latLng: com.google.android.gms.maps.model.LatLng) {
        _destinationName.value = name
        _destinationLatLng.value = latLng
        _isDestinationSelected.value = true
        _isRouteSheetOpen.value = true
        showToast("Directions to $name")
        vibratePhone(40)
    }

    fun clearDestination() {
        _isDestinationSelected.value = false
        _isRouteSheetOpen.value = false
        _selectedSamplePoint.value = null
        _searchQuery.value = ""
        showToast("Map cleared")
        vibratePhone(20)
    }

    fun swapLocations() {
        switchLeg()
    }

    fun selectRouteById(id: String) {
        _selectedRouteId.value = id
        _isDestinationSelected.value = true
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
        val msg = if (_isAiMuted.value) "AI Voice companion muted" else "AI Voice companion listening"
        showToast(msg)
    }

    fun setEvidenceSheetVisible(visible: Boolean) {
        _showEvidenceSheet.value = visible
    }

    // Gemini 3.8 Live Voice Session Actions
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
            showToast("Local alarm disarmed.")
        } else {
            _sirenArmed.value = true
            showToast("Alarm primed. 2-second safety delay active.")
            viewModelScope.launch {
                delay(2000)
                if (_sirenArmed.value) {
                    showToast("Audible alert tone active on speaker.")
                    vibratePhone(1000)
                }
            }
        }
    }

    fun triggerSosDispatch() {
        vibratePhone(800)
        showToast("Opening device dialer for 911. Please confirm the call.")
        try {
            val context = getApplication<Application>()
            val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:911")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(dialIntent)
        } catch (_: Exception) {
            showToast("Please open your phone dialer and call 911 directly.")
        }
    }

    fun callPrimaryContact() {
        val contact = safetyContacts.value.firstOrNull()
        val phoneNumber = contact?.phoneNumber.orEmpty()
        val isPlaceholder = phoneNumber.contains("555-01")
        if (contact == null || phoneNumber.isBlank() || isPlaceholder) {
            showToast("Add a real trusted contact before using this shortcut.")
            return
        }

        showToast("Opening the device dialer for your trusted contact.")
        vibratePhone(60)
        try {
            val context = getApplication<Application>()
            val dialIntent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phoneNumber, null)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(dialIntent)
        } catch (_: Exception) {
            showToast("Open the phone app and call your trusted contact directly.")
        }
    }

    fun confirmArrivedSafely() {
        showToast("Walk completed. Send a text to your contacts to confirm arrival.")
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
            showToast("Saved as an in-app community observation pending moderation and sync.")
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
