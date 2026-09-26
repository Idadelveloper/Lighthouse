package com.example.model

enum class NavTab(val title: String) {
    MAP("Map"),
    WALK("Walk"),
    COMMUNITY("Community"),
    SAFETY("Safety")
}

enum class DayNightMode {
    DAY,
    NIGHT
}

data class RouteOption(
    val id: String,
    val title: String,
    val badge: String,
    val badgeColorType: String, // "RECOMMENDED", "FASTEST", "STEP_FREE", "NIGHT_RECOMMENDED"
    val durationMinutes: Int,
    val distanceMiles: Float,
    val elevationFt: Int,
    val clarityOrLitScore: String, // e.g. "Streetlight assets mapped" or "Grade < 3%"
    val bulletPoints: List<Pair<String, String>>, // (iconName, text)
    val contextNote: String,
    val openHavensCount: Int = 0,
    val isRecommended: Boolean = false,
    val estimatedSteps: Int = (distanceMiles * 2150).toInt(),
    val estimatedCalories: Int = (distanceMiles * 68).toInt()
)

enum class CommunityFilter(val label: String, val icon: String) {
    ALL("All Conditions", "view_agenda"),
    OUTAGES("SF 311 Outages", "lightbulb"),
    TRANSIT("Transit Stops", "elevator"),
    HAVENS("Storefront Candidates", "storefront"),
    COMMUNITY("Community Observations", "group")
}
