package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface MusicGuardDao {
  @Transaction
  @Query("SELECT * FROM scan_records ORDER BY createdAt DESC")
  fun observeAllScans(): Flow<List<ScanWithDetails>>

  @Transaction
  @Query("SELECT * FROM scan_records WHERE id = :scanId LIMIT 1")
  fun observeScanById(scanId: String): Flow<ScanWithDetails?>

  @Transaction
  @Query("SELECT * FROM scan_records WHERE id = :scanId LIMIT 1")
  suspend fun getScanById(scanId: String): ScanWithDetails?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertScanRecord(record: ScanRecordEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTrackMatches(matches: List<TrackMatchEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertLicenseEvidence(evidence: LicenseEvidenceEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertScanReport(report: ScanReportEntity)

  @Query("UPDATE scan_records SET scanStatus = :status, statusReason = :reason WHERE id = :scanId")
  suspend fun updateScanStatus(scanId: String, status: String, reason: String)

  @Query("DELETE FROM scan_records WHERE id = :scanId")
  suspend fun deleteScanById(scanId: String)

  @Query("DELETE FROM scan_records WHERE isDemo = 1")
  suspend fun deleteAllDemoScans()

  @Query("DELETE FROM scan_records")
  suspend fun deleteAllScans()
}
