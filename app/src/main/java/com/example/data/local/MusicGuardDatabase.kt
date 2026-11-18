package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
  entities =
    [
      ScanRecordEntity::class,
      TrackMatchEntity::class,
      LicenseEvidenceEntity::class,
      ScanReportEntity::class,
    ],
  version = 1,
  exportSchema = false,
)
abstract class MusicGuardDatabase : RoomDatabase() {
  abstract fun musicGuardDao(): MusicGuardDao

  companion object {
    @Volatile private var INSTANCE: MusicGuardDatabase? = null

    fun getInstance(context: Context): MusicGuardDatabase {
      return INSTANCE
        ?: synchronized(this) {
          INSTANCE
            ?: Room.databaseBuilder(
                context.applicationContext,
                MusicGuardDatabase::class.java,
                "music_guard_ai.db",
              )
              .build()
              .also { INSTANCE = it }
        }
    }
  }
}
