package com.example.model

import com.google.android.gms.maps.model.LatLng

data class SafeHavenMarker(
    val id: String,
    val name: String,
    val address: String,
    val position: LatLng,
    val hours: String,
    val havenType: String, // "GROCERY", "BAKERY", "PHARMACY", "TRANSIT"
    val staffActive: Boolean = true,
    val phone: String = "(415) 555-0192",
    val description: String = "Staffed safe haven with active indoor lighting and public emergency access."
)

data class SearchPlace(
    val id: String,
    val title: String,
    val subtitle: String,
    val position: LatLng,
    val category: String, // "DESTINATION", "ORIGIN", "HAVEN", "TRANSIT", "PARK", "MEDICAL", "STORE", "CIVIC", "LANDMARK", "GEOCODED"
    val isSafeHaven: Boolean = false,
    val address: String = "",
    val safetyBadge: String = "",
    val isRecent: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
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
            hours = "Open until 11:00 PM",
            havenType = "GROCERY",
            staffActive = true,
            description = "Well-lit entrance, verified AED on-site, warm community presence."
        ),
        SafeHavenMarker(
            id = "haven_tartine",
            name = "Tartine Bakery & Cafe",
            address = "600 Guerrero St, San Francisco",
            position = TARTINE,
            hours = "Open until 8:00 PM",
            havenType = "BAKERY",
            staffActive = true,
            description = "Staffed lobby with bright exterior lighting and high foot traffic."
        ),
        SafeHavenMarker(
            id = "haven_walgreens",
            name = "Walgreens 24/7 Pharmacy",
            address = "2141 Mission St, San Francisco",
            position = WALGREENS_24,
            hours = "Open 24 Hours",
            havenType = "PHARMACY",
            staffActive = true,
            description = "24-hour staffed pharmacy, exterior security cameras, municipal transit hub."
        )
    )

    val searchSuggestions = listOf(
        SearchPlace(
            id = "s_dolores",
            title = "Mission Dolores Park",
            subtitle = "Dolores St & 19th St • Green space • Public Haven",
            position = DOLORES_PARK,
            category = "PARK",
            address = "Dolores St & 19th St",
            safetyBadge = "Public Haven • Active Park Patrol"
        ),
        SearchPlace(
            id = "s_birite",
            title = "Bi-Rite Creamery (Safe Haven)",
            subtitle = "3692 18th St • Staffed Haven • Open til 11 PM",
            position = BI_RITE,
            category = "HAVEN",
            isSafeHaven = true,
            address = "3692 18th St",
            safetyBadge = "Safe Haven • Staffed til 11 PM"
        ),
        SearchPlace(
            id = "s_tartine",
            title = "Tartine Bakery",
            subtitle = "600 Guerrero St • Staffed Cafe • High Vis Corridor",
            position = TARTINE,
            category = "HAVEN",
            isSafeHaven = true,
            address = "600 Guerrero St",
            safetyBadge = "Safe Haven • High Foot Traffic"
        ),
        SearchPlace(
            id = "s_bart_16",
            title = "16th St Mission BART Station",
            subtitle = "16th & Mission St • Transit Hub • Concourse Escort",
            position = BART_16TH,
            category = "TRANSIT",
            address = "2000 Mission St",
            safetyBadge = "Transit Hub • Staffed Ambassadors"
        ),
        SearchPlace(
            id = "s_walgreens_24",
            title = "Walgreens 24/7 Pharmacy",
            subtitle = "2141 Mission St • 24 Hours • Emergency Safe Spot",
            position = WALGREENS_24,
            category = "MEDICAL",
            isSafeHaven = true,
            address = "2141 Mission St",
            safetyBadge = "24/7 Monitored • Security On-Site"
        ),
        SearchPlace(
            id = "s_valencia",
            title = "Valencia Pedestrian Corridor",
            subtitle = "Between 16th & 24th • SFPUC Smart Poles • 96% Lit",
            position = LatLng(37.763000, -122.421600),
            category = "DESTINATION",
            address = "Valencia St (16th-24th)",
            safetyBadge = "96% Illuminated • High Activity"
        ),
        SearchPlace(
            id = "s_safeway_market",
            title = "Safeway 24-Hour Supermarket",
            subtitle = "2020 Market St • Open 24/7 • Monitored Corridor",
            position = LatLng(37.769200, -122.427800),
            category = "STORE",
            isSafeHaven = true,
            address = "2020 Market St",
            safetyBadge = "Open 24/7 • Bright Parking & Lobby"
        ),
        SearchPlace(
            id = "s_bart_24",
            title = "24th St Mission BART Station",
            subtitle = "24th & Mission St • Transit Hub • Plaza Security",
            position = LatLng(37.752248, -122.418452),
            category = "TRANSIT",
            address = "2800 Mission St",
            safetyBadge = "Transit Hub • Monitored Plaza"
        ),
        SearchPlace(
            id = "s_womens_building",
            title = "The Women's Building",
            subtitle = "3543 18th St • Community Center • Day Sanctuary",
            position = LatLng(37.761700, -122.422300),
            category = "CIVIC",
            isSafeHaven = true,
            address = "3543 18th St",
            safetyBadge = "Safe Haven • Community Staffed"
        ),
        SearchPlace(
            id = "s_police_mission",
            title = "SFPD Mission Police Station",
            subtitle = "630 Valencia St • 24/7 Public Safety Station",
            position = LatLng(37.762880, -122.421940),
            category = "CIVIC",
            isSafeHaven = true,
            address = "630 Valencia St",
            safetyBadge = "24/7 Law Enforcement Haven"
        ),
        SearchPlace(
            id = "s_fire_station7",
            title = "SFFD Fire Station 7",
            subtitle = "2300 Folsom St • 24/7 First Responder Haven",
            position = LatLng(37.759900, -122.415200),
            category = "CIVIC",
            isSafeHaven = true,
            address = "2300 Folsom St",
            safetyBadge = "24/7 Emergency Sanctuary"
        ),
        SearchPlace(
            id = "s_castro_theatre",
            title = "Castro Theatre",
            subtitle = "429 Castro St • Historic Corridor • High Foot Traffic",
            position = LatLng(37.762000, -122.434700),
            category = "LANDMARK",
            address = "429 Castro St",
            safetyBadge = "Active Corridor • Well-lit Sidewalks"
        ),
        SearchPlace(
            id = "s_four_barrel",
            title = "Four Barrel Coffee",
            subtitle = "375 Valencia St • Active Frontage • Cafe Haven",
            position = LatLng(37.767100, -122.422100),
            category = "STORE",
            address = "375 Valencia St",
            safetyBadge = "Open Terrace • Active Eyes on Street"
        ),
        SearchPlace(
            id = "s_ritual_coffee",
            title = "Ritual Coffee Roasters",
            subtitle = "1026 Valencia St • Bustling Pedestrian Hub",
            position = LatLng(37.756400, -122.421100),
            category = "STORE",
            address = "1026 Valencia St",
            safetyBadge = "Active Corridor • Outdoor Seating"
        ),
        SearchPlace(
            id = "s_clarion_alley",
            title = "Clarion Alley Mural Walk",
            subtitle = "Between Mission & Valencia • Public Art Corridor",
            position = LatLng(37.763100, -122.420600),
            category = "LANDMARK",
            address = "Clarion Alley (17th & 18th)",
            safetyBadge = "Pedestrian Art Walk • Daylight Recommended"
        ),
        SearchPlace(
            id = "s_library_mission",
            title = "SF Public Library - Mission Branch",
            subtitle = "300 Bartlett St • Free Wi-Fi • Staffed Civic Haven",
            position = LatLng(37.752800, -122.419900),
            category = "CIVIC",
            isSafeHaven = true,
            address = "300 Bartlett St",
            safetyBadge = "Public Civic Haven • Staff on Duty"
        ),
        SearchPlace(
            id = "s_alamo_square",
            title = "Alamo Square Park",
            subtitle = "Steiner & Hayes • Iconic Hilltop Green Space",
            position = LatLng(37.776200, -122.434600),
            category = "PARK",
            address = "Steiner St & Hayes St",
            safetyBadge = "Daytime Recommended • Open Vistas"
        ),
        SearchPlace(
            id = "s_bernal_heights",
            title = "Bernal Heights Summit",
            subtitle = "Bernal Heights Blvd • 360° Trail & Dog Park",
            position = LatLng(37.743100, -122.415000),
            category = "PARK",
            address = "Bernal Heights Blvd",
            safetyBadge = "Recreational Trail • Daylight Hours"
        ),
        SearchPlace(
            id = "s_sf_general",
            title = "Zuckerberg SF General Hospital",
            subtitle = "1001 Potrero Ave • 24/7 Level 1 Trauma Care",
            position = LatLng(37.755800, -122.405700),
            category = "MEDICAL",
            isSafeHaven = true,
            address = "1001 Potrero Ave",
            safetyBadge = "24/7 Staffed Emergency Entry"
        ),
        SearchPlace(
            id = "s_muni_metro_church",
            title = "Church & Market Muni Metro",
            subtitle = "Market & Church St • J-Church & F-Line Connection",
            position = LatLng(37.767500, -122.429000),
            category = "TRANSIT",
            address = "Church St & Market St",
            safetyBadge = "Muni Transit Connection • Well-lit"
        ),
        SearchPlace(
            id = "s_civic_center_bart",
            title = "Civic Center / UN Plaza BART",
            subtitle = "Market & 8th St • Central Transit Concourse",
            position = LatLng(37.779700, -122.414000),
            category = "TRANSIT",
            address = "Market St & 8th St",
            safetyBadge = "Transit Hub • Police Presence"
        ),
        SearchPlace(
            id = "s_ferry_building",
            title = "Ferry Building Marketplace",
            subtitle = "1 Ferry Building • Waterfront Promenade",
            position = LatLng(37.795500, -122.393700),
            category = "LANDMARK",
            address = "The Embarcadero",
            safetyBadge = "High Security • Wide Pedestrian Esplanade"
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
        LatLng(37.764900, -122.421900), // Valencia & 16th (Lit)
        LatLng(37.763350, -122.421750), // Valencia & 17th (Lit)
        LatLng(37.761800, -122.421600), // Valencia & 18th (Tartine)
        LatLng(37.761650, -122.423900),
        LatLng(37.761500, -122.425800), // Bi-Rite Haven
        DOLORES_PARK
    )

    val nightFastestPolyline = listOf(
        BART_16TH,
        LatLng(37.764800, -122.423800),
        OUTAGE_17TH, // 311 Outage
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
