package com.example.service

import android.content.Context
import com.example.model.LegData
import com.example.model.RankedRoute
import com.example.model.RankingResult
import com.example.model.RouteData
import com.example.model.SamplePoint
import com.google.android.gms.maps.model.LatLng
import org.json.JSONObject
import kotlin.math.roundToInt

class LighthouseRoutingEngine(private val context: Context) {

    private val legs = mutableMapOf<String, LegData>()
    var isLoaded: Boolean = false
        private set

    init {
        loadData()
    }

    private fun loadData() {
        try {
            val jsonString = context.assets.open("lighthouse_data.json").bufferedReader().use { it.readText() }
            val root = JSONObject(jsonString)
            val legsObj = root.optJSONObject("legs") ?: return

            val legKeys = legsObj.keys()
            while (legKeys.hasNext()) {
                val legKey = legKeys.next()
                val legObj = legsObj.optJSONObject(legKey) ?: continue

                val label = legObj.optString("label", "")
                val defaultTime = legObj.optString("default_time", "day")
                val defaultVia = legObj.optString("default_via", "")

                val origObj = legObj.optJSONObject("origin")
                val originName = origObj?.optString("name", "Origin") ?: "Origin"
                val originLatLng = LatLng(
                    origObj?.optDouble("lat", 37.76506) ?: 37.76506,
                    origObj?.optDouble("lng", -122.41969) ?: -122.41969
                )

                val destObj = legObj.optJSONObject("destination")
                val destName = destObj?.optString("name", "Destination") ?: "Destination"
                val destLatLng = LatLng(
                    destObj?.optDouble("lat", 37.75971) ?: 37.75971,
                    destObj?.optDouble("lng", -122.42706) ?: -122.42706
                )

                val routesArray = legObj.optJSONArray("routes")
                val routesList = mutableListOf<RouteData>()

                if (routesArray != null) {
                    for (i in 0 until routesArray.length()) {
                        val rObj = routesArray.optJSONObject(i) ?: continue
                        val id = rObj.optString("id")
                        val via = rObj.optString("via")
                        val durationS = rObj.optInt("duration_s")
                        val distanceM = rObj.optInt("distance_m")

                        // Parse path
                        val pathArray = rObj.optJSONArray("path")
                        val pathList = mutableListOf<LatLng>()
                        if (pathArray != null) {
                            for (j in 0 until pathArray.length()) {
                                val pt = pathArray.optJSONArray(j)
                                if (pt != null && pt.length() >= 2) {
                                    pathList.add(LatLng(pt.optDouble(0), pt.optDouble(1)))
                                }
                            }
                        }

                        val hasSteps = rObj.optBoolean("has_steps", false)
                        val samples = mutableListOf<SamplePoint>()
                        val samplesArray = rObj.optJSONArray("samples")
                        if (samplesArray != null) {
                            for (j in 0 until samplesArray.length()) {
                                val sample = samplesArray.optJSONObject(j) ?: continue
                                samples.add(
                                    SamplePoint(
                                        lat = sample.optDouble("lat"),
                                        lng = sample.optDouble("lng"),
                                        panoId = sample.optString("pano_id"),
                                        imageDate = sample.optString("image_date").ifBlank { null }
                                    )
                                )
                            }
                        }

                        routesList.add(
                            RouteData(
                                id = id,
                                via = via,
                                durationS = durationS,
                                distanceM = distanceM,
                                path = pathList,
                                hasSteps = hasSteps,
                                samples = samples
                            )
                        )
                    }
                }

                legs[legKey] = LegData(
                    id = legKey,
                    label = label,
                    defaultTime = defaultTime,
                    originName = originName,
                    originLatLng = originLatLng,
                    destinationName = destName,
                    destinationLatLng = destLatLng,
                    defaultVia = defaultVia,
                    routes = routesList
                )
            }
            isLoaded = true
        } catch (_: Exception) {
            isLoaded = false
        }
    }

    fun getLeg(legId: String = "to_park"): LegData? = legs[legId]

    /**
     * Truthful route ranking:
     * Strictly ordered by duration (and distance as tiebreak), filtered by accessibility constraints.
     * Unvalidated comfort scores, shade, lighting, or vision ratings are NEVER used to rank routes.
     */
    fun rank(
        routes: List<RouteData>,
        @Suppress("UNUSED_PARAMETER")
        timeOfDay: String = "day",
        @Suppress("UNUSED_PARAMETER")
        maxExtraMin: Double = 6.0,
        stepFree: Boolean = false
    ): RankingResult = RouteRanker.rank(routes, stepFree)
}

/** Pure production route ordering: duration, distance, then an explicit step constraint. */
object RouteRanker {
    fun rank(routes: List<RouteData>, stepFree: Boolean = false): RankingResult {
        if (routes.isEmpty()) {
            return RankingResult(emptyList(), "", "", "No routes available.")
        }

        // Rank strictly by duration (shortest first) and distance
        val sortedByDuration = routes.sortedWith(
            compareBy<RouteData> { it.durationS }.thenBy { it.distanceM }
        )

        val fastest = sortedByDuration.first()

        val rankedList = sortedByDuration.map { r ->
            val extra = round1((r.durationS - fastest.durationS) / 60.0)
            RankedRoute(
                route = r,
                extraMin = extra,
                isRecommended = false,
                isFastest = r.id == fastest.id
            )
        }

        // Step-free filtering if requested
        val eligible = if (stepFree) {
            rankedList.filter { !it.route.hasSteps }
        } else {
            rankedList
        }

        val primaryRoute = eligible.firstOrNull() ?: rankedList.first()

        val finalized = rankedList.map {
            it.copy(isRecommended = it.route.id == primaryRoute.route.id)
        }

        val note = if (stepFree && eligible.isEmpty()) {
            "No step-free route found; displaying shortest route."
        } else null

        return RankingResult(
            rankedRoutes = finalized,
            fastestId = fastest.id,
            recommendedId = primaryRoute.route.id,
            note = note
        )
    }

    private fun round1(v: Double): Double = (v * 10.0).roundToInt() / 10.0
}
