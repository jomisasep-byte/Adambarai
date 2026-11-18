package com.example.export

import com.example.data.local.ScanWithDetails
import com.example.data.model.ScanStatus
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Formatters {
  private val dateFormatter = SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale("id", "ID"))

  fun formatTimestamp(epochMs: Long): String = dateFormatter.format(Date(epochMs))

  fun formatBytes(bytes: Long?): String {
    if (bytes == null || bytes < 0L) return "Tidak diketahui"
    val kb = 1024.0
    val mb = kb * 1024.0
    val gb = mb * 1024.0
    return when {
      bytes >= gb -> String.format(Locale.US, "%.2f GB", bytes / gb)
      bytes >= mb -> String.format(Locale.US, "%.2f MB", bytes / mb)
      bytes >= kb -> String.format(Locale.US, "%.1f KB", bytes / kb)
      else -> "$bytes B"
    }
  }

  fun formatDuration(durationMs: Long?): String {
    if (durationMs == null || durationMs < 0L) return "Tidak diketahui"
    val totalSeconds = durationMs / 1000L
    val hours = totalSeconds / 3600L
    val minutes = (totalSeconds % 3600L) / 60L
    val seconds = totalSeconds % 60L
    return if (hours > 0) {
      String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
    } else {
      String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }
  }
}

object ReportGenerator {

  private const val DISCLAIMER_TEXT =
    "MUSIC GUARD AI bukan YouTube Content ID dan bukan sistem verifikasi hak cipta resmi YouTube. " +
      "Laporan ini tidak menjamin video bebas klaim, blokir, atau demonetisasi. " +
      "Selalu periksa bukti lisensi resmi dan YouTube Studio Checks sebelum publikasi."

  fun generateMarkdown(scan: ScanWithDetails): String {
    val r = scan.record
    val status = runCatching { ScanStatus.valueOf(r.scanStatus) }.getOrNull()
    val statusLabel = status?.labelId ?: r.scanStatus
    val demoBanner =
      if (r.isDemo) "\n> **CATATAN:** Rekaman ini adalah data pratinjau **DEMO**, bukan hasil pemindaian API nyata.\n"
      else ""

    return buildString {
      appendLine("# Laporan Pemeriksaan MUSIC GUARD AI")
      if (demoBanner.isNotBlank()) appendLine(demoBanner)
      appendLine("- **ID Pemeriksaan:** `${r.id}`")
      appendLine("- **Waktu Pemeriksaan:** ${Formatters.formatTimestamp(r.createdAt)}")
      appendLine("- **Status:** **$statusLabel**")
      appendLine("- **Alasan Status:** ${r.statusReason}")
      appendLine()
      appendLine("## 1. Informasi Media")
      appendLine("- **Nama File:** `${r.fileName}`")
      appendLine("- **Jenis Media:** ${r.mediaKind}")
      appendLine("- **Tipe MIME:** ${r.mimeType ?: "Tidak tersedia"}")
      appendLine("- **Ukuran:** ${Formatters.formatBytes(r.fileSizeBytes)}")
      appendLine("- **Durasi:** ${Formatters.formatDuration(r.durationMs)}")
      appendLine("- **Track Audio:** ${when (r.hasAudioTrack) { true -> "Terdeteksi"; false -> "Tidak ada"; null -> "Belum diperiksa" }}")
      appendLine("- **Cakupan Pemeriksaan:** ${r.scanCoverageDescription}")
      if (!r.errorMessage.isNullOrBlank()) {
        appendLine("- **Detail Error:** ${r.errorMessage}")
      }
      appendLine()
      appendLine("## 2. Kandidat Lagu (Pengenalan Musik)")
      if (scan.matches.isEmpty()) {
        appendLine("_Tidak ada kandidat lagu yang tercatat pada pemeriksaan ini._")
      } else {
        scan.matches.forEachIndexed { index, m ->
          appendLine("### Kandidat ${index + 1}")
          appendLine("- **Judul:** ${m.title ?: "-"}")
          appendLine("- **Artis:** ${m.artist ?: "-"}")
          appendLine("- **Album:** ${m.album ?: "-"}")
          appendLine("- **Label:** ${m.label ?: "-"}")
          appendLine("- **Provider:** ${m.providerName}")
          appendLine(
            "- **Segmen Cocok:** ${Formatters.formatDuration(m.segmentStartMs)} - ${Formatters.formatDuration(m.segmentEndMs)}"
          )
          if (!m.externalUrl.isNullOrBlank()) {
            appendLine("- **Tautan Referensi:** ${m.externalUrl}")
          }
        }
      }
      appendLine()
      appendLine("## 3. Bukti Lisensi (Dicatat Pengguna)")
      val lic = scan.licenseEvidence
      if (lic == null) {
        appendLine("_Belum ada bukti lisensi yang dicatat oleh pengguna._")
      } else {
        appendLine("- **Sumber Musik:** ${lic.sourceName ?: "-"}")
        appendLine("- **Jenis Lisensi:** ${lic.licenseType ?: "-"}")
        appendLine("- **Pemberi Lisensi:** ${lic.licensorName ?: "-"}")
        appendLine("- **Platform Diizinkan:** ${lic.allowedPlatforms ?: "-"}")
        appendLine(
          "- **Izin Monetisasi:** ${when (lic.monetizationAllowed) { true -> "Ya"; false -> "Tidak"; null -> "Belum diisi" }}"
        )
        appendLine("- **Wilayah:** ${lic.territory ?: "-"}")
        appendLine("- **Masa Berlaku:** ${lic.validUntilText ?: "-"}")
        appendLine(
          "- **Wajib Atribusi:** ${when (lic.attributionRequired) { true -> "Ya"; false -> "Tidak"; null -> "Belum diisi" }}"
        )
        appendLine("- **URL Lisensi:** ${lic.licenseUrl ?: "-"}")
        appendLine("- **Dokumen Bukti:** ${lic.evidenceDocRef ?: "-"}")
        appendLine("- **Catatan:** ${lic.notes ?: "-"}")
      }
      appendLine()
      appendLine("## 4. Rekomendasi Langkah Selanjutnya")
      appendLine("1. Verifikasi dokumen/sertifikat lisensi asli dari penyedia musik.")
      appendLine("2. Pastikan platform YouTube, monetisasi, wilayah, dan masa berlaku tercakup.")
      appendLine("3. Unggah sebagai Unlisted/Draft terlebih dahulu dan pantau **YouTube Studio Checks**.")
      appendLine()
      appendLine("## 5. Disclaimer")
      appendLine("> $DISCLAIMER_TEXT")
    }
  }

