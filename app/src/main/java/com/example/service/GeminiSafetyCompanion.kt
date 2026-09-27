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

/**
 * Fail-closed public-build companion.
 *
 * It keeps the existing ViewModel contract buildable without embedding a
 * credential, contacting a model, or changing navigation state.
 */
class GeminiSafetyCompanion {
    suspend fun askCompanion(@Suppress("UNUSED_PARAMETER") userQuery: String): String =
        GeminiLiveAudioEngine.UNAVAILABLE_MESSAGE

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
    ): GeminiLiveResponse = GeminiLiveResponse(
        spokenText = GeminiLiveAudioEngine.UNAVAILABLE_MESSAGE,
        actionType = GeminiAction.NONE
    )

    fun resetConversation() = Unit
}
