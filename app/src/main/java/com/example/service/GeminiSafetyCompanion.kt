package com.example.service

import com.example.BuildConfig
import com.example.model.DayNightMode
import com.example.model.RouteOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

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

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    // Multi-turn conversational memory for sustaining dialogue during the whole walk
    private val conversationHistory = mutableListOf<Pair<String, String>>()
    private var pendingDestinationConfirmation: String? = null

    suspend fun askCompanion(userQuery: String): String {
        return converseWithLive(
            userQuery = userQuery,
            currentOrigin = "16th St Mission BART",
            currentDestination = "Mission Dolores Park",
            dayNightMode = DayNightMode.DAY,
            activeRoute = null,
            allRoutes = emptyList(),
            stepsWalked = 0,
            caloriesBurned = 0f
        ).spokenText
    }

    suspend fun converseWithLive(
        userQuery: String,
        currentOrigin: String,
        currentDestination: String,
        dayNightMode: DayNightMode,
        activeRoute: RouteOption?,
        allRoutes: List<RouteOption>,
        stepsWalked: Int = 0,
        caloriesBurned: Float = 0f
    ): GeminiLiveResponse = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (_: Throwable) {
            ""
        }

        val modeLabel = if (dayNightMode == DayNightMode.DAY) {
            "Day Mode (Tree canopy & solar shade model)"
        } else {
            "Night Mode (SFPUC smart illumination & foot-traffic corridor model)"
        }

        val routesSummary = allRoutes.joinToString("; ") {
            "${it.title} (id: ${it.id}, ${it.durationMinutes} min, ${it.estimatedSteps} steps, ${it.estimatedCalories} kcal, ${it.clarityOrLitScore}, ${it.badge})"
        }

        val historyText = conversationHistory.takeLast(6).joinToString("\n") { (role, msg) ->
            "$role: $msg"
        }

        val systemPrompt = """
            You are "Lighthouse", a calm, clear, direct, and protective audio safety copilot for pedestrians walking in San Francisco.
            Your persona is calm, clear, direct, and protective.
            
            Strict Guidelines:
            1. BREVITY: Keep all spoken responses under 2 sentences unless explicitly asked for details. The user is walking or driving. Never use markdown formatting like asterisks or bullet points.
            2. SAFETY FIRST: Prioritize well-lit, populated, and main-road routes. Always warn about high-risk or dark paths if navigational data shows them (e.g. 17th St alley outage).
            3. CLEAR NAVIGATION: Spoken directions must be turn-by-turn and high-contrast (e.g., "Turn right past Tartine Bakery onto 18th St" rather than "Head north").
            4. CONFIRMATION & CONSENT: When a destination is requested, state the safest route option and immediately ask for verbal confirmation before starting turn-by-turn guidance. (Example: "I found a route via Valencia Street—it's well-lit and stays on main streets. Should I start navigation?")
            5. CRISIS DETECTOR: If the user indicates danger, panic, or being followed, immediately switch to direct safety protocol: speak calmly, keep the line open, provide nearest safe havens (police stations, open 24/7 stores like Bi-Rite or Walgreens), and offer to trigger emergency contacts.
            6. STEP & CALORIE TRACKING: If the user asks about steps, calories, or distance, provide their current walk stats: $stepsWalked steps taken, ${String.format("%.1f", caloriesBurned)} kcal burned, or remaining steps from available routes.
            
            Current Pedestrian Telemetry & Context:
            - Mode: $modeLabel
            - Current Location / Start: $currentOrigin
            - Destination: $currentDestination
            - Pending confirmation: ${pendingDestinationConfirmation ?: "None"}
            - Current Steps Walked: $stepsWalked steps
            - Calories Burned: ${String.format("%.1f", caloriesBurned)} kcal
            - Active Selected Route: ${activeRoute?.title ?: "None"} (${activeRoute?.durationMinutes ?: 14} min, ${activeRoute?.estimatedSteps ?: 1290} steps, ${activeRoute?.estimatedCalories ?: 41} kcal)
            - Available Routes: $routesSummary
            - Known Open Safe Havens nearby:
              1. Bi-Rite Creamery (18th & Dolores, open until 11 PM, staffed, AED on-site)
              2. Tartine Bakery (18th & Guerrero, open until 8 PM, well-lit cafe lobby)
              3. Walgreens 24/7 (2141 Mission St, 24-hour staffed pharmacy)
            - Active Alerts:
              - 17th St & Guerrero has a confirmed SF 311 streetlight outage (darker alleyway).
              - Valencia Street is 96% illuminated by verified SFPUC smart poles and active storefronts.

            Action tags:
            At the very end of your response, attach one of these action tags if applicable:
            - When destination requested and proposing route: [ACTION:SET_DESTINATION:Destination Name]
            - When user confirms navigation ("yes", "start", "let's go"): [ACTION:START_WALK]
            - When user requests fastest or step-free route: [ACTION:SWITCH_ROUTE:route_id]
            - When user asks for next turn: [ACTION:READ_NEXT_CUE]
            - When user asks for safe haven: [ACTION:SAFE_HAVEN_INFO]
            - When crisis / panic detected: [ACTION:CRISIS_PROTOCOL]
        """.trimIndent()

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            val response = getSmartGroundedResponse(userQuery, currentOrigin, currentDestination, dayNightMode, activeRoute, stepsWalked, caloriesBurned)
            recordTurn(userQuery, response.spokenText)
            return@withContext response
        }

        val modelsToTry = listOf("gemini-3.5-flash", "gemini-2.5-flash-native-audio-preview-12-2025")

        for (modelName in modelsToTry) {
            try {
                val jsonBody = JSONObject().apply {
                    val contentsArray = JSONArray().apply {
                        put(JSONObject().apply {
                            put("role", "user")
                            put("parts", JSONArray().apply {
                                put(JSONObject().apply {
                                    val fullPrompt = if (historyText.isNotBlank()) {
                                        "$systemPrompt\n\nRecent conversation:\n$historyText\n\nUser: \"$userQuery\""
                                    } else {
                                        "$systemPrompt\n\nUser: \"$userQuery\""
                                    }
                                    put("text", fullPrompt)
                                })
                            })
                        })
                    }
                    put("contents", contentsArray)
                    put("generationConfig", JSONObject().apply {
                        put("temperature", 0.3)
                        put("maxOutputTokens", 140)
                    })
                }

                val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url("https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey")
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""

                if (response.isSuccessful) {
                    val jsonResponse = JSONObject(responseBody)
                    val candidates = jsonResponse.optJSONArray("candidates")
                    val firstCandidate = candidates?.optJSONObject(0)
                    val content = firstCandidate?.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    val rawText = parts?.optJSONObject(0)?.optString("text")

                    if (!rawText.isNullOrBlank()) {
                        val parsed = parseGeminiOutput(rawText.trim())
                        recordTurn(userQuery, parsed.spokenText)
                        return@withContext parsed
                    }
                }
            } catch (_: Exception) {
                // Fallback to grounded
            }
        }

        val fallback = getSmartGroundedResponse(userQuery, currentOrigin, currentDestination, dayNightMode, activeRoute, stepsWalked, caloriesBurned)
        recordTurn(userQuery, fallback.spokenText)
        fallback
    }

    private fun recordTurn(userQuery: String, aiResponse: String) {
        conversationHistory.add("User" to userQuery)
        conversationHistory.add("Lighthouse" to aiResponse)
        if (conversationHistory.size > 20) {
            conversationHistory.removeAt(0)
            conversationHistory.removeAt(0)
        }
    }

    private fun parseGeminiOutput(rawText: String): GeminiLiveResponse {
        var cleanText = rawText
        var action = GeminiAction.NONE
        var target = ""

        val regex = Regex("\\[ACTION:([A-Z_]+)(?::([^]]+))?\\]")
        val match = regex.find(rawText)

        if (match != null) {
            val actionName = match.groupValues[1]
            target = match.groupValues.getOrNull(2) ?: ""
            action = when (actionName) {
                "SET_DESTINATION" -> {
                    pendingDestinationConfirmation = target
                    GeminiAction.SET_DESTINATION
                }
                "START_WALK" -> {
                    pendingDestinationConfirmation = null
                    GeminiAction.START_WALK
                }
                "SWITCH_ROUTE" -> GeminiAction.SWITCH_ROUTE
                "READ_NEXT_CUE" -> GeminiAction.READ_NEXT_CUE
                "SAFE_HAVEN_INFO" -> GeminiAction.SAFE_HAVEN_INFO
                "CRISIS_PROTOCOL" -> GeminiAction.CRISIS_PROTOCOL
                else -> GeminiAction.NONE
            }
            cleanText = rawText.replace(match.value, "").trim()
        }

        cleanText = cleanText.replace("*", "").replace("#", "").replace("`", "").trim()

        return GeminiLiveResponse(
            spokenText = cleanText,
            actionType = action,
            actionTarget = target
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
        conversationHistory.clear()
        pendingDestinationConfirmation = null
    }
}
