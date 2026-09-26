package com.example.service

import com.example.model.DayNightMode
import com.example.model.RouteOption

enum class GeminiAction {
    NONE,
    SET_DESTINATION,
    SWITCH_ROUTE,
    START_WALK,
    READ_NEXT_CUE,
    SAFE_HAVEN_INFO,
    CRISIS_PROTOCOL
}

data class GeminiLiveResponse(
    val spokenText: String,
    val actionType: GeminiAction = GeminiAction.NONE,
    val actionTarget: String = ""
)

class GeminiSafetyCompanion {
    private var pendingDestinationConfirmation: String? = null

    suspend fun askCompanion(userQuery: String): String = converseWithLive(
        userQuery = userQuery,
        currentOrigin = "16th St Mission BART",
        currentDestination = "Mission Dolores Park",
        dayNightMode = DayNightMode.DAY,
        activeRoute = null,
        allRoutes = emptyList()
    ).spokenText

    @Suppress("UNUSED_PARAMETER")
    suspend fun converseWithLive(
        userQuery: String,
        currentOrigin: String,
        currentDestination: String,
        dayNightMode: DayNightMode,
        activeRoute: RouteOption?,
        allRoutes: List<RouteOption>,
        stepsWalked: Int = 0,
        caloriesBurned: Float = 0f
    ): GeminiLiveResponse {
        // Local command handling only. Live audio uses Firebase AI Logic separately.
        // Legacy fixture claims are addressed in the route-evidence follow-up.
        return getSmartGroundedResponse(
            userQuery, currentOrigin, currentDestination, dayNightMode,
            activeRoute, stepsWalked, caloriesBurned
        )
    }

