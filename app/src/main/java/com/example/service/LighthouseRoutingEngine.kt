package com.example.service

import android.content.Context
import com.example.model.LegData
import com.example.model.RankedRoute
import com.example.model.RankingResult
import com.example.model.RouteData
import com.example.model.RouteFeatures
import com.example.model.SamplePoint
import com.example.model.ScoreData
import com.google.android.gms.maps.model.LatLng
import org.json.JSONObject
import java.io.InputStream
import kotlin.math.roundToInt

class LighthouseRoutingEngine(private val context: Context) {

    private val timeCostPerMin = 0.01
    val defaultMaxExtraMin = 6.0

    private val legs = mutableMapOf<String, LegData>()
    var isLoaded = false
        private set

    init {
        loadBundle()
    }

    private fun loadBundle() {
        try {
            val jsonString = context.assets.open("lighthouse_data.json").bufferedReader().use { it.readText() }
            val root = JSONObject(jsonString)
            val legsObj = root.optJSONObject("legs") ?: return

            val legKeys = listOf("to_park", "to_bart")
            for (legKey in legKeys) {
                val legObj = legsObj.optJSONObject(legKey) ?: continue
                val label = legObj.optString("label", legKey)
                val defaultTime = legObj.optString("default_time", "day")
                val defaultVia = legObj.optString("default_via", "")

                val originObj = legObj.optJSONObject("origin")
                val originName = originObj?.optString("name") ?: "16th St Mission BART"
                val originLatLng = LatLng(
                    originObj?.optDouble("lat", 37.76506) ?: 37.76506,
                    originObj?.optDouble("lng", -122.41969) ?: -122.41969
                )

                val destObj = legObj.optJSONObject("destination")
                val destName = destObj?.optString("name") ?: "Dolores Park"
                val destLatLng = LatLng(
                    destObj?.optDouble("lat", 37.76156) ?: 37.76156,
                    destObj?.optDouble("lng", -122.42582) ?: -122.42582
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

                        // Parse features
                        val fObj = rObj.optJSONObject("features")
                        val features = RouteFeatures(
                            shade = fObj?.optDouble("shade", 0.0) ?: 0.0,
                            lighting = fObj?.optDouble("lighting", 0.0) ?: 0.0,
                            sidewalk = fObj?.optDouble("sidewalk", 1.0) ?: 1.0,
                            activity = fObj?.optDouble("activity", 0.0) ?: 0.0,
                            clearPath = fObj?.optDouble("clear_path", 0.8) ?: 0.8
                        )

                        val coverage = rObj.optDouble("coverage", 0.9)
                        val confidence = rObj.optDouble("confidence", 0.9)
                        val hasSteps = rObj.optBoolean("has_steps", false)
                        val nImages = rObj.optInt("n_images", 25)
                        val weakest = rObj.optString("weakest", "shade")
                        val oldestImage = rObj.optString("oldest_image", "2024-12")

                        // Comfort map
                        val comfortObj = rObj.optJSONObject("comfort")
                        val comfortMap = mutableMapOf<String, Double>()
                        if (comfortObj != null) {
                            comfortMap["day"] = comfortObj.optDouble("day", 0.68)
                            comfortMap["night"] = comfortObj.optDouble("night", 0.70)
                        }

                        // Explanation map
                        val expObj = rObj.optJSONObject("explanation")
                        val expMap = mutableMapOf<String, String>()
                        if (expObj != null) {
                            expMap["day"] = expObj.optString("day", "")
                            expMap["night"] = expObj.optString("night", "")
                        }

                        // Samples
                        val samplesArray = rObj.optJSONArray("samples")
                        val samplesList = mutableListOf<SamplePoint>()
                        if (samplesArray != null) {
                            for (k in 0 until samplesArray.length()) {
                                val sObj = samplesArray.optJSONObject(k) ?: continue
                                val lat = sObj.optDouble("lat")
                                val lng = sObj.optDouble("lng")
                                val panoId = sObj.optString("pano_id")
                                val imageDate = sObj.optString("image_date")

                                val scObj = sObj.optJSONObject("score")
                                val score = if (scObj != null) {
                                    ScoreData(
                                        sidewalk = scObj.optInt("sidewalk", 2),
                                        streetlights = scObj.optInt("streetlights", 1),
                                        treeShade = scObj.optInt("tree_shade", 1),
                                        activeFrontage = scObj.optInt("active_frontage", 1),
                                        obstructions = scObj.optInt("obstructions", 0),
                                        stepsOrSteep = scObj.optBoolean("steps_or_steep", false),
                                        confidence = scObj.optDouble("confidence", 0.9),
                                        note = scObj.optString("note", "")
                                    )
                                } else null

                                samplesList.add(
                                    SamplePoint(
                                        lat = lat,
                                        lng = lng,
                                        panoId = panoId,
                                        imageDate = imageDate,
                                        score = score
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
                                features = features,
                                coverage = coverage,
                                confidence = confidence,
                                hasSteps = hasSteps,
                                nImages = nImages,
                                weakest = weakest,
                                oldestImage = oldestImage,
                                comfort = comfortMap,
                                explanation = expMap,
                                samples = samplesList
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
     * Exact Kotlin port of Python scoring.rank()
     */
    fun rank(
        routes: List<RouteData>,
        timeOfDay: String = "day",
        maxExtraMin: Double = defaultMaxExtraMin,
        stepFree: Boolean = false
    ): RankingResult {
        if (routes.isEmpty()) {
            return RankingResult(emptyList(), "", "", "No routes available.")
        }

        val fastest = routes.minByOrNull { it.durationS } ?: routes.first()

        val rankedList = routes.map { r ->
            val extra = round1((r.durationS - fastest.durationS) / 60.0)
            val comfort = r.comfort[timeOfDay]
            val value = comfort?.let { round3(it - timeCostPerMin * extra) }
            RankedRoute(
                route = r,
                extraMin = extra,
                comfort = comfort,
                value = value,
                isRecommended = false,
                isFastest = r.id == fastest.id
            )
        }

        val eligible = rankedList.filter {
            it.comfort != null && it.extraMin <= maxExtraMin && !(stepFree && it.route.hasSteps)
        }

        val recommended = eligible.maxWithOrNull(
            compareBy<RankedRoute> { it.value ?: 0.0 }.thenBy { -it.route.durationS }
        ) ?: rankedList.first { it.route.id == fastest.id }

        val finalized = rankedList.map {
            it.copy(isRecommended = it.route.id == recommended.route.id)
        }

        val note = if (eligible.isEmpty()) {
            "No route met your settings, so this shows the fastest one."
        } else null

        return RankingResult(
            rankedRoutes = finalized,
            fastestId = fastest.id,
            recommendedId = recommended.route.id,
            note = note
        )
    }

    private fun round1(v: Double): Double = (v * 10.0).roundToInt() / 10.0
    private fun round3(v: Double): Double = (v * 1000.0).roundToInt() / 1000.0
}
