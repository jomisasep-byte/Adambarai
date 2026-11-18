package com.example.data.model

enum class ScanStatus(
  val labelId: String,
  val summaryId: String,
) {
  NEEDS_VERIFICATION(
    labelId = "PERLU VERIFIKASI",
    summaryId = "Kandidat lagu ditemukan atau perlu cek hak pakai; izin belum dibuktikan.",
  ),
  UNKNOWN(
    labelId = "BELUM DIKETAHUI",
    summaryId = "Belum ada kecocokan lagu pada sampel atau layanan pengenalan belum dijalankan.",
  ),
  NEEDS_ATTENTION(
    labelId = "PERLU PERHATIAN",
    summaryId = "Data lisensi yang dicatat mengindikasikan izin mungkin belum sesuai atau terbatas.",
  ),
  FAILED(
    labelId = "PEMERIKSAAN GAGAL",
    summaryId = "Validasi media, ekstraksi audio, atau pemanggilan API pengenalan gagal.",
  ),
}

enum class MediaKind(val labelId: String) {
  VIDEO("Video"),
  AUDIO("Audio"),
  UNKNOWN("Tidak Diketahui"),
}

enum class ThemePreference(val labelId: String) {
  CYBER_DARK("Cyber Dark (Default)"),
  LIGHT("Terang"),
  SYSTEM("Ikuti Sistem"),
}

enum class RecognitionProviderState(val labelId: String) {
  NOT_CONFIGURED("Belum Dikonfigurasi"),
  CONFIGURED_PROXY("Proxy Terkonfigurasi (Siap Tahap 4)"),
}

data class MediaInspectionInfo(
  val uriString: String,
  val fileName: String,
  val mimeType: String?,
  val fileSizeBytes: Long?,
  val durationMs: Long?,
  val mediaKind: MediaKind,
  val hasAudioTrack: Boolean?,
  val audioCodecSummary: String?,
  val isReadable: Boolean,
  val validationError: String? = null,
)
