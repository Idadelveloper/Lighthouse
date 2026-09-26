package com.example.model

/**
 * Normalized provider for ground truth and civic evidence layers along the
 * 16th St Mission BART -> Mission Dolores Park corridor.
 *
 * Source semantics:
 * - SFPUC Streetlights FeatureServer: static dataset inventory only. An ACTIVE asset does not prove the lamp currently works.
 * - SF 311 dataset vw6y-z8j6: static metadata only. Check current SF 311 reports; status UNKNOWN until a backend retrieval supplies fetchedAt/report timestamps.
 * - Daytime shade: no verified live official shade feed connected. Status UNKNOWN.
 * - Community observations: unverified until separately confirmed.
 */
object KnownCorridorEvidence {

    const val SFPUC_URL =
        "https://services.arcgis.com/Zs2aNLFN00jrS4gG/arcgis/rest/services/SFPUC_Streetlights/FeatureServer/0"
    const val SF311_URL =
        "https://data.sf.gov/resource/vw6y-z8j6.json"
    const val SF311_DATASET_ID = "vw6y-z8j6"
    const val SFPUC_DATASET_ID = "SFPUC_Streetlights_0"

    const val SFPUC_LIMITATION =
        "Dataset inventory only. An ACTIVE asset record indicates a mapped streetlight pole, but does not prove the lamp currently works or is emitting light."

    const val SF311_LIMITATION =
        "Reports are not exhaustive, and no report does not mean no problem. Municipal reports rely on citizen logging and scheduled inspection."

    const val SHADE_LIMITATION =
        "No verified live official shade feed is connected yet. Status UNKNOWN. Tree canopy estimates and sun angles are not live verified sensor feeds."

    const val COMMUNITY_LIMITATION =
        "Community observations are unverified until separately confirmed by multiple pedestrians or official verification."

    fun getEvidence(mode: DayNightMode): List<RouteEvidenceItem> {
        return when (mode) {
            DayNightMode.NIGHT -> listOf(
                RouteEvidenceItem(
                    id = "night_puc_streetlights",
                    category = "LIGHTING",
                    title = "Streetlight assets mapped",
                    summary = "Streetlight assets mapped along Valencia corridor. Working status unknown.",
                    sourceName = "SFPUC Streetlights FeatureServer",
                    sourceDatasetId = SFPUC_DATASET_ID,
                    sourceUrl = SFPUC_URL,
                    evidenceTimeLabel = "Static Inventory Layer",
                    status = EvidenceStatus.AVAILABLE,
                    limitation = SFPUC_LIMITATION
                ),
                RouteEvidenceItem(
                    id = "night_311_outages",
                    category = "311_REPORTS",
                    title = "Check current SF 311 reports",
                    summary = "Check current SF 311 reports for streetlight outages near 17th St. Status UNKNOWN until live feed connected.",
                    sourceName = "SF 311 Cases",
                    sourceDatasetId = SF311_DATASET_ID,
                    sourceUrl = SF311_URL,
                    evidenceTimeLabel = "Not fetched yet",
                    status = EvidenceStatus.UNKNOWN,
                    limitation = SF311_LIMITATION
                ),
                RouteEvidenceItem(
                    id = "night_shade_layer",
                    category = "SHADE",
                    title = "Shade data unavailable",
                    summary = "Compare sun exposure when a verified layer is connected.",
                    sourceName = "Solar Ingestion Feed",
                    sourceDatasetId = "N/A",
                    sourceUrl = "https://data.sf.gov",
                    evidenceTimeLabel = "Not Connected",
                    status = EvidenceStatus.UNKNOWN,
                    limitation = SHADE_LIMITATION
                ),
                RouteEvidenceItem(
                    id = "night_community_reports",
                    category = "COMMUNITY",
                    title = "Pedestrian Community Reports",
                    summary = "In-app community observations pending independent verification.",
                    sourceName = "Lighthouse Community Reports",
                    sourceDatasetId = "community_local",
                    sourceUrl = "https://data.sf.gov",
                    evidenceTimeLabel = "Unverified User Submissions",
                    status = EvidenceStatus.COMMUNITY_UNVERIFIED,
                    limitation = COMMUNITY_LIMITATION
                )
            )

            DayNightMode.DAY -> listOf(
                RouteEvidenceItem(
                    id = "day_shade_layer",
                    category = "SHADE",
                    title = "Shade data unavailable",
                    summary = "Compare sun exposure when a verified layer is connected. No verified live feed connected.",
                    sourceName = "Civic Shade & Canopy Feed",
                    sourceDatasetId = "N/A",
                    sourceUrl = "https://data.sf.gov",
                    evidenceTimeLabel = "Feed Pending",
                    status = EvidenceStatus.UNKNOWN,
                    limitation = SHADE_LIMITATION
                ),
                RouteEvidenceItem(
                    id = "day_puc_streetlights",
                    category = "LIGHTING",
                    title = "Streetlight assets mapped",
                    summary = "Streetlight physical inventory mapped along corridor. Working status unknown.",
                    sourceName = "SFPUC Streetlights FeatureServer",
                    sourceDatasetId = SFPUC_DATASET_ID,
                    sourceUrl = SFPUC_URL,
                    evidenceTimeLabel = "Static Inventory Layer",
                    status = EvidenceStatus.AVAILABLE,
                    limitation = SFPUC_LIMITATION
                ),
                RouteEvidenceItem(
                    id = "day_311_sidewalks",
                    category = "311_REPORTS",
                    title = "Check current SF 311 reports",
                    summary = "Check current SF 311 reports for pavement repairs. Status UNKNOWN until live feed connected.",
                    sourceName = "SF 311 Cases",
                    sourceDatasetId = SF311_DATASET_ID,
                    sourceUrl = SF311_URL,
                    evidenceTimeLabel = "Not fetched yet",
                    status = EvidenceStatus.UNKNOWN,
                    limitation = SF311_LIMITATION
                ),
                RouteEvidenceItem(
                    id = "day_community_reports",
                    category = "COMMUNITY",
                    title = "Pedestrian Community Reports",
                    summary = "Community reports on curb conditions. Unverified until separately confirmed.",
                    sourceName = "Lighthouse Community Reports",
                    sourceDatasetId = "community_local",
                    sourceUrl = "https://data.sf.gov",
                    evidenceTimeLabel = "Unverified User Submissions",
                    status = EvidenceStatus.COMMUNITY_UNVERIFIED,
                    limitation = COMMUNITY_LIMITATION
                )
            )
        }
    }
}
