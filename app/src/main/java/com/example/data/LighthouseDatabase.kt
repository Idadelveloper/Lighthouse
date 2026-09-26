package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        CommunityReportEntity::class,
        SafetyContactEntity::class,
        SavedWalkEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class LighthouseDatabase : RoomDatabase() {
    abstract fun lighthouseDao(): LighthouseDao

    companion object {
        @Volatile
        private var INSTANCE: LighthouseDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): LighthouseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LighthouseDatabase::class.java,
                    "lighthouse_database"
                )
                    .addCallback(LighthouseDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class LighthouseDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database.lighthouseDao())
                }
            }
        }

        suspend fun populateInitialData(dao: LighthouseDao) {
            // Seed initial safety contacts
            dao.insertContacts(
                listOf(
                    SafetyContactEntity(
                        name = "Maya Lin (Mom)",
                        relationship = "Mother · Primary Guardian",
                        phoneNumber = "+1 (415) 555-0192",
                        batteryPercent = 82,
                        isSharingGps = true,
                        isPrimary = true
                    ),
                    SafetyContactEntity(
                        name = "Sarah Chen",
                        relationship = "Trusted Friend",
                        phoneNumber = "+1 (415) 555-0144",
                        batteryPercent = 94,
                        isSharingGps = true,
                        isPrimary = false
                    )
                )
            )

            // Seed real civic & community reports
            dao.insertReports(
                listOf(
                    CommunityReportEntity(
                        title = "Streetlight Dark on 17th St",
                        description = "Streetlight fixture dark on 17th St between Mission & Valencia. SFPUC crew dispatched.",
                        category = "OUTAGES",
                        source = "SF 311 • Official Ticket #89214",
                        timeAgo = "14m ago",
                        status = "Utility Dispatched",
                        location = "17th St between Mission & Valencia",
                        verified = true
                    ),
                    CommunityReportEntity(
                        title = "16th St Mission West Elevator",
                        description = "OPERATIONAL. Concourse step-free clearance verified by automated continuous telemetry.",
                        category = "TRANSIT",
                        source = "BART API & SF 511 • Real-time",
                        timeAgo = "3m ago",
                        status = "Active Verification",
                        location = "16th St Mission BART Station",
                        verified = true
                    ),
                    CommunityReportEntity(
                        title = "Paired Transit Ambassadors On Duty",
                        description = "Stationed at 16th & Mission concourse. Walking escorts available along the 16th-Valencia pedestrian corridor.",
                        category = "COMMUNITY",
                        source = "SFMTA Ambassador Program",
                        timeAgo = "Active shift until 11:30 PM",
                        status = "Staff Present",
                        location = "16th & Mission Concourse",
                        verified = true
                    ),
                    CommunityReportEntity(
                        title = "Bi-Rite Creamery (18th & Dolores)",
                        description = "Open until 11:00 PM. Well-lit entryway, public emergency landline, and CPR-trained staff present on premises.",
                        category = "HAVEN",
                        source = "Lighthouse Partner Haven #44",
                        timeAgo = "Confirmed Haven",
                        status = "Open · Safe Haven",
                        location = "3692 18th St",
                        verified = true
                    ),
                    CommunityReportEntity(
                        title = "Sidewalk Pinch Point on Guerrero",
                        description = "Guerrero St sidewalk scaffolding creates a narrow squeeze for wheelchairs & strollers. Recommend crossing to East sidewalk.",
                        category = "ACCESSIBILITY",
                        source = "Pedestrian Observation • Opt-in Community",
                        timeAgo = "28m ago",
                        status = "Community Alert",
                        confirmations = 8,
                        location = "Guerrero St & 17th St",
                        verified = false
                    )
                )
            )
        }
    }
}
