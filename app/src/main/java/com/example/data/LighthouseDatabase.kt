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
            // Seed sample pedestrian observations clearly marked as unverified samples
            dao.insertReports(
                listOf(
                    CommunityReportEntity(
                        title = "Reported Dark Fixture on 17th St",
                        description = "Pedestrian observation of dark streetlight fixture near 17th St. Check SF 311 for recent service tickets.",
                        category = "OUTAGES",
                        source = "Sample • not live",
                        timeAgo = "Sample",
                        status = "Unverified Sample",
                        location = "17th St between Mission & Valencia",
                        verified = false
                    ),
                    CommunityReportEntity(
                        title = "16th St Mission Concourse Access",
                        description = "Station concourse elevator access. Real-time telemetry feed not connected.",
                        category = "TRANSIT",
                        source = "Sample • not live",
                        timeAgo = "Sample",
                        status = "Unverified Sample",
                        location = "16th St Mission BART Station",
                        verified = false
                    ),
                    CommunityReportEntity(
                        title = "Bi-Rite Creamery Storefront",
                        description = "Neighborhood storefront. Live operating hours and staffing unverified by feed.",
                        category = "HAVEN",
                        source = "Sample • not live",
                        timeAgo = "Sample",
                        status = "Unverified Sample",
                        location = "3692 18th St",
                        verified = false
                    ),
                    CommunityReportEntity(
                        title = "Sidewalk Scaffolding on Guerrero",
                        description = "Community observation noting narrow pathway near scaffolding on Guerrero St.",
                        category = "ACCESSIBILITY",
                        source = "Sample • not live",
                        timeAgo = "Sample",
                        status = "Unverified Sample",
                        confirmations = 2,
                        location = "Guerrero St & 17th St",
                        verified = false
                    )
                )
            )
        }
    }
}
