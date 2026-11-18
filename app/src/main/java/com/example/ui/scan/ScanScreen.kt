package com.example.ui.scan

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.MediaInspectionInfo
import com.example.data.model.RecognitionProviderState
import com.example.export.Formatters
import com.example.recognition.RecognitionResult
import com.example.ui.ScanFlowState
import com.example.ui.components.CyberCard
import com.example.ui.components.DisclaimerBanner
import com.example.ui.components.ProviderStatusPill
import com.example.ui.components.RecognitionConsentDialog
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberError
import com.example.ui.theme.CyberSuccess
import com.example.ui.theme.CyberVoid
import com.example.ui.theme.CyberWarning
import com.example.ui.theme.MonoDataStyle

@Composable
fun ScanScreen(
  scanState: ScanFlowState,
  selectedMode: String,
  providerState: RecognitionProviderState,
  showConsentDialog: Boolean,
  onModeSelected: (String) -> Unit,
  onMediaPicked: (Uri) -> Unit,
  onCancelProcess: () -> Unit,
  onResetScan: () -> Unit,
  onRequestRecognition: () -> Unit,
  onConfirmConsent: () -> Unit,
  onDismissConsent: () -> Unit,
  onSaveToHistoryAndOpenDetail: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val documentPickerLauncher =
    rememberLauncherForActivityResult(
      contract = ActivityResultContracts.OpenDocument(),
      onResult = { uri ->
        if (uri != null) {
          onMediaPicked(uri)
        }
      },
    )

  fun launchPicker() {
    val mimeTypes =
      when (selectedMode) {
        "VIDEO" -> arrayOf("video/*")
        "AUDIO" -> arrayOf("audio/*")
        else -> arrayOf("video/*", "audio/*")
      }
    documentPickerLauncher.launch(mimeTypes)
  }

  if (showConsentDialog && scanState is ScanFlowState.Inspected) {
    RecognitionConsentDialog(
      fileName = scanState.media.fileName,
      providerName = "AudD (via Backend HTTPS Proxy)",
      onConfirm = onConfirmConsent,
      onDismiss = onDismissConsent,
    )
  }

  Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
    LazyColumn(
      modifier = Modifier.fillMaxSize().widthIn(max = 760.dp),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      item {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text(
            text = "Inspeksi & Pemindaian Media",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
          )
          Text(
            text =
              "Pilih video atau audio lewat Storage Access Framework. File asli tidak pernah diubah.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }

      // Mode Filter & Picker Card
      item {
        CyberCard(borderColor = CyberCyan.copy(alpha = 0.35f)) {
          Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text("Jenis Media", style = MaterialTheme.typography.titleSmall)
              ProviderStatusPill(state = providerState)
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              FilterChip(
                selected = selectedMode == "VIDEO",
                onClick = { onModeSelected("VIDEO") },
                label = { Text("Video") },
                leadingIcon = {
                  Icon(Icons.Filled.VideoFile, contentDescription = null, modifier = Modifier.size(16.dp))
                },
                modifier = Modifier.testTag("mode_chip_video"),
              )
              FilterChip(
                selected = selectedMode == "AUDIO",
                onClick = { onModeSelected("AUDIO") },
                label = { Text("Audio") },
                leadingIcon = {
                  Icon(Icons.Filled.AudioFile, contentDescription = null, modifier = Modifier.size(16.dp))
                },
                modifier = Modifier.testTag("mode_chip_audio"),
              )
              FilterChip(
                selected = selectedMode == "ALL",
                onClick = { onModeSelected("ALL") },
                label = { Text("Semua") },
                leadingIcon = {
                  Icon(Icons.Filled.MusicNote, contentDescription = null, modifier = Modifier.size(16.dp))
                },
                modifier = Modifier.testTag("mode_chip_all"),
              )
            }

            Button(
              onClick = { launchPicker() },
              modifier = Modifier.fillMaxWidth().height(52.dp).testTag("pick_media_button"),
              colors =
                ButtonDefaults.buttonColors(
                  containerColor = CyberCyan,
                  contentColor = CyberVoid,
                ),
            ) {
              Icon(Icons.Filled.FolderOpen, contentDescription = "Pilih file media")
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                when (selectedMode) {
                  "VIDEO" -> "Pilih File Video (SAF)"
                  "AUDIO" -> "Pilih File Audio (SAF)"
                  else -> "Pilih Video atau Audio (SAF)"
                }
              )
            }
          }
        }
      }

      // Dynamic Scan Flow State
      when (scanState) {
        is ScanFlowState.Idle -> {
          item {
            CyberCard {
              Column(
                modifier = Modifier.fillMaxWidth().padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                  Icon(
                    imageVector = Icons.Filled.Radar,
                    contentDescription = "Alur pemeriksaan",
                    tint = CyberCyan,
                  )
                  Text("Tahapan Pemeriksaan", style = MaterialTheme.typography.titleMedium)
                }
                Text(
                  "1. Validasi akses URI, ukuran, durasi, & keberadaan track audio secara lokal.\n" +
                    "2. (Tahap 3–4) Ekstraksi sampel audio & pengenalan via backend HTTPS proxy.\n" +
                    "3. Catat bukti lisensi manual dan ekspor laporan pra-publikasi.",
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }
          }
        }

        is ScanFlowState.Inspecting -> {
          item {
            CyberCard(borderColor = CyberCyan.copy(alpha = 0.5f)) {
              Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                  CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = CyberCyan,
                    strokeWidth = 2.5.dp,
                  )
                  Text(
                    text = scanState.progressLabel,
                    style = MaterialTheme.typography.titleSmall,
                  )
                }
                LinearProgressIndicator(
                  modifier = Modifier.fillMaxWidth(),
                  color = CyberCyan,
                )
                OutlinedButton(
                  onClick = onCancelProcess,
                  modifier = Modifier.fillMaxWidth().testTag("cancel_inspect_button"),
                ) {
                  Icon(Icons.Filled.Cancel, contentDescription = "Batalkan inspeksi")
                  Spacer(modifier = Modifier.width(8.dp))
                  Text("Batalkan")
                }
              }
            }
          }
        }

        is ScanFlowState.Error -> {
          item {
            CyberCard(borderColor = CyberError.copy(alpha = 0.6f)) {
              Column(
                modifier = Modifier.fillMaxWidth().padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                  Icon(
                    imageVector = Icons.Filled.ErrorOutline,
                    contentDescription = "Validasi media gagal",
                    tint = CyberError,
                  )
                  Text(
                    text = "Validasi Media Gagal",
                    style = MaterialTheme.typography.titleMedium,
                    color = CyberError,
                  )
                }
                if (!scanState.fileName.isNullOrBlank()) {
                  Text(
                    text = "File: ${scanState.fileName}",
                    style = MonoDataStyle,
                    color = MaterialTheme.colorScheme.onSurface,
                  )
                }
                Text(
                  text = scanState.message,
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                  Button(
                    onClick = { launchPicker() },
                    modifier = Modifier.testTag("retry_pick_media_button"),
                  ) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Pilih file lain")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Pilih File Lain")
                  }
                  OutlinedButton(
                    onClick = onResetScan,
                    modifier = Modifier.testTag("reset_error_button"),
                  ) {
                    Text("Tutup")
                  }
                }
              }
            }
          }
        }

        is ScanFlowState.Inspected -> {
          item {
            InspectedMediaCard(
              media = scanState.media,
              recognitionResult = scanState.recognitionResult,
              isRecognizing = scanState.isRecognizing,
              onRequestRecognition = onRequestRecognition,
              onCancelRecognition = onCancelProcess,
              onSaveAndOpenDetail = onSaveToHistoryAndOpenDetail,
              onReset = onResetScan,
            )
          }
        }
      }

      item {
        DisclaimerBanner(
          body =
            "Hasil inspeksi file dan pengenalan audio bersifat pendukung pra-publikasi. Bukan YouTube Content ID.",
        )
      }
    }
  }
}

