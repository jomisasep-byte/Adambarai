package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.LicenseEvidenceEntity
import com.example.data.local.ScanRecordEntity
import com.example.data.local.ScanWithDetails
import com.example.data.model.ScanStatus
import com.example.export.ReportGenerator
import com.example.recognition.AudioSampleRequest
import com.example.recognition.AuddRecognitionProvider
import com.example.recognition.RecognitionResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `app_name matches Music Guard AI`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Music Guard AI", appName)
  }

  @Test
  fun `audd provider returns NotConfigured when proxy url is blank`() = runTest {
    val provider = AuddRecognitionProvider()
    val result =
      provider.recognize(
        sample =
          AudioSampleRequest(
            scanId = "SCN-TEST",
            fileName = "test.mp3",
            segmentStartMs = 0L,
            segmentEndMs = 15_000L,
            mimeType = "audio/mpeg",
          ),
        backendProxyUrl = "",
      )
    assertTrue(result is RecognitionResult.NotConfigured)
  }

  @Test
  fun `report generator includes disclaimer and honest status`() {
    val scan =
      ScanWithDetails(
        record =
          ScanRecordEntity(
            id = "SCN-1234",
            fileName = "clip.mp4",
            mimeType = "video/mp4",
            fileSizeBytes = 2048L,
            durationMs = 12_000L,
            mediaKind = "VIDEO",
            hasAudioTrack = true,
            createdAt = 1_700_000_000_000L,
            scanStatus = ScanStatus.UNKNOWN.name,
            statusReason = "Belum ada kecocokan lagu.",
            scanCoverageDescription = "Inspeksi metadata lokal",
          ),
        matches = emptyList(),
        licenseEvidence =
          LicenseEvidenceEntity(
            id = "LIC-1",
            scanId = "SCN-1234",
            sourceName = "YouTube Audio Library",
            licenseType = "Standard",
            licensorName = null,
            allowedPlatforms = "YouTube",
            monetizationAllowed = true,
            territory = "Worldwide",
            validUntilText = null,
            attributionRequired = false,
            licenseUrl = null,
            evidenceDocRef = null,
            notes = null,
            updatedAt = 1_700_000_000_000L,
          ),
        reports = emptyList(),
      )

    val md = ReportGenerator.generateMarkdown(scan)
    val json = ReportGenerator.generateJson(scan)

    assertTrue(md.contains("BELUM DIKETAHUI"))
    assertTrue(md.contains("bukan YouTube Content ID"))
    assertTrue(json.contains("\"scanId\": \"SCN-1234\""))
    assertTrue(json.contains("YouTube Audio Library"))
  }
}
