package com.example.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppSettings
import com.example.data.local.ScanWithDetails
import com.example.data.local.SettingsDataStore
import com.example.data.model.MediaInspectionInfo
import com.example.data.model.RecognitionProviderState
import com.example.data.model.ScanStatus
import com.example.data.model.ThemePreference
import com.example.data.repository.MusicGuardRepository
import com.example.media.MediaInspector
import com.example.recognition.AudioSampleRequest
import com.example.recognition.RecognitionProvider
import com.example.recognition.RecognitionResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface ScanFlowState {
  data object Idle : ScanFlowState
  data class Inspecting(val progressLabel: String) : ScanFlowState
  data class Inspected(
    val media: MediaInspectionInfo,
    val recognitionResult: RecognitionResult? = null,
    val isRecognizing: Boolean = false,
    val savedScanId: String? = null,
  ) : ScanFlowState
  data class Error(val message: String, val fileName: String? = null) : ScanFlowState
}

class MusicGuardViewModel(
  private val repository: MusicGuardRepository,
  private val settingsDataStore: SettingsDataStore,
  private val mediaInspector: MediaInspector,
  private val recognitionProvider: RecognitionProvider,
) : ViewModel() {

  val scans: StateFlow<List<ScanWithDetails>> =
    repository.allScans.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList(),
    )

  val settings: StateFlow<AppSettings> =
    settingsDataStore.settingsFlow.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = AppSettings(),
    )

  private val _scanState = MutableStateFlow<ScanFlowState>(ScanFlowState.Idle)
  val scanState: StateFlow<ScanFlowState> = _scanState.asStateFlow()

  private val _selectedMediaMode = MutableStateFlow("ALL")
  val selectedMediaMode: StateFlow<String> = _selectedMediaMode.asStateFlow()

  private val _showConsentDialog = MutableStateFlow(false)
  val showConsentDialog: StateFlow<Boolean> = _showConsentDialog.asStateFlow()

  private val _statusMessage = MutableStateFlow<String?>(null)
  val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

  private var activeJob: Job? = null

  fun setPreferredScanMode(mode: String) {
    _selectedMediaMode.value = mode
  }

  fun clearStatusMessage() {
    _statusMessage.value = null
  }

  fun observeScan(scanId: String) = repository.observeScan(scanId)

  fun inspectMediaUri(uri: Uri) {
    activeJob?.cancel()
    activeJob =
      viewModelScope.launch {
        _scanState.value = ScanFlowState.Inspecting("Membaca metadata & memeriksa track audio...")
        val info = mediaInspector.inspectUri(uri)
        if (!info.isReadable) {
          _scanState.value =
            ScanFlowState.Error(
              message = info.validationError ?: "File tidak dapat diproses.",
              fileName = info.fileName,
            )
        } else {
          _scanState.value = ScanFlowState.Inspected(media = info)
        }
      }
  }

  fun cancelActiveProcess() {
    activeJob?.cancel()
    activeJob = null
    val current = _scanState.value
    if (current is ScanFlowState.Inspected && current.isRecognizing) {
      _scanState.value = current.copy(isRecognizing = false)
      _statusMessage.value = "Proses pengenalan dibatalkan oleh pengguna."
    } else if (current is ScanFlowState.Inspecting) {
      _scanState.value = ScanFlowState.Idle
      _statusMessage.value = "Inspeksi file dibatalkan."
    }
  }

  fun resetScanState() {
    activeJob?.cancel()
    activeJob = null
    _scanState.value = ScanFlowState.Idle
  }

  fun requestRecognitionCheck() {
    val current = _scanState.value as? ScanFlowState.Inspected ?: return
    if (settings.value.requireUploadConsent) {
      _showConsentDialog.value = true
    } else {
      executeRecognitionCheck(current.media)
    }
  }

  fun dismissConsentDialog() {
    _showConsentDialog.value = false
  }

  fun confirmConsentAndRunRecognition() {
    _showConsentDialog.value = false
    val current = _scanState.value as? ScanFlowState.Inspected ?: return
    executeRecognitionCheck(current.media)
  }

  private fun executeRecognitionCheck(media: MediaInspectionInfo) {
    activeJob?.cancel()
    activeJob =
      viewModelScope.launch {
        _scanState.value = ScanFlowState.Inspected(media = media, isRecognizing = true)
        val result =
          recognitionProvider.recognize(
            sample =
              AudioSampleRequest(
                scanId = "PENDING",
                fileName = media.fileName,
                segmentStartMs = 0L,
                segmentEndMs = (media.durationMs ?: 0L).coerceAtMost(15_000L),
                mimeType = media.mimeType,
              ),
            backendProxyUrl = settings.value.backendProxyUrl,
          )
        _scanState.value =
          ScanFlowState.Inspected(
            media = media,
            recognitionResult = result,
            isRecognizing = false,
          )
      }
  }

  fun saveInspectedMediaToHistory(onSaved: (String) -> Unit) {
    val current = _scanState.value as? ScanFlowState.Inspected ?: return
    viewModelScope.launch {
      val media = current.media
      val result = current.recognitionResult
      val (status, reason, err) =
        when (result) {
          is RecognitionResult.MatchFound ->
            Triple(
              ScanStatus.NEEDS_VERIFICATION,
              "Kandidat lagu ditemukan; bukti lisensi belum diverifikasi.",
              null,
            )
          is RecognitionResult.NoMatch ->
            Triple(
              ScanStatus.UNKNOWN,
              "Tidak ditemukan kecocokan pada sampel. No-match bukan bukti bebas hak cipta.",
              null,
            )
          is RecognitionResult.NotConfigured ->
            Triple(
              ScanStatus.UNKNOWN,
              "Inspeksi media lokal berhasil; layanan pengenalan musik eksternal belum dikonfigurasi.",
              result.reason,
            )
          is RecognitionResult.InvalidToken ->
            Triple(ScanStatus.FAILED, "Token provider pada backend tidak valid.", "Invalid token")
          is RecognitionResult.RateLimited ->
            Triple(ScanStatus.FAILED, "Kuota atau batas permintaan provider terlampaui.", "Rate limited")
          is RecognitionResult.Timeout ->
            Triple(ScanStatus.FAILED, "Permintaan ke layanan pengenalan melebihi batas waktu.", "Timeout")
          is RecognitionResult.NetworkError ->
            Triple(ScanStatus.FAILED, "Koneksi jaringan ke layanan pengenalan gagal.", result.message)
          is RecognitionResult.UnsupportedMedia ->
            Triple(ScanStatus.FAILED, "Format media tidak didukung provider.", result.message)
          is RecognitionResult.MalformedResponse ->
            Triple(ScanStatus.FAILED, "Respons server pengenalan tidak valid.", result.message)
          is RecognitionResult.ServerError ->
            Triple(ScanStatus.FAILED, "Terjadi kesalahan server pengenalan (${result.code}).", result.message)
          null ->
            Triple(
              ScanStatus.UNKNOWN,
              "Metadata & track audio berhasil diperiksa secara lokal. Pengenalan musik eksternal belum dijalankan.",
              null,
            )
        }

      val coverage =
        if (result == null || result is RecognitionResult.NotConfigured) {
          "Inspeksi metadata & track audio lokal saja (sampel belum dikirim ke provider eksternal)."
        } else {
          "Sampel yang diperiksa: 00:00 - ${formatBriefDuration((media.durationMs ?: 0L).coerceAtMost(15_000L))}"
        }

      val scanId =
        repository.saveMediaInspectionRecord(
          inspection = media,
          status = status,
          statusReason = reason,
          coverageDescription = coverage,
          errorMessage = err,
        )
      _scanState.value = current.copy(savedScanId = scanId)
      _statusMessage.value = "Hasil inspeksi disimpan ($scanId)."
      onSaved(scanId)
    }
  }

  fun saveLicenseEvidence(
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
  ) {
    viewModelScope.launch {
      repository.saveLicenseEvidence(
        scanId = scanId,
        sourceName = sourceName,
        licenseType = licenseType,
        licensorName = licensorName,
        allowedPlatforms = allowedPlatforms,
        monetizationAllowed = monetizationAllowed,
        territory = territory,
        validUntilText = validUntilText,
        attributionRequired = attributionRequired,
        licenseUrl = licenseUrl,
        evidenceDocRef = evidenceDocRef,
        notes = notes,
      )
      _statusMessage.value = "Bukti lisensi dicatat ke database lokal."
    }
  }

  fun recordReportExport(scanId: String, format: String, exportedUri: String?) {
    viewModelScope.launch {
      repository.recordExportedReport(scanId, format, exportedUri)
      _statusMessage.value = "Laporan $format berhasil dibuat."
    }
  }

  fun deleteScan(scanId: String) {
    viewModelScope.launch {
      repository.deleteScan(scanId)
      _statusMessage.value = "Catatan pemeriksaan dihapus."
    }
  }

  fun deleteAllScans() {
    viewModelScope.launch {
      repository.deleteAllScans()
      _statusMessage.value = "Seluruh riwayat pemeriksaan telah dihapus."
    }
  }

  fun seedDemoPreview() {
    viewModelScope.launch {
      repository.seedDemoPreviewRecords()
      _statusMessage.value = "Data contoh (DEMO) dimuat untuk pratinjau."
    }
  }

  fun clearDemoPreview() {
    viewModelScope.launch {
      repository.deleteDemoScans()
      _statusMessage.value = "Data contoh (DEMO) dibersihkan."
    }
  }

  fun setThemePreference(theme: ThemePreference) {
    viewModelScope.launch { settingsDataStore.setThemePreference(theme) }
  }

  fun setBackendProxyUrl(url: String) {
    viewModelScope.launch {
      settingsDataStore.setBackendProxyUrl(url)
      _statusMessage.value =
        if (url.isBlank()) "URL proxy dikosongkan." else "URL proxy disimpan (aktivasi request penuh di Tahap 4)."
    }
  }

  fun setRequireUploadConsent(required: Boolean) {
    viewModelScope.launch { settingsDataStore.setRequireUploadConsent(required) }
  }

  fun providerState(appSettings: AppSettings): RecognitionProviderState =
    if (appSettings.backendProxyUrl.isBlank()) {
      RecognitionProviderState.NOT_CONFIGURED
    } else {
      RecognitionProviderState.CONFIGURED_PROXY
    }

  private fun formatBriefDuration(ms: Long): String {
    val sec = ms / 1000L
    return String.format("%02d:%02d", sec / 60L, sec % 60L)
  }

  class Factory(
    private val repository: MusicGuardRepository,
    private val settingsDataStore: SettingsDataStore,
    private val mediaInspector: MediaInspector,
    private val recognitionProvider: RecognitionProvider,
  ) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      return MusicGuardViewModel(
        repository = repository,
        settingsDataStore = settingsDataStore,
        mediaInspector = mediaInspector,
        recognitionProvider = recognitionProvider,
      ) as T
    }
  }
}
