package com.example.ui.settings

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.BuildConfig
import com.example.R
import com.example.data.local.AppSettings
import com.example.data.model.RecognitionProviderState
import com.example.data.model.ThemePreference
import com.example.ui.components.CyberCard
import com.example.ui.components.DisclaimerBanner
import com.example.ui.components.ProviderStatusPill
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberViolet
import com.example.ui.theme.MonoDataStyle

@Composable
fun SettingsScreen(
  settings: AppSettings,
  providerState: RecognitionProviderState,
  onSaveProxyUrl: (String) -> Unit,
  onThemeChange: (ThemePreference) -> Unit,
  onToggleConsent: (Boolean) -> Unit,
  onClearDemoData: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var proxyInput by remember(settings.backendProxyUrl) { mutableStateOf(settings.backendProxyUrl) }
  var proxyValidationError by remember { mutableStateOf<String?>(null) }

  Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
    LazyColumn(
      modifier = Modifier.fillMaxSize().widthIn(max = 760.dp),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      item {
        Text(
          text = "Pengaturan & Konfigurasi",
          style = MaterialTheme.typography.headlineSmall,
          color = MaterialTheme.colorScheme.onBackground,
        )
      }

      // Provider & Backend Security Card
      item {
        CyberCard(borderColor = CyberCyan.copy(alpha = 0.35f)) {
          Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Icon(Icons.Filled.CloudOff, contentDescription = null, tint = CyberCyan)
              Text("Provider Pengenalan Musik", style = MaterialTheme.typography.titleMedium)
            }
            ProviderStatusPill(state = providerState)
            Text(
              text =
                "Provider target: AudD (https://api.audd.io/). Demi keamanan, API token AudD tidak boleh disimpan di dalam APK. Gunakan endpoint proxy HTTPS milik Anda yang menyimpan token secara server-side.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedTextField(
              value = proxyInput,
              onValueChange = {
                proxyInput = it
                proxyValidationError = null
              },
              label = { Text("URL Backend Proxy HTTPS (Opsional untuk Tahap 4)") },
              placeholder = { Text("https://api.domainanda.com/recognize") },
              isError = proxyValidationError != null,
              supportingText =
                proxyValidationError?.let { err -> { Text(err) } },
              modifier = Modifier.fillMaxWidth().testTag("proxy_url_input"),
              singleLine = true,
            )
            Button(
              onClick = {
                val trimmed = proxyInput.trim()
                if (trimmed.isNotEmpty() && !trimmed.startsWith("https://")) {
                  proxyValidationError = "Endpoint proxy harus menggunakan HTTPS (https://...)."
                } else {
                  onSaveProxyUrl(trimmed)
                }
              },
              modifier = Modifier.fillMaxWidth().height(48.dp).testTag("save_proxy_url_button"),
            ) {
              Text("Simpan Konfigurasi Endpoint")
            }
          }
        }
      }

      // Privacy & Consent Card
      item {
        CyberCard {
          Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Icon(Icons.Filled.Lock, contentDescription = null, tint = CyberViolet)
              Text("Privasi & Persetujuan", style = MaterialTheme.typography.titleMedium)
            }
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text("Konfirmasi Sebelum Kirim Sampel", style = MaterialTheme.typography.titleSmall)
                Text(
                  "Selalu tampilkan dialog persetujuan sebelum mengunggah sampel audio ke layanan pihak ketiga.",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
              Switch(
                checked = settings.requireUploadConsent,
                onCheckedChange = onToggleConsent,
                modifier = Modifier.testTag("consent_switch"),
              )
            }
            Text(
              text =
                "Kebijakan Data Lokal: Metadata pemeriksaan dan bukti lisensi disimpan lokal di perangkat (Room SQLite). Aplikasi tidak menyimpan file media mentah secara permanen dan membersihkan cache sementara setelah pemrosesan.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedButton(
              onClick = onClearDemoData,
              modifier = Modifier.fillMaxWidth().testTag("clear_demo_data_button"),
            ) {
              Text("Hapus Semua Data Contoh (DEMO)")
            }
          }
        }
      }

      // Theme Card
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
              Icon(Icons.Filled.Palette, contentDescription = null, tint = CyberCyan)
              Text("Tampilan Aplikasi", style = MaterialTheme.typography.titleMedium)
            }
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              ThemePreference.entries.forEach { pref ->
                FilterChip(
                  selected = settings.themePreference == pref,
                  onClick = { onThemeChange(pref) },
                  label = { Text(pref.labelId) },
                )
              }
            }
          }
        }
      }

      // About & Version Card
      item {
        CyberCard {
          Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Icon(Icons.Filled.Info, contentDescription = null, tint = CyberCyan)
              Text("Tentang Aplikasi", style = MaterialTheme.typography.titleMedium)
            }
            Text(
              text = "MUSIC GUARD AI — v${BuildConfig.VERSION_NAME}",
              style = MonoDataStyle,
              color = CyberCyan,
            )
            Text(
              text =
                "Tahap 1 aktif: Fondasi UI Cyber, Navigasi 5 menu, Inspeksi Metadata & Track Audio SAF, Penyimpanan Riwayat Room, Form Bukti Lisensi, serta Ekspor Markdown/JSON.\n" +
                  "Tahap berikutnya: Ekstraksi & segmentasi sampel audio (Tahap 3) dan integrasi HTTPS proxy AudD (Tahap 4).",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }

      item {
        DisclaimerBanner(
          title = stringResource(R.string.disclaimer_title),
          body = stringResource(R.string.disclaimer_full),
        )
      }
    }
  }
}
