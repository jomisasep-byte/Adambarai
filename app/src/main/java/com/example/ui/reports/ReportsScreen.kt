package com.example.ui.reports

import android.content.Intent
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.ScanWithDetails
import com.example.export.ReportGenerator
import com.example.ui.components.CyberCard
import com.example.ui.components.DemoBadge
import com.example.ui.components.EmptyStateView
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberVoid
import com.example.ui.theme.MonoDataStyle

@Composable
fun ReportsScreen(
  scans: List<ScanWithDetails>,
  onGoToScan: () -> Unit,
  onLoadDemoPreview: () -> Unit,
  onRecordExport: (scanId: String, format: String, uri: String?) -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  var selectedScanId by remember(scans) { mutableStateOf(scans.firstOrNull()?.record?.id) }
  var selectedFormat by remember { mutableStateOf("MARKDOWN") }

  val selectedScan =
    remember(scans, selectedScanId) {
      scans.firstOrNull { it.record.id == selectedScanId } ?: scans.firstOrNull()
    }

  val previewContent =
    remember(selectedScan, selectedFormat) {
      selectedScan?.let {
        if (selectedFormat == "JSON") ReportGenerator.generateJson(it)
        else ReportGenerator.generateMarkdown(it)
      } ?: ""
    }

  val saveMdLauncher =
    rememberLauncherForActivityResult(
      contract = ActivityResultContracts.CreateDocument("text/markdown"),
      onResult = { uri ->
        if (uri != null && selectedScan != null) {
          runCatching {
            context.contentResolver.openOutputStream(uri)?.use { out ->
              out.write(ReportGenerator.generateMarkdown(selectedScan).toByteArray(Charsets.UTF_8))
            }
          }
          onRecordExport(selectedScan.record.id, "MARKDOWN", uri.toString())
        }
      },
    )

  val saveJsonLauncher =
    rememberLauncherForActivityResult(
      contract = ActivityResultContracts.CreateDocument("application/json"),
      onResult = { uri ->
        if (uri != null && selectedScan != null) {
          runCatching {
            context.contentResolver.openOutputStream(uri)?.use { out ->
              out.write(ReportGenerator.generateJson(selectedScan).toByteArray(Charsets.UTF_8))
            }
          }
          onRecordExport(selectedScan.record.id, "JSON", uri.toString())
        }
      },
    )

  Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
    LazyColumn(
      modifier = Modifier.fillMaxSize().widthIn(max = 760.dp),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      item {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Text(
            text = "Ekspor Laporan",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
          )
          Text(
            text = "Buat dokumentasi pra-publikasi dalam format Markdown (.md) atau JSON (.json).",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }

      if (selectedScan == null) {
        item {
          EmptyStateView(
            icon = Icons.Filled.Description,
            title = "Belum Ada Data Laporan",
            description =
              "Simpan hasil inspeksi media terlebih dahulu atau muat contoh DEMO untuk menguji pratinjau ekspor Markdown & JSON.",
            actionLabel = "Ke Halaman Scan",
            onAction = onGoToScan,
            secondaryActionLabel = "Muat Contoh Preview (DEMO)",
            onSecondaryAction = onLoadDemoPreview,
          )
        }
      } else {
        item {
          Text("Pilih Catatan Pemeriksaan", style = MaterialTheme.typography.titleSmall)
          Spacer(modifier = Modifier.height(8.dp))
          LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(scans, key = { it.record.id }) { item ->
              FilterChip(
                selected = selectedScan.record.id == item.record.id,
                onClick = { selectedScanId = item.record.id },
                label = {
                  Text(
                    text =
                      if (item.record.isDemo) "[DEMO] ${item.record.fileName}"
                      else item.record.fileName,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                  )
                },
              )
            }
          }
        }

        item {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            FilterChip(
              selected = selectedFormat == "MARKDOWN",
              onClick = { selectedFormat = "MARKDOWN" },
              label = { Text("Markdown (.md)") },
              modifier = Modifier.testTag("format_markdown_chip"),
            )
            FilterChip(
              selected = selectedFormat == "JSON",
              onClick = { selectedFormat = "JSON" },
              label = { Text("JSON (.json)") },
              modifier = Modifier.testTag("format_json_chip"),
            )
            if (selectedScan.record.isDemo) {
              DemoBadge()
            }
          }
        }

        item {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Button(
              onClick = {
                val baseName = "music_guard_${selectedScan.record.id}"
                if (selectedFormat == "JSON") {
                  saveJsonLauncher.launch("$baseName.json")
                } else {
                  saveMdLauncher.launch("$baseName.md")
                }
              },
              modifier = Modifier.weight(1f).height(48.dp).testTag("export_save_saf_button"),
              colors =
                ButtonDefaults.buttonColors(
                  containerColor = CyberCyan,
                  contentColor = CyberVoid,
                ),
            ) {
              Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Simpan File")
            }
            OutlinedButton(
              onClick = {
                val sendIntent =
                  Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(
                      Intent.EXTRA_SUBJECT,
                      "Music Guard AI Report - ${selectedScan.record.fileName}",
                    )
                    putExtra(Intent.EXTRA_TEXT, previewContent)
                  }
                context.startActivity(Intent.createChooser(sendIntent, "Bagikan Laporan"))
                onRecordExport(selectedScan.record.id, "SHARE_$selectedFormat", null)
              },
              modifier = Modifier.weight(1f).height(48.dp).testTag("export_share_button"),
            ) {
              Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Bagikan")
            }
          }
        }

        item {
          CyberCard(borderColor = CyberCyan.copy(alpha = 0.3f)) {
            Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
              Text(
                text = "Pratinjau Output ($selectedFormat)",
                style = MaterialTheme.typography.titleSmall,
                color = CyberCyan,
              )
              Spacer(modifier = Modifier.height(10.dp))
              Text(
                text = previewContent,
                style = MonoDataStyle,
                color = MaterialTheme.colorScheme.onSurface,
              )
            }
          }
        }
      }
    }
  }
}
