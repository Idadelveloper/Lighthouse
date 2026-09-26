package com.example.model

import com.google.android.gms.maps.model.LatLng

data class SamplePoint(
    val lat: Double,
    val lng: Double,
    val panoId: String,
    val imageDate: String?
) {
    val latLng: LatLng get() = LatLng(lat, lng)
}

data class RouteData(
    val id: String,
    val via: String,
    val durationS: Int,
    val distanceM: Int,
    val path: List<LatLng>,
    val hasSteps: Boolean,
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
    val isRecommended: Boolean,
    val isFastest: Boolean
)

data class RankingResult(
    val rankedRoutes: List<RankedRoute>,
    val fastestId: String,
    val recommendedId: String,
    val note: String?
)