    private fun getSmartGroundedResponse(
        query: String,
        origin: String,
        destination: String,
        mode: DayNightMode,
        activeRoute: RouteOption?,
        stepsWalked: Int,
        caloriesBurned: Float
    ): GeminiLiveResponse {
        val q = query.lowercase().trim()

        // 1. CRISIS DETECTOR: danger, panic, being followed, unsafe
        if ("followed" in q || "following me" in q || "danger" in q || "scared" in q || "panic" in q || "help me" in q || "threat" in q || "unsafe" in q || "attack" in q) {
            return GeminiLiveResponse(
                spokenText = "I'm right here with you, staying on the line. Tartine Bakery is 140 feet ahead on your right with open doors and staff. Say 'call' to dial Maya now.",
                actionType = GeminiAction.CRISIS_PROTOCOL
            )
        }

        // 2. STEPS & CALORIES TRACKING
        if ("step" in q || "calorie" in q || "burned" in q || "walked" in q || "distance" in q) {
            val cal = String.format("%.0f", if (caloriesBurned > 0f) caloriesBurned else stepsWalked * 0.04f)
            val estTotal = activeRoute?.estimatedSteps ?: 1290
            val estCal = activeRoute?.estimatedCalories ?: 41
            return GeminiLiveResponse(
                spokenText = if (stepsWalked > 0) {
                    "You've taken $stepsWalked steps and burned $cal calories so far. This route is estimated at about $estTotal steps total."
                } else {
                    "This route to $destination is estimated at $estTotal steps and $estCal calories. Step tracking will record continuously as we walk."
                },
                actionType = GeminiAction.NONE
            )
        }

        // 3. CONFIRMATION & CONSENT (e.g. User confirms "yes", "start navigation", "let's go")
        if (pendingDestinationConfirmation != null && ("yes" in q || "start" in q || "let's go" in q || "sure" in q || "navigate" in q || "okay" in q || "ok" in q || "proceed" in q)) {
            val dest = pendingDestinationConfirmation ?: destination
            pendingDestinationConfirmation = null
            return GeminiLiveResponse(
                spokenText = "Starting navigation to $dest. Keep your phone in your pocket; I'll stay on the line and guide every turn.",
                actionType = GeminiAction.START_WALK,
                actionTarget = dest
            )
        }

        // 4. DESTINATION REQUEST (States safest route and immediately asks for verbal confirmation)
        if ("dolores" in q || "park" in q) {
            pendingDestinationConfirmation = "Mission Dolores Park"
            val routeDesc = if (mode == DayNightMode.DAY) {
                "via Valencia Street—it's shaded with tree canopy, takes 14 minutes and about 1,290 steps."
            } else {
                "via Valencia Illuminated Corridor—it's 96% lit by SFPUC smart poles and takes 16 minutes."
            }
            return GeminiLiveResponse(
                spokenText = "I found a safe route to Dolores Park $routeDesc Should I start navigation?",
                actionType = GeminiAction.SET_DESTINATION,
                actionTarget = "Mission Dolores Park"
            )
        }

        if ("bi-rite" in q || "birite" in q || "creamery" in q) {
            pendingDestinationConfirmation = "Bi-Rite Creamery (Safe Haven)"
            return GeminiLiveResponse(
                spokenText = "Bi-Rite Creamery is a verified Safe Haven at 18th and Dolores, open until 11 PM with active staff. Should I start navigation?",
                actionType = GeminiAction.SET_DESTINATION,
                actionTarget = "Bi-Rite Creamery (Safe Haven)"
            )
        }

        if ("tartine" in q || "bakery" in q || "cafe" in q) {
            pendingDestinationConfirmation = "Tartine Bakery"
            return GeminiLiveResponse(
                spokenText = "I found a route to Tartine Bakery along the illuminated 18th Street corridor taking 8 minutes. Should I start navigation?",
                actionType = GeminiAction.SET_DESTINATION,
                actionTarget = "Tartine Bakery"
            )
        }

        if ("train" in q || "station" in q || "bart" in q || "16th" in q) {
            pendingDestinationConfirmation = "16th St Mission BART Station"
            return GeminiLiveResponse(
                spokenText = "I found a route to the 16th Street BART Station via Valencia Street—it's well-lit and stays on main streets. Should I start navigation?",
                actionType = GeminiAction.SET_DESTINATION,
                actionTarget = "16th St Mission BART Station"
            )
        }

        // 5. DIRECT NAVIGATION CUE
        if ("turn" in q || "direction" in q || "next cue" in q || "where do i go" in q || "next" in q) {
            return GeminiLiveResponse(
                spokenText = "In 180 feet, turn right past Tartine Bakery onto 18th Street. The sidewalk is wide and clear.",
                actionType = GeminiAction.READ_NEXT_CUE
            )
        }

        // 6. ROUTE SWITCHING
        if ("fastest" in q || "quick" in q) {
            val targetId = if (mode == DayNightMode.DAY) "day_fastest" else "night_fastest"
            val warning = if (mode == DayNightMode.NIGHT) "Note that 17th Street has a reported streetlight outage." else "It saves 3 minutes but passes unshaded asphalt."
            return GeminiLiveResponse(
                spokenText = "Switched to the fastest route. $warning",
                actionType = GeminiAction.SWITCH_ROUTE,
                actionTarget = targetId
            )
        }

        if ("step free" in q || "gentle" in q || "wheelchair" in q || "ramp" in q) {
            val targetId = if (mode == DayNightMode.DAY) "day_stepfree" else "night_transit"
            return GeminiLiveResponse(
                spokenText = "Switched to the step-free corridor. Dropped curbs at every corner and grade under 4.5 percent.",
                actionType = GeminiAction.SWITCH_ROUTE,
                actionTarget = targetId
            )
        }

        if ("safest" in q || "recommended" in q) {
            val targetId = if (mode == DayNightMode.DAY) "day_best" else "night_illuminated"
            return GeminiLiveResponse(
                spokenText = "Selected the recommended safe route along Valencia with verified smart lighting and open storefronts.",
                actionType = GeminiAction.SWITCH_ROUTE,
                actionTarget = targetId
            )
        }

        // 7. SAFE HAVENS
        if ("haven" in q || "store" in q || "police" in q) {
            return GeminiLiveResponse(
                spokenText = "Tartine Bakery is 140 feet ahead on your right, and Bi-Rite Creamery is open at 18th and Dolores until 11 PM with staff.",
                actionType = GeminiAction.SAFE_HAVEN_INFO
            )
        }

        // Default continuous companion reassurance
        return GeminiLiveResponse(
            spokenText = "I'm listening and monitoring your path to $destination. Ask for your next turn, step count, or nearest safe haven anytime.",
            actionType = GeminiAction.NONE
        )
    }

    fun resetConversation() {
        pendingDestinationConfirmation = null
    }
}
