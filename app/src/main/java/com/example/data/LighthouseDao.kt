package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LighthouseDao {

    @Query("SELECT * FROM community_reports ORDER BY timestamp DESC")
    fun getAllReports(): Flow<List<CommunityReportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: CommunityReportEntity): Long

    @Query("UPDATE community_reports SET confirmations = confirmations + 1 WHERE id = :id")
    suspend fun incrementConfirmation(id: Int)

    @Query("SELECT * FROM safety_contacts ORDER BY isPrimary DESC, id ASC")
    fun getAllContacts(): Flow<List<SafetyContactEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: SafetyContactEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContacts(contacts: List<SafetyContactEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReports(reports: List<CommunityReportEntity>)

    @Query("SELECT * FROM saved_walks ORDER BY timestamp DESC")
    fun getAllSavedWalks(): Flow<List<SavedWalkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedWalk(walk: SavedWalkEntity): Long
}