@Composable
private fun InspectedMediaCard(
  media: MediaInspectionInfo,
  recognitionResult: RecognitionResult?,
  isRecognizing: Boolean,
  onRequestRecognition: () -> Unit,
  onCancelRecognition: () -> Unit,
  onSaveAndOpenDetail: () -> Unit,
  onReset: () -> Unit,
) {
  CyberCard(borderColor = CyberSuccess.copy(alpha = 0.5f)) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Icon(
          imageVector = Icons.Filled.CheckCircle,
          contentDescription = "Metadata media valid",
          tint = CyberSuccess,
        )
        Text(
          text = "Metadata & Track Audio Tervalidasi",
          style = MaterialTheme.typography.titleMedium,
          color = CyberSuccess,
        )
      }

      MetadataRow("Nama File", media.fileName)
      MetadataRow("Jenis / MIME", "${media.mediaKind.labelId} (${media.mimeType ?: "tidak diketahui"})")
      MetadataRow("Ukuran", Formatters.formatBytes(media.fileSizeBytes))
      MetadataRow("Durasi", Formatters.formatDuration(media.durationMs))
      MetadataRow(
        "Track Audio",
        if (media.hasAudioTrack == true) {
          "Terdeteksi (${media.audioCodecSummary ?: "audio"})"
        } else {
          "Tidak terdeteksi"
        },
      )

      if (isRecognizing) {
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = CyberCyan)
        OutlinedButton(
          onClick = onCancelRecognition,
          modifier = Modifier.fillMaxWidth().testTag("cancel_recognition_button"),
        ) {
          Text("Batalkan Pengenalan")
        }
      } else {
        if (recognitionResult != null) {
          CyberCard(
            borderColor = CyberWarning.copy(alpha = 0.5f),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
          ) {
            Row(
              modifier = Modifier.padding(12.dp),
              horizontalArrangement = Arrangement.spacedBy(10.dp),
              verticalAlignment = Alignment.Top,
            ) {
              Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = "Status provider pengenalan",
                tint = CyberWarning,
              )
              Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                  text = "Status Pengenalan Musik",
                  style = MaterialTheme.typography.titleSmall,
                  color = CyberWarning,
                )
                val message =
                  when (recognitionResult) {
                    is RecognitionResult.NotConfigured -> recognitionResult.reason
                    is RecognitionResult.NoMatch ->
                      "Tidak ditemukan kecocokan pada sampel (${recognitionResult.coverageDescription})."
                    is RecognitionResult.MatchFound ->
                      "Ditemukan ${recognitionResult.candidates.size} kandidat lagu."
                    is RecognitionResult.NetworkError -> recognitionResult.message
                    is RecognitionResult.ServerError ->
                      "Server error ${recognitionResult.code}: ${recognitionResult.message}"
                    else -> "Status: $recognitionResult"
                  }
                Text(
                  text = message,
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          OutlinedButton(
            onClick = onRequestRecognition,
            modifier = Modifier.weight(1f).height(48.dp).testTag("run_recognition_button"),
          ) {
            Icon(Icons.Filled.Radar, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Cek Audio")
          }
          Button(
            onClick = onSaveAndOpenDetail,
            modifier = Modifier.weight(1f).height(48.dp).testTag("save_inspection_button"),
            colors =
              ButtonDefaults.buttonColors(
                containerColor = CyberCyan,
                contentColor = CyberVoid,
              ),
          ) {
            Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Simpan & Lisensi")
          }
        }
        OutlinedButton(
          onClick = onReset,
          modifier = Modifier.fillMaxWidth().testTag("reset_inspected_button"),
        ) {
          Text("Bersihkan Pilihan")
        }
      }
    }
  }
}

@Composable
private fun MetadataRow(label: String, value: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.Top,
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.width(105.dp),
    )
    Text(
      text = value,
      style = MonoDataStyle,
      color = MaterialTheme.colorScheme.onSurface,
      modifier = Modifier.weight(1f),
    )
  }
}