  fun generateJson(scan: ScanWithDetails): String {
    val r = scan.record
    val status = runCatching { ScanStatus.valueOf(r.scanStatus) }.getOrNull()
    val root = JSONObject()
    root.put("app", "MUSIC GUARD AI")
    root.put("scanId", r.id)
    root.put("isDemo", r.isDemo)
    root.put("createdAtEpochMs", r.createdAt)
    root.put("createdAtFormatted", Formatters.formatTimestamp(r.createdAt))
    root.put("status", status?.labelId ?: r.scanStatus)
    root.put("statusReason", r.statusReason)
    root.put("coverageDescription", r.scanCoverageDescription)
    root.put("errorMessage", r.errorMessage ?: JSONObject.NULL)

    val mediaObj =
      JSONObject().apply {
        put("fileName", r.fileName)
        put("mediaKind", r.mediaKind)
        put("mimeType", r.mimeType ?: JSONObject.NULL)
        put("fileSizeBytes", r.fileSizeBytes ?: JSONObject.NULL)
        put("durationMs", r.durationMs ?: JSONObject.NULL)
        put("hasAudioTrack", r.hasAudioTrack ?: JSONObject.NULL)
      }
    root.put("media", mediaObj)

    val matchesArray = JSONArray()
    scan.matches.forEach { m ->
      matchesArray.put(
        JSONObject().apply {
          put("id", m.id)
          put("title", m.title ?: JSONObject.NULL)
          put("artist", m.artist ?: JSONObject.NULL)
          put("album", m.album ?: JSONObject.NULL)
          put("label", m.label ?: JSONObject.NULL)
          put("provider", m.providerName)
          put("segmentStartMs", m.segmentStartMs ?: JSONObject.NULL)
          put("segmentEndMs", m.segmentEndMs ?: JSONObject.NULL)
          put("externalUrl", m.externalUrl ?: JSONObject.NULL)
        }
      )
    }
    root.put("trackMatches", matchesArray)

    val lic = scan.licenseEvidence
    root.put(
      "licenseEvidence",
      if (lic == null) {
        JSONObject.NULL
      } else {
        JSONObject().apply {
          put("statusLabel", "Bukti lisensi dicatat oleh pengguna")
          put("sourceName", lic.sourceName ?: JSONObject.NULL)
          put("licenseType", lic.licenseType ?: JSONObject.NULL)
          put("licensorName", lic.licensorName ?: JSONObject.NULL)
          put("allowedPlatforms", lic.allowedPlatforms ?: JSONObject.NULL)
          put("monetizationAllowed", lic.monetizationAllowed ?: JSONObject.NULL)
          put("territory", lic.territory ?: JSONObject.NULL)
          put("validUntil", lic.validUntilText ?: JSONObject.NULL)
          put("attributionRequired", lic.attributionRequired ?: JSONObject.NULL)
          put("licenseUrl", lic.licenseUrl ?: JSONObject.NULL)
          put("evidenceDocRef", lic.evidenceDocRef ?: JSONObject.NULL)
          put("notes", lic.notes ?: JSONObject.NULL)
        }
      },
    )

    root.put(
      "recommendations",
      JSONArray(
        listOf(
          "Verifikasi dokumen lisensi asli dari penyedia musik.",
          "Pastikan izin monetisasi, wilayah, dan atribusi terpenuhi.",
          "Lakukan YouTube Studio Checks sebelum mempublikasikan video secara publik.",
        )
      ),
    )
    root.put("disclaimer", DISCLAIMER_TEXT)
    return root.toString(2)
  }
}
