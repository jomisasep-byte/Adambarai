package com.example.ui.result

import android.content.Intent
import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.local.ScanWithDetails
import com.example.data.model.ScanStatus
import com.example.export.Formatters
import com.example.export.ReportGenerator
import com.example.ui.components.CyberCard
import com.example.ui.components.DemoBadge
import com.example.ui.components.DisclaimerBanner
import com.example.ui.components.StatusBadge
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberVoid
import com.example.ui.theme.MonoDataStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultDetailScreen(
  scan: ScanWithDetails?,
  onBack: () -> Unit,
  onSaveLicenseEvidence:
    (
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
    ) -> Unit,
  onRecordExport: (scanId: String, format: String, uri: String?) -> Unit,
  modifier: Modifier = Modifier,
) {
  BackHandler(onBack = onBack)

  val context = LocalContext.current

  val createMdLauncher =
    rememberLauncherForActivityResult(
      contract = ActivityResultContracts.CreateDocument("text/markdown"),
      onResult = { uri ->
        if (uri != null && scan != null) {
          runCatching {
            context.contentResolver.openOutputStream(uri)?.use { out ->
              out.write(ReportGenerator.generateMarkdown(scan).toByteArray(Charsets.UTF_8))
            }
          }
          onRecordExport(scan.record.id, "MARKDOWN", uri.toString())
        }
      },
    )

  Scaffold(
    modifier = modifier.fillMaxSize(),
    topBar = {
      TopAppBar(
        title = { Text("Detail Pemeriksaan") },
        navigationIcon = {
          IconButton(
            onClick = onBack,
            modifier = Modifier.testTag("result_back_button"),
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Kembali",
            )
          }
        },
        colors =
          TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
          ),
      )
    },
  ) { innerPadding ->
    if (scan == null) {
      Box(
        modifier = Modifier.fillMaxSize().padding(innerPadding),
        contentAlignment = Alignment.Center,
      ) {
        Text("Data pemeriksaan tidak ditemukan.")
      }
      return@Scaffold
    }

    val record = scan.record
    val status =
      runCatching { ScanStatus.valueOf(record.scanStatus) }.getOrDefault(ScanStatus.UNKNOWN)

    var sourceName by remember(record.id) { mutableStateOf(scan.licenseEvidence?.sourceName ?: "") }
    var licenseType by remember(record.id) { mutableStateOf(scan.licenseEvidence?.licenseType ?: "") }
    var licensorName by remember(record.id) { mutableStateOf(scan.licenseEvidence?.licensorName ?: "") }
    var allowedPlatforms by
      remember(record.id) { mutableStateOf(scan.licenseEvidence?.allowedPlatforms ?: "YouTube") }
    var monetizationAllowed by
      remember(record.id) { mutableStateOf(scan.licenseEvidence?.monetizationAllowed) }
    var territory by remember(record.id) { mutableStateOf(scan.licenseEvidence?.territory ?: "Worldwide") }
    var validUntilText by remember(record.id) { mutableStateOf(scan.licenseEvidence?.validUntilText ?: "") }
    var attributionRequired by
      remember(record.id) { mutableStateOf(scan.licenseEvidence?.attributionRequired) }
    var licenseUrl by remember(record.id) { mutableStateOf(scan.licenseEvidence?.licenseUrl ?: "") }
    var evidenceDocRef by
      remember(record.id) { mutableStateOf(scan.licenseEvidence?.evidenceDocRef ?: "") }
    var notes by remember(record.id) { mutableStateOf(scan.licenseEvidence?.notes ?: "") }

    LaunchedEffect(scan.licenseEvidence?.updatedAt) {
      scan.licenseEvidence?.let { lic ->
        sourceName = lic.sourceName ?: ""
        licenseType = lic.licenseType ?: ""
        licensorName = lic.licensorName ?: ""
        allowedPlatforms = lic.allowedPlatforms ?: ""
        monetizationAllowed = lic.monetizationAllowed
        territory = lic.territory ?: ""
        validUntilText = lic.validUntilText ?: ""
        attributionRequired = lic.attributionRequired
        licenseUrl = lic.licenseUrl ?: ""
        evidenceDocRef = lic.evidenceDocRef ?: ""
        notes = lic.notes ?: ""
      }
    }

    Box(
      modifier = Modifier.fillMaxSize().padding(innerPadding),
      contentAlignment = Alignment.TopCenter,
    ) {
      LazyColumn(
        modifier = Modifier.fillMaxSize().widthIn(max = 760.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        // Summary Card
        item {
          CyberCard(borderColor = CyberCyan.copy(alpha = 0.4f)) {
            Column(
              modifier = Modifier.fillMaxWidth().padding(16.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                StatusBadge(status = status)
                if (record.isDemo) {
                  DemoBadge()
                }
              }
              Text(record.fileName, style = MaterialTheme.typography.titleLarge)
              Text(
                text = "ID: ${record.id} • ${Formatters.formatTimestamp(record.createdAt)}",
                style = MonoDataStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
              Text(
                text =
                  "Jenis: ${record.mediaKind} (${record.mimeType ?: "-"}) • Ukuran: ${Formatters.formatBytes(record.fileSizeBytes)} • Durasi: ${Formatters.formatDuration(record.durationMs)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
              Text(
                text = "Cakupan: ${record.scanCoverageDescription}",
                style = MaterialTheme.typography.bodySmall,
                color = CyberCyan,
              )
              Text(
                text = "Alasan Status: ${record.statusReason}",
                style = MaterialTheme.typography.bodyMedium,
              )
              if (!record.errorMessage.isNullOrBlank()) {
                Text(
                  text = "Catatan Teknis: ${record.errorMessage}",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.error,
                )
              }
            }
          }
        }

        // Track Matches Card
        item {
          CyberCard {
            Column(
              modifier = Modifier.fillMaxWidth().padding(16.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Icon(Icons.Filled.MusicNote, contentDescription = null, tint = CyberCyan)
                Text("Kandidat Lagu (Pengenalan Musik)", style = MaterialTheme.typography.titleMedium)
              }
              if (scan.matches.isEmpty()) {
                Text(
                  text =
                    "Belum ada kandidat lagu tercatat. Pengenalan audio hanya identitas awal, bukan bukti hak pakai, dan no-match bukan bukti audio bebas hak cipta.",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              } else {
                scan.matches.forEach { m ->
                  CyberCard(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest) {
                    Column(
                      modifier = Modifier.fillMaxWidth().padding(12.dp),
                      verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                      Text(m.title ?: "Judul tidak tersedia", style = MaterialTheme.typography.titleSmall)
                      Text(
                        "Artis: ${m.artist ?: "-"} • Album: ${m.album ?: "-"}",
                        style = MaterialTheme.typography.bodySmall,
                      )
                      Text(
                        "Label: ${m.label ?: "-"} • Provider: ${m.providerName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                      )
                      Text(
                        "Segmen: ${Formatters.formatDuration(m.segmentStartMs)} - ${Formatters.formatDuration(m.segmentEndMs)}",
                        style = MonoDataStyle,
                        color = CyberCyan,
                      )
                      if (!m.externalUrl.isNullOrBlank()) {
                        Text(
                          "Tautan: ${m.externalUrl}",
                          style = MaterialTheme.typography.bodySmall,
                          color = MaterialTheme.colorScheme.primary,
                        )
                      }
                    }
                  }
                }
              }
            }
          }
        }

        // License Evidence Form
        item {
          CyberCard {
            Column(
              modifier = Modifier.fillMaxWidth().padding(16.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              Text(
                text = "Pencatatan Bukti Lisensi (Manual)",
                style = MaterialTheme.typography.titleMedium,
              )
              Text(
                text =
                  "Aplikasi mencatat informasi lisensi yang Anda masukkan. Status dicatat sebagai bukti pengguna, bukan verifikasi hukum otomatis.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )

              OutlinedTextField(
                value = sourceName,
                onValueChange = { sourceName = it },
                label = { Text("Sumber Musik (mis. Epidemic Sound, YouTube Audio Library)") },
                modifier = Modifier.fillMaxWidth().testTag("license_source_input"),
                singleLine = true,
              )
              OutlinedTextField(
                value = licenseType,
                onValueChange = { licenseType = it },
                label = { Text("Jenis Lisensi (mis. Commercial Subscription, CC-BY 4.0)") },
                modifier = Modifier.fillMaxWidth().testTag("license_type_input"),
                singleLine = true,
              )
              OutlinedTextField(
                value = licensorName,
                onValueChange = { licensorName = it },
                label = { Text("Pemilik / Pemberi Lisensi") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
              )
              OutlinedTextField(
                value = allowedPlatforms,
                onValueChange = { allowedPlatforms = it },
                label = { Text("Platform Diizinkan") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
              )

              Text("Izin Monetisasi", style = MaterialTheme.typography.labelLarge)
              Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                  selected = monetizationAllowed == true,
                  onClick = { monetizationAllowed = true },
                  label = { Text("Diizinkan") },
                )
                FilterChip(
                  selected = monetizationAllowed == false,
                  onClick = { monetizationAllowed = false },
                  label = { Text("Tidak") },
                )
                FilterChip(
                  selected = monetizationAllowed == null,
                  onClick = { monetizationAllowed = null },
                  label = { Text("Belum Jelas") },
                )
              }

              OutlinedTextField(
                value = territory,
                onValueChange = { territory = it },
                label = { Text("Wilayah (Territory)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
              )
              OutlinedTextField(
                value = validUntilText,
                onValueChange = { validUntilText = it },
                label = { Text("Masa Berlaku Lisensi") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
              )

              Text("Kewajiban Atribusi", style = MaterialTheme.typography.labelLarge)
              Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                  selected = attributionRequired == true,
                  onClick = { attributionRequired = true },
                  label = { Text("Wajib") },
                )
                FilterChip(
                  selected = attributionRequired == false,
                  onClick = { attributionRequired = false },
                  label = { Text("Tidak Wajib") },
                )
              }

              OutlinedTextField(
                value = licenseUrl,
                onValueChange = { licenseUrl = it },
                label = { Text("URL Lisensi / Referensi") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
              )
              OutlinedTextField(
                value = evidenceDocRef,
                onValueChange = { evidenceDocRef = it },
                label = { Text("Nama File / Nomor Sertifikat Bukti") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
              )
              OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Catatan Tambahan") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
              )

              Button(
                onClick = {
                  onSaveLicenseEvidence(
                    record.id,
                    sourceName,
                    licenseType,
                    licensorName,
                    allowedPlatforms,
                    monetizationAllowed,
                    territory,
                    validUntilText,
                    attributionRequired,
                    licenseUrl,
                    evidenceDocRef,
                    notes,
                  )
                },
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("save_license_button"),
                colors =
                  ButtonDefaults.buttonColors(
                    containerColor = CyberCyan,
                    contentColor = CyberVoid,
                  ),
              ) {
                Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Simpan Bukti Lisensi")
              }
            }
          }
        }

        // Export Actions
        item {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            OutlinedButton(
              onClick = { createMdLauncher.launch("music_guard_${record.id}.md") },
              modifier = Modifier.weight(1f).height(48.dp).testTag("detail_export_md_button"),
            ) {
              Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Simpan .MD")
            }
            OutlinedButton(
              onClick = {
                val md = ReportGenerator.generateMarkdown(scan)
                val sendIntent =
                  Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, "Laporan Music Guard AI - ${record.fileName}")
                    putExtra(Intent.EXTRA_TEXT, md)
                  }
                context.startActivity(Intent.createChooser(sendIntent, "Bagikan Laporan"))
                onRecordExport(record.id, "SHARE", null)
              },
              modifier = Modifier.weight(1f).height(48.dp).testTag("detail_share_button"),
            ) {
              Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Bagikan")
            }
          }
        }

        item {
          DisclaimerBanner(
            body =
              "Catatan lisensi di atas merupakan dokumentasi pengguna. Selalu pastikan ketentuan lisensi berlaku dan pantau YouTube Studio Checks.",
          )
        }
      }
    }
  }
}
