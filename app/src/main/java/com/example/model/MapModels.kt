package com.example.model

import com.google.android.gms.maps.model.LatLng

data class SafeHavenMarker(
    val id: String,
    val name: String,
    val address: String,
    val position: LatLng,
    val hours: String = "Hours unverified by live feed",
    val havenType: String = "STOREFRONT",
    val staffActive: Boolean = false,
    val phone: String = "",
    val description: String = "Nearby support place candidate. Operating hours and staffing unverified by live feed."
)

data class SearchPlace(
    val id: String,
    val title: String,
    val subtitle: String,
    val position: LatLng,
    val category: String, // "DESTINATION", "ORIGIN", "HAVEN", "TRANSIT"
    val isSafeHaven: Boolean = false
)

object MapDataDefaults {
    val BART_16TH = LatLng(37.765062, -122.419694)
    val DOLORES_PARK = LatLng(37.759711, -122.427063)
    val BI_RITE = LatLng(37.761596, -122.425712)
    val TARTINE = LatLng(37.761425, -122.424108)
    val WALGREENS_24 = LatLng(37.764400, -122.419800)
    val OUTAGE_17TH = LatLng(37.763200, -122.423800)

    val safeHavens = listOf(
        SafeHavenMarker(
            id = "haven_birite",
            name = "Bi-Rite Creamery & Grocery",
            address = "3692 18th St, San Francisco",
            position = BI_RITE,
            hours = "Hours unverified by live feed",
            havenType = "GROCERY",
            staffActive = false,
            phone = "",
            description = "Local retail storefront. Operating hours and staffing unverified by live feed."
        ),
        SafeHavenMarker(
            id = "haven_tartine",
            name = "Tartine Bakery & Cafe",
            address = "600 Guerrero St, San Francisco",
            position = TARTINE,
            hours = "Hours unverified by live feed",
            havenType = "BAKERY",
            staffActive = false,
            phone = "",
            description = "Local neighborhood bakery. Operating hours unverified by live feed."
        ),
        SafeHavenMarker(
            id = "haven_walgreens",
            name = "Walgreens Pharmacy",
            address = "2141 Mission St, San Francisco",
            position = WALGREENS_24,
            hours = "Hours unverified by live feed",
            havenType = "PHARMACY",
            staffActive = false,
            phone = "",
            description = "Retail pharmacy location. Check posted door hours for current access."
        )
    )

    val searchSuggestions = listOf(
        SearchPlace(
            id = "s_dolores",
            title = "Mission Dolores Park",
            subtitle = "Dolores St & 19th St • Destination park",
            position = DOLORES_PARK,
            category = "DESTINATION"
        ),
        SearchPlace(
            id = "s_birite",
            title = "Bi-Rite Creamery",
            subtitle = "3692 18th St • Storefront candidate (hours unverified)",
            position = BI_RITE,
            category = "HAVEN",
            isSafeHaven = true
        ),
        SearchPlace(
            id = "s_tartine",
            title = "Tartine Bakery",
            subtitle = "600 Guerrero St • Storefront candidate (hours unverified)",
            position = TARTINE,
            category = "HAVEN",
            isSafeHaven = true
        ),
        SearchPlace(
            id = "s_bart",
            title = "16th St Mission BART Station",
            subtitle = "16th & Mission St • Transit Hub",
            position = BART_16TH,
            category = "TRANSIT"
        ),
        SearchPlace(
            id = "s_valencia",
            title = "Valencia Street Pedestrian Corridor",
            subtitle = "Between 16th & 19th • Streetlight assets mapped",
            position = LatLng(37.763000, -122.421600),
            category = "DESTINATION"
        )
    )

    // Route coordinates for the different options
    val dayBestPolyline = listOf(
        BART_16TH,
        LatLng(37.764950, -122.420800),
        LatLng(37.764900, -122.421900), // Valencia & 16th
        LatLng(37.763350, -122.421750), // Valencia & 17th
        LatLng(37.761800, -122.421600), // Valencia & 18th
        LatLng(37.761650, -122.423900), // Tartine (18th & Guerrero)
        LatLng(37.761500, -122.425800), // Bi-Rite (18th & Dolores)
        DOLORES_PARK
    )

    val dayFastestPolyline = listOf(
        BART_16TH,
        LatLng(37.765000, -122.421900),
        LatLng(37.764800, -122.423800), // 16th & Guerrero
        OUTAGE_17TH,                    // 17th & Guerrero
        LatLng(37.761650, -122.423900), // 18th & Guerrero
        DOLORES_PARK
    )

    val dayStepFreePolyline = listOf(
        BART_16TH,
        LatLng(37.763400, -122.419500), // Mission & 17th
        LatLng(37.763350, -122.421750), // Valencia & 17th
        LatLng(37.760000, -122.421400), // Valencia & 19th
        LatLng(37.759800, -122.425900), // 19th & Dolores
        DOLORES_PARK
    )

    val nightIlluminatedPolyline = listOf(
        BART_16TH,
        LatLng(37.764900, -122.421900), // Valencia & 16th
        LatLng(37.763350, -122.421750), // Valencia & 17th
        LatLng(37.761800, -122.421600), // Valencia & 18th
        LatLng(37.761650, -122.423900),
        LatLng(37.761500, -122.425800),
        DOLORES_PARK
    )

    val nightFastestPolyline = listOf(
        BART_16TH,
        LatLng(37.764800, -122.423800),
        OUTAGE_17TH,
        LatLng(37.761650, -122.423900),
        DOLORES_PARK
    )

    val nightTransitPolyline = listOf(
        BART_16TH,
        LatLng(37.763400, -122.419500),
        LatLng(37.760000, -122.421400),
        DOLORES_PARK
    )
}
