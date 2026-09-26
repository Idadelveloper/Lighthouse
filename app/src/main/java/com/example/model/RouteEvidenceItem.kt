package com.example.model

enum class EvidenceStatus {
    AVAILABLE,
    UNKNOWN,
    COMMUNITY_UNVERIFIED
}

/**
 * Normalized evidence contract for pedestrian routing conditions along the
 * 16th St Mission BART to Mission Dolores Park corridor.
 */
data class RouteEvidenceItem(
    val id: String,
    val category: String, // e.g. "LIGHTING", "311_REPORTS", "SHADE", "SIDEWALK", "TRANSIT", "COMMUNITY"
    val title: String,
    val summary: String,
    val sourceName: String,
    val sourceDatasetId: String,
    val sourceUrl: String,
    val evidenceTimeLabel: String,
    val status: EvidenceStatus,
    val limitation: String
)
