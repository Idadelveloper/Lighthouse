package com.example.util

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import com.example.model.MapDataDefaults
import com.example.model.SearchPlace
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

object LocationGeocoder {

    // San Francisco Bounding Box
    private const val SF_LOWER_LEFT_LAT = 37.7000
    private const val SF_LOWER_LEFT_LNG = -122.5200
    private const val SF_UPPER_RIGHT_LAT = 37.8200
    private const val SF_UPPER_RIGHT_LNG = -122.3600

    suspend fun searchLocations(context: Context, query: String): List<SearchPlace> = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return@withContext emptyList()

        val results = mutableListOf<SearchPlace>()

        // 1. First, search curated local database of 25+ verified safe havens & landmarks
        val localMatches = MapDataDefaults.searchSuggestions.filter { place ->
            place.title.contains(trimmed, ignoreCase = true) ||
                    place.subtitle.contains(trimmed, ignoreCase = true) ||
                    place.address.contains(trimmed, ignoreCase = true) ||
                    place.category.contains(trimmed, ignoreCase = true)
        }
        results.addAll(localMatches)

        // 2. Query Android Geocoder for real physical addresses and places
        try {
            if (Geocoder.isPresent()) {
                val geocodedAddresses = fetchAddresses(context, trimmed)
                for ((idx, address) in geocodedAddresses.withIndex()) {
                    val lat = address.latitude
                    val lng = address.longitude
                    val pos = LatLng(lat, lng)

                    // Avoid duplicate if very close to any local match
                    val isDuplicate = results.any {
                        kotlin.math.abs(it.position.latitude - lat) < 0.0008 &&
                                kotlin.math.abs(it.position.longitude - lng) < 0.0008
                    }

                    if (!isDuplicate) {
                        val featureName = address.featureName ?: ""
                        val thoroughfare = address.thoroughfare ?: ""
                        val subThoroughfare = address.subThoroughfare ?: ""
                        val locality = address.locality ?: "San Francisco"

                        val title = when {
                            featureName.isNotBlank() && featureName != thoroughfare -> featureName
                            thoroughfare.isNotBlank() -> if (subThoroughfare.isNotBlank()) "$subThoroughfare $thoroughfare" else thoroughfare
                            address.getAddressLine(0) != null -> address.getAddressLine(0).split(",").firstOrNull() ?: trimmed
                            else -> trimmed
                        }

                        val fullAddress = address.getAddressLine(0) ?: "$title, $locality"
                        val subtitle = if (fullAddress.isNotBlank()) fullAddress else "$locality • Geocoded Location"

                        results.add(
                            SearchPlace(
                                id = "geocoded_${System.currentTimeMillis()}_$idx",
                                title = title,
                                subtitle = subtitle,
                                position = pos,
                                category = "GEOCODED",
                                address = fullAddress,
                                safetyBadge = "Mapped Location"
                            )
                        )
                    }
                }
            }
        } catch (_: Exception) {
            // Geocoder failure (e.g. offline/network timeout) - continue with curated results
        }

        results
    }

    private suspend fun fetchAddresses(context: Context, query: String): List<Address> = withContext(Dispatchers.IO) {
        val geocoder = Geocoder(context, Locale.getDefault())
        val searchWithArea = if (!query.contains("San Francisco", ignoreCase = true) &&
            !query.contains("SF", ignoreCase = true) &&
            !query.contains("CA", ignoreCase = true)
        ) {
            "$query, San Francisco, CA"
        } else {
            query
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            suspendCancellableCoroutine { cont ->
                try {
                    geocoder.getFromLocationName(
                        searchWithArea,
                        5,
                        SF_LOWER_LEFT_LAT,
                        SF_LOWER_LEFT_LNG,
                        SF_UPPER_RIGHT_LAT,
                        SF_UPPER_RIGHT_LNG,
                        object : Geocoder.GeocodeListener {
                            override fun onGeocode(addresses: MutableList<Address>) {
                                if (cont.isActive) cont.resume(addresses)
                            }
                            override fun onError(errorMessage: String?) {
                                if (cont.isActive) cont.resume(emptyList())
                            }
                        }
                    )
                } catch (_: Exception) {
                    if (cont.isActive) cont.resume(emptyList())
                }
            }
        } else {
            try {
                @Suppress("DEPRECATION")
                val list = geocoder.getFromLocationName(
                    searchWithArea,
                    5,
                    SF_LOWER_LEFT_LAT,
                    SF_LOWER_LEFT_LNG,
                    SF_UPPER_RIGHT_LAT,
                    SF_UPPER_RIGHT_LNG
                )
                list ?: emptyList()
            } catch (_: Exception) {
                emptyList()
            }
        }
    }
}
