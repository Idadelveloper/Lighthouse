package com.example.model

import com.google.android.gms.maps.model.LatLng

data class ScoreData(
    val sidewalk: Int,
    val streetlights: Int,
    val treeShade: Int,
    val activeFrontage: Int,
    val obstructions: Int,
    val stepsOrSteep: Boolean,
    val confidence: Double,
    val note: String
)

data class SamplePoint(
    val lat: Double,
    val lng: Double,
    val panoId: String,
    val imageDate: String?,
    val score: ScoreData?
) {
    val latLng: LatLng get() = LatLng(lat, lng)
}

data class RouteFeatures(
    val shade: Double,
    val lighting: Double,
    val sidewalk: Double,
    val activity: Double,
    val clearPath: Double
)

data class RouteData(
    val id: String,
    val via: String,
    val durationS: Int,
    val distanceM: Int,
    val path: List<LatLng>,
    val features: RouteFeatures,
    val coverage: Double,
    val confidence: Double,
    val hasSteps: Boolean,
    val nImages: Int,
    val weakest: String,
    val oldestImage: String?,
    val comfort: Map<String, Double>, // "day" -> 0.689, "night" -> 0.748
    val explanation: Map<String, String>, // "day" -> text, "night" -> text
    val samples: List<SamplePoint>
) {
    val durationMinutes: Int get() = (durationS + 30) / 60
    val distanceMiles: Float get() = (distanceM * 0.000621371f * 10).toInt() / 10f
}

data class LegData(
    val id: String,
    val label: String,
    val defaultTime: String,
    val originName: String,
    val originLatLng: LatLng,
    val destinationName: String,
    val destinationLatLng: LatLng,
    val defaultVia: String,
    val routes: List<RouteData>
)

data class RankedRoute(
    val route: RouteData,
    val extraMin: Double,
    val comfort: Double?,
    val value: Double?,
    val isRecommended: Boolean,
    val isFastest: Boolean
)

data class RankingResult(
    val rankedRoutes: List<RankedRoute>,
    val fastestId: String,
    val recommendedId: String,
    val note: String?
)
