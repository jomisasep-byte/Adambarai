package com.example.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "scan_records")
data class ScanRecordEntity(
  @PrimaryKey val id: String,
  val fileName: String,
  val mimeType: String?,
  val fileSizeBytes: Long?,
  val durationMs: Long?,
  val mediaKind: String,
  val hasAudioTrack: Boolean?,
  val createdAt: Long,
  val scanStatus: String,
  val statusReason: String,
  val scanCoverageDescription: String,
  val errorMessage: String? = null,
  val isDemo: Boolean = false,
)

@Entity(
  tableName = "track_matches",
  foreignKeys =
    [
      ForeignKey(
        entity = ScanRecordEntity::class,
        parentColumns = ["id"],
        childColumns = ["scanId"],
        onDelete = ForeignKey.CASCADE,
      )
    ],
  indices = [Index("scanId")],
)
data class TrackMatchEntity(
  @PrimaryKey val id: String,
  val scanId: String,
  val title: String?,
  val artist: String?,
  val album: String?,
  val label: String?,
  val externalUrl: String?,
  val segmentStartMs: Long?,
  val segmentEndMs: Long?,
  val providerName: String,
)

@Entity(
  tableName = "license_evidence",
  foreignKeys =
    [
      ForeignKey(
        entity = ScanRecordEntity::class,
        parentColumns = ["id"],
        childColumns = ["scanId"],
        onDelete = ForeignKey.CASCADE,
      )
    ],
  indices = [Index("scanId")],
)
data class LicenseEvidenceEntity(
  @PrimaryKey val id: String,
  val scanId: String,
  val sourceName: String?,
  val licenseType: String?,
  val licensorName: String?,
  val allowedPlatforms: String?,
  val monetizationAllowed: Boolean?,
  val territory: String?,
  val validUntilText: String?,
  val attributionRequired: Boolean?,
  val licenseUrl: String?,
  val evidenceDocRef: String?,
  val notes: String?,
  val updatedAt: Long,
)

@Entity(
  tableName = "scan_reports",
  foreignKeys =
    [
      ForeignKey(
        entity = ScanRecordEntity::class,
        parentColumns = ["id"],
        childColumns = ["scanId"],
        onDelete = ForeignKey.CASCADE,
      )
    ],
  indices = [Index("scanId")],
)
data class ScanReportEntity(
  @PrimaryKey val id: String,
  val scanId: String,
  val format: String,
  val createdAt: Long,
  val exportedUri: String?,
)

data class ScanWithDetails(
  @Embedded val record: ScanRecordEntity,
  @Relation(parentColumn = "id", entityColumn = "scanId") val matches: List<TrackMatchEntity>,
  @Relation(parentColumn = "id", entityColumn = "scanId")
  val licenseEvidence: LicenseEvidenceEntity?,
  @Relation(parentColumn = "id", entityColumn = "scanId") val reports: List<ScanReportEntity>,
)
