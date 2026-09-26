package com.example.data

import kotlinx.coroutines.flow.Flow

class LighthouseRepository(private val dao: LighthouseDao) {
    val allReports: Flow<List<CommunityReportEntity>> = dao.getAllReports()
    val allContacts: Flow<List<SafetyContactEntity>> = dao.getAllContacts()
    val allSavedWalks: Flow<List<SavedWalkEntity>> = dao.getAllSavedWalks()

    suspend fun insertReport(report: CommunityReportEntity) = dao.insertReport(report)
    suspend fun incrementConfirmation(id: Int) = dao.incrementConfirmation(id)
    suspend fun insertContact(contact: SafetyContactEntity) = dao.insertContact(contact)
    suspend fun insertSavedWalk(walk: SavedWalkEntity) = dao.insertSavedWalk(walk)
}
