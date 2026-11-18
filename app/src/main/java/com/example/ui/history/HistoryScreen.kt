package com.example.ui.history

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.ScanWithDetails
import com.example.data.model.ScanStatus
import com.example.export.Formatters
import com.example.ui.components.CyberCard
import com.example.ui.components.DemoBadge
import com.example.ui.components.EmptyStateView
import com.example.ui.components.StatusBadge
import com.example.ui.theme.CyberError

@Composable
fun HistoryScreen(
  scans: List<ScanWithDetails>,
  onOpenScanDetail: (String) -> Unit,
  onDeleteScan: (String) -> Unit,
  onDeleteAllScans: () -> Unit,
  onGoToScan: () -> Unit,
  onLoadDemoPreview: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var searchQuery by remember { mutableStateOf("") }
  var selectedStatusFilter by remember { mutableStateOf<ScanStatus?>(null) }
  var showClearAllDialog by remember { mutableStateOf(false) }

  val filteredScans =
    remember(scans, searchQuery, selectedStatusFilter) {
      scans.filter { item ->
        val matchesStatus =
          selectedStatusFilter == null || item.record.scanStatus == selectedStatusFilter?.name
        val q = searchQuery.trim().lowercase()
        val matchesQuery =
          q.isEmpty() ||
            item.record.fileName.lowercase().contains(q) ||
            item.record.id.lowercase().contains(q) ||
            item.matches.any {
              (it.title?.lowercase()?.contains(q) == true) ||
                (it.artist?.lowercase()?.contains(q) == true)
            }
        matchesStatus && matchesQuery
      }
    }

  if (showClearAllDialog) {
    AlertDialog(
      onDismissRequest = { showClearAllDialog = false },
      icon = {
        Icon(Icons.Filled.DeleteSweep, contentDescription = null, tint = CyberError)
      },
      title = { Text("Hapus Seluruh Riwayat?") },
      text = {
        Text(
          "Semua catatan pemeriksaan, kandidat lagu, bukti lisensi, dan log laporan lokal akan dihapus permanen."
        )
      },
      confirmButton = {
        Button(
          onClick = {
            showClearAllDialog = false
            onDeleteAllScans()
          },
          colors = ButtonDefaults.buttonColors(containerColor = CyberError),
          modifier = Modifier.testTag("confirm_clear_all_history_button"),
        ) {
          Text("Hapus Semua")
        }
      },
      dismissButton = {
        TextButton(onClick = { showClearAllDialog = false }) { Text("Batal") }
      },
    )
  }

  Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
    LazyColumn(
      modifier = Modifier.fillMaxSize().widthIn(max = 760.dp),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Column {
            Text(
              text = "Riwayat Pemeriksaan",
              style = MaterialTheme.typography.headlineSmall,
              color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
              text = "Tersimpan persisten di database lokal perangkat",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
          if (scans.isNotEmpty()) {
            TextButton(
              onClick = { showClearAllDialog = true },
              modifier = Modifier.testTag("clear_all_history_button"),
            ) {
              Icon(
                Icons.Filled.DeleteSweep,
                contentDescription = "Hapus seluruh riwayat",
                tint = CyberError,
                modifier = Modifier.size(18.dp),
              )
              Text(" Hapus", color = CyberError)
            }
          }
        }
      }

      if (scans.isNotEmpty()) {
        item {
          OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth().testTag("history_search_input"),
            placeholder = { Text("Cari nama file, ID, judul, atau artis...") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Cari") },
            trailingIcon = {
              if (searchQuery.isNotEmpty()) {
                IconButton(onClick = { searchQuery = "" }) {
                  Icon(Icons.Filled.Clear, contentDescription = "Hapus kata kunci")
                }
              }
            },
            singleLine = true,
          )
        }

        item {
          LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
              FilterChip(
                selected = selectedStatusFilter == null,
                onClick = { selectedStatusFilter = null },
                label = { Text("Semua (${scans.size})") },
              )
            }
            items(ScanStatus.entries) { status ->
              val count = scans.count { it.record.scanStatus == status.name }
              FilterChip(
                selected = selectedStatusFilter == status,
                onClick = {
                  selectedStatusFilter = if (selectedStatusFilter == status) null else status
                },
                label = { Text("${status.labelId} ($count)") },
              )
            }
          }
        }
      }

      if (filteredScans.isEmpty()) {
        item {
          EmptyStateView(
            icon = Icons.Filled.History,
            title = if (scans.isEmpty()) "Riwayat Masih Kosong" else "Tidak Ada Hasil Cocok",
            description =
              if (scans.isEmpty()) {
                "Pemeriksaan media yang Anda simpan akan muncul di sini dan tetap tersimpan saat aplikasi ditutup."
              } else {
                "Ubah kata kunci pencarian atau filter status."
              },
            actionLabel = if (scans.isEmpty()) "Ke Halaman Scan" else null,
            onAction = if (scans.isEmpty()) onGoToScan else null,
            secondaryActionLabel = if (scans.isEmpty()) "Muat Contoh Preview (DEMO)" else null,
            onSecondaryAction = if (scans.isEmpty()) onLoadDemoPreview else null,
          )
        }
      } else {
        items(filteredScans, key = { it.record.id }) { item ->
          val status =
            runCatching { ScanStatus.valueOf(item.record.scanStatus) }.getOrDefault(ScanStatus.UNKNOWN)
          CyberCard(
            onClick = { onOpenScanDetail(item.record.id) },
            modifier = Modifier.fillMaxWidth().testTag("history_item_${item.record.id}"),
          ) {
            Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                  modifier = Modifier.weight(1f),
                ) {
                  StatusBadge(status = status)
                  if (item.record.isDemo) {
                    DemoBadge()
                  }
                }
                IconButton(
                  onClick = { onDeleteScan(item.record.id) },
                  modifier = Modifier.size(48.dp).testTag("delete_scan_${item.record.id}"),
                ) {
                  Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Hapus catatan ${item.record.fileName}",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
              }

              Text(
                text = item.record.fileName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text =
                  "${item.record.id} • ${item.record.mediaKind} • ${Formatters.formatBytes(item.record.fileSizeBytes)} • ${Formatters.formatDuration(item.record.durationMs)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = item.record.scanCoverageDescription,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
              )
              if (item.licenseEvidence != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = "Bukti lisensi dicatat: ${item.licenseEvidence.sourceName ?: "Tersimpan"}",
                  style = MaterialTheme.typography.labelSmall,
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
