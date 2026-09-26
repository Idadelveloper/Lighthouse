package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "community_reports")
data class CommunityReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val category: String, // OUTAGES, ACCESSIBILITY, COMMUNITY, TRANSIT, HAVEN
    val source: String,
    val timeAgo: String,
    val status: String,
    val confirmations: Int = 0,
    val location: String = "17th & Mission",
    val verified: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "safety_contacts")
data class SafetyContactEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val relationship: String,
    val phoneNumber: String,
    val batteryPercent: Int = 82,
    val isSharingGps: Boolean = true,
    val isPrimary: Boolean = false,
    val avatarUrl: String = ""
)

@Entity(tableName = "saved_walks")
data class SavedWalkEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val origin: String,
    val destination: String,
    val durationMinutes: Int,
    val distanceMiles: Float,
    val conditionMode: String, // DAY_SHADE or NIGHT_ILLUMINATED
    val timestamp: Long = System.currentTimeMillis()
)
