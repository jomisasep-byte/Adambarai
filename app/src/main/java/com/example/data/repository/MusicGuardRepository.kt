package com.example.data.repository

import com.example.data.local.LicenseEvidenceEntity
import com.example.data.local.MusicGuardDao
import com.example.data.local.ScanRecordEntity
import com.example.data.local.ScanReportEntity
import com.example.data.local.ScanWithDetails
import com.example.data.local.TrackMatchEntity
import com.example.data.model.MediaInspectionInfo
import com.example.data.model.MediaKind
import com.example.data.model.ScanStatus
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class MusicGuardRepository(private val dao: MusicGuardDao) {

  val allScans: Flow<List<ScanWithDetails>> = dao.observeAllScans()

  fun observeScan(scanId: String): Flow<ScanWithDetails?> = dao.observeScanById(scanId)

  suspend fun getScan(scanId: String): ScanWithDetails? = dao.getScanById(scanId)

  suspend fun saveMediaInspectionRecord(
    inspection: MediaInspectionInfo,
    status: ScanStatus,
    statusReason: String,
    coverageDescription: String,
    errorMessage: String? = null,
  ): String {
    val scanId = "SCN-${UUID.randomUUID().toString().take(8).uppercase()}"
    val entity =
      ScanRecordEntity(
        id = scanId,
        fileName = inspection.fileName,
        mimeType = inspection.mimeType,
        fileSizeBytes = inspection.fileSizeBytes,
        durationMs = inspection.durationMs,
        mediaKind = inspection.mediaKind.name,
        hasAudioTrack = inspection.hasAudioTrack,
        createdAt = System.currentTimeMillis(),
        scanStatus = status.name,
        statusReason = statusReason,
        scanCoverageDescription = coverageDescription,
        errorMessage = errorMessage,
        isDemo = false,
      )
    dao.insertScanRecord(entity)
    return scanId
  }

  suspend fun saveLicenseEvidence(
    scanId: String,
    sourceName: String,
    licenseType: String,
    licensorName: String,
    allowedPlatforms: String,
    monetizationAllowed: Boolean?,
    territory: String,
    validUntilText: String,
    attributionRequired: Boolean?,
    licenseUrl: String,
    evidenceDocRef: String,
    notes: String,
    markAttentionIfRestricted: Boolean = false,
  ) {
    val existing = dao.getScanById(scanId) ?: return
    val evidenceId = existing.licenseEvidence?.id ?: "LIC-${UUID.randomUUID().toString().take(8).uppercase()}"
    val evidence =
      LicenseEvidenceEntity(
        id = evidenceId,
        scanId = scanId,
        sourceName = sourceName.trim().ifBlank { null },
        licenseType = licenseType.trim().ifBlank { null },
        licensorName = licensorName.trim().ifBlank { null },
        allowedPlatforms = allowedPlatforms.trim().ifBlank { null },
        monetizationAllowed = monetizationAllowed,
        territory = territory.trim().ifBlank { null },
        validUntilText = validUntilText.trim().ifBlank { null },
        attributionRequired = attributionRequired,
        licenseUrl = licenseUrl.trim().ifBlank { null },
        evidenceDocRef = evidenceDocRef.trim().ifBlank { null },
        notes = notes.trim().ifBlank { null },
        updatedAt = System.currentTimeMillis(),
      )
    dao.upsertLicenseEvidence(evidence)

    // Transparent status evaluation based on user-entered evidence
    val currentStatus = runCatching { ScanStatus.valueOf(existing.record.scanStatus) }.getOrNull()
    if (markAttentionIfRestricted || monetizationAllowed == false) {
      dao.updateScanStatus(
        scanId = scanId,
        status = ScanStatus.NEEDS_ATTENTION.name,
        reason =
          "Bukti lisensi dicatat oleh pengguna dengan batasan (mis. monetisasi tidak diizinkan atau cakupan perlu perhatian).",
      )
    } else if (currentStatus == ScanStatus.NEEDS_ATTENTION && monetizationAllowed == true) {
      dao.updateScanStatus(
        scanId = scanId,
        status = ScanStatus.NEEDS_VERIFICATION.name,
        reason =
          "Bukti lisensi dicatat oleh pengguna. Tetap verifikasi cakupan asli dan lakukan YouTube Studio Checks.",
      )
    }
  }

  suspend fun recordExportedReport(scanId: String, format: String, exportedUri: String?) {
    val report =
      ScanReportEntity(
        id = "RPT-${UUID.randomUUID().toString().take(8).uppercase()}",
        scanId = scanId,
        format = format,
        createdAt = System.currentTimeMillis(),
        exportedUri = exportedUri,
      )
    dao.insertScanReport(report)
  }

  suspend fun deleteScan(scanId: String) {
    dao.deleteScanById(scanId)
  }

  suspend fun deleteAllScans() {
    dao.deleteAllScans()
  }

  suspend fun deleteDemoScans() {
    dao.deleteAllDemoScans()
  }

  /**
   * Inserts clearly-labeled DEMO preview records so users can explore the UI flows
   * (Result Detail, Track Match display, License Evidence form, History search, and Reports export)
   * without claiming real API execution.
   */
  suspend fun seedDemoPreviewRecords() {
    val now = System.currentTimeMillis()
    val demo1Id = "DEMO-001"
    val demo2Id = "DEMO-002"

    dao.insertScanRecord(
      ScanRecordEntity(
        id = demo1Id,
        fileName = "vlog_tokyo_highlight_v3.mp4",
        mimeType = "video/mp4",
        fileSizeBytes = 48_240_000L,
        durationMs = 195_000L,
        mediaKind = MediaKind.VIDEO.name,
        hasAudioTrack = true,
        createdAt = now - 3_600_000L,
        scanStatus = ScanStatus.NEEDS_VERIFICATION.name,
        statusReason =
          "[DEMO] Contoh kandidat lagu terdeteksi pada segmen sampel; izin lisensi belum dibuktikan.",
        scanCoverageDescription = "[DEMO] Sampel yang diperiksa: 00:15 - 00:30 & 01:40 - 01:55",
        errorMessage = null,
        isDemo = true,
      )
    )
    dao.insertTrackMatches(
      listOf(
        TrackMatchEntity(
          id = "DEMO-TRK-1",
          scanId = demo1Id,
          title = "Night Drive Synth (Contoh DEMO)",
          artist = "Sample Creator Audio",
          album = "Neon Horizons",
          label = "Independent Demo Label",
          externalUrl = "https://example.com/demo-track-info",
          segmentStartMs = 15_000L,
          segmentEndMs = 30_000L,
          providerName = "AudD (Preview DEMO)",
        )
      )
    )
    dao.upsertLicenseEvidence(
      LicenseEvidenceEntity(
        id = "DEMO-LIC-1",
        scanId = demo1Id,
        sourceName = "Epidemic / Artlist (Contoh DEMO)",
        licenseType = "Creator Subscription License",
        licensorName = "Demo Licensor",
        allowedPlatforms = "YouTube, Instagram",
        monetizationAllowed = true,
        territory = "Worldwide",
        validUntilText = "Selama langganan aktif saat publikasi",
        attributionRequired = false,
        licenseUrl = "https://example.com/demo-license",
        evidenceDocRef = "certificate-demo-2026.pdf",
        notes = "Data contoh DEMO untuk pratinjau UI pencatatan lisensi.",
        updatedAt = now - 3_500_000L,
      )
    )

    dao.insertScanRecord(
      ScanRecordEntity(
        id = demo2Id,
        fileName = "podcast_intro_raw.wav",
        mimeType = "audio/wav",
        fileSizeBytes = 8_420_000L,
        durationMs = 42_000L,
        mediaKind = MediaKind.AUDIO.name,
        hasAudioTrack = true,
        createdAt = now - 7_200_000L,
        scanStatus = ScanStatus.UNKNOWN.name,
        statusReason =
          "[DEMO] Tidak ada kandidat lagu pada sampel. No-match bukan bukti audio bebas hak cipta.",
        scanCoverageDescription = "[DEMO] Sampel yang diperiksa: 00:00 - 00:20",
        errorMessage = null,
        isDemo = true,
      )
    )
  }
}
