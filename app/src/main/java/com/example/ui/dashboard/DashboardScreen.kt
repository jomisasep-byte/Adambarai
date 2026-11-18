package com.example.ui.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.ScanWithDetails
import com.example.data.model.RecognitionProviderState
import com.example.data.model.ScanStatus
import com.example.export.Formatters
import com.example.ui.components.CyberCard
import com.example.ui.components.DemoBadge
import com.example.ui.components.DisclaimerBanner
import com.example.ui.components.EmptyStateView
import com.example.ui.components.MetricTile
import com.example.ui.components.ProviderStatusPill
import com.example.ui.components.StatusBadge
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberNavy
import com.example.ui.theme.CyberViolet
import com.example.ui.theme.CyberVoid
import com.example.ui.theme.CyberWarning
import com.example.ui.theme.MonoDataStyle

@Composable
fun DashboardScreen(
  scans: List<ScanWithDetails>,
  providerState: RecognitionProviderState,
  onScanVideoClick: () -> Unit,
  onScanAudioClick: () -> Unit,
  onOpenScanDetail: (String) -> Unit,
  onViewAllHistory: () -> Unit,
  onLoadDemoPreview: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val realScans = scans.filter { !it.record.isDemo }
  val needsVerificationCount =
    realScans.count {
      it.record.scanStatus == ScanStatus.NEEDS_VERIFICATION.name ||
        it.record.scanStatus == ScanStatus.NEEDS_ATTENTION.name
    }
  val recentScans = scans.take(4)

  Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
    LazyColumn(
      modifier = Modifier.fillMaxSize().widthIn(max = 760.dp),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      // Hero Branding Card
      item {
        CyberCard(
          borderColor = CyberCyan.copy(alpha = 0.4f),
          containerColor = CyberNavy,
        ) {
          Box(modifier = Modifier.fillMaxWidth()) {
            Image(
              painter = painterResource(id = R.drawable.img_cyber_guard_banner_1795014256352),
              contentDescription = "Latar spektrum audio cyber Music Guard AI",
              contentScale = ContentScale.Crop,
              alpha = 0.28f,
              modifier = Modifier.matchParentSize(),
            )
            Box(
              modifier =
                Modifier.matchParentSize()
                  .background(
                    Brush.verticalGradient(
                      colors = listOf(CyberVoid.copy(alpha = 0.45f), CyberNavy.copy(alpha = 0.94f))
                    )
                  )
            )
            Column(modifier = Modifier.padding(18.dp)) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
              ) {
                Box(
                  modifier =
                    Modifier.size(40.dp)
                      .clip(MaterialTheme.shapes.small)
                      .background(CyberCyan.copy(alpha = 0.16f)),
                  contentAlignment = Alignment.Center,
                ) {
                  Icon(
                    imageVector = Icons.Filled.Shield,
                    contentDescription = "Ikon Music Guard AI",
                    tint = CyberCyan,
                    modifier = Modifier.size(24.dp),
                  )
                }
                Column {
                  Text(
                    text = stringResource(R.string.app_brand_display),
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                  )
                  Text(
                    text = stringResource(R.string.app_tagline),
                    style = MonoDataStyle,
                    color = CyberCyan,
                  )
                }
              }

              Spacer(modifier = Modifier.height(10.dp))
              Text(
                text = stringResource(R.string.app_subtitle_id),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )

              Spacer(modifier = Modifier.height(14.dp))
              ProviderStatusPill(
                state = providerState,
                modifier = Modifier.testTag("dashboard_provider_status"),
              )

              Spacer(modifier = Modifier.height(16.dp))
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
              ) {
                Button(
                  onClick = onScanVideoClick,
                  modifier = Modifier.weight(1f).height(50.dp).testTag("scan_video_button"),
                  colors =
                    ButtonDefaults.buttonColors(
                      containerColor = CyberCyan,
                      contentColor = CyberVoid,
                    ),
                ) {
                  Icon(
                    imageVector = Icons.Filled.VideoFile,
                    contentDescription = "Scan Video",
                    modifier = Modifier.size(18.dp),
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text("Scan Video", maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                FilledTonalButton(
                  onClick = onScanAudioClick,
                  modifier = Modifier.weight(1f).height(50.dp).testTag("scan_audio_button"),
                  colors =
                    ButtonDefaults.filledTonalButtonColors(
                      containerColor = CyberViolet.copy(alpha = 0.24f),
                      contentColor = Color(0xFFE9D5FF),
                    ),
                ) {
                  Icon(
                    imageVector = Icons.Filled.AudioFile,
                    contentDescription = "Scan Audio",
                    modifier = Modifier.size(18.dp),
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text("Scan Audio", maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
              }
            }
          }
        }
      }

      // Honest Metrics Row (Real count only, zero fake stats)
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          MetricTile(
            label = "Total Pemeriksaan",
            value = realScans.size.toString(),
            subtitle =
              if (realScans.isEmpty()) "Belum ada inspeksi nyata"
              else "Tersimpan lokal di perangkat",
            icon = Icons.Filled.FactCheck,
            accentColor = CyberCyan,
            modifier = Modifier.weight(1f).testTag("metric_total_scans"),
          )
          MetricTile(
            label = "Perlu Verifikasi",
            value = needsVerificationCount.toString(),
            subtitle = "Lisensi / hak perlu dicek",
            icon = Icons.Filled.Warning,
            accentColor = CyberWarning,
            modifier = Modifier.weight(1f).testTag("metric_needs_verification"),
          )
        }
      }

      // Recent Scans Header
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = "Pemeriksaan Terbaru",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
          )
          if (scans.isNotEmpty()) {
            TextButton(
              onClick = onViewAllHistory,
              modifier = Modifier.testTag("dashboard_view_all_history"),
            ) {
              Text("Lihat Semua")
            }
          }
        }
      }

      if (recentScans.isEmpty()) {
        item {
          EmptyStateView(
            icon = Icons.Filled.Radar,
            title = "Belum Ada Riwayat Pemeriksaan",
            description =
              "Pilih file video atau audio untuk memeriksa metadata & track audio, atau muat contoh pratinjau berlabel DEMO.",
            actionLabel = "Mulai Scan Media",
            onAction = onScanVideoClick,
            actionTestTag = "empty_start_scan_button",
            secondaryActionLabel = "Muat Contoh Preview (DEMO)",
            onSecondaryAction = onLoadDemoPreview,
            secondaryTestTag = "load_demo_preview_button",
          )
        }
      } else {
        items(recentScans, key = { it.record.id }) { item ->
          val status =
            runCatching { ScanStatus.valueOf(item.record.scanStatus) }.getOrDefault(ScanStatus.UNKNOWN)
          CyberCard(
            onClick = { onOpenScanDetail(item.record.id) },
            modifier = Modifier.fillMaxWidth().testTag("recent_scan_card_${item.record.id}"),
          ) {
            Row(
              modifier = Modifier.fillMaxWidth().padding(14.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                  StatusBadge(status = status)
                  if (item.record.isDemo) {
                    DemoBadge()
                  }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                  text = item.record.fileName,
                  style = MaterialTheme.typography.titleSmall,
                  color = MaterialTheme.colorScheme.onSurface,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text =
                    "${item.record.mediaKind} • ${Formatters.formatDuration(item.record.durationMs)} • ${Formatters.formatTimestamp(item.record.createdAt)}",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
              }
              Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = "Buka detail pemeriksaan",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
        }
      }

      // Disclaimer Card
      item {
        DisclaimerBanner(
          title = stringResource(R.string.disclaimer_title),
          body = stringResource(R.string.disclaimer_short),
        )
      }
    }
  }
}
