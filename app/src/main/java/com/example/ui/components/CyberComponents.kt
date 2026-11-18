package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.RecognitionProviderState
import com.example.data.model.ScanStatus
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberError
import com.example.ui.theme.CyberErrorContainer
import com.example.ui.theme.CyberInfo
import com.example.ui.theme.CyberInfoContainer
import com.example.ui.theme.CyberSuccess
import com.example.ui.theme.CyberSuccessContainer
import com.example.ui.theme.CyberViolet
import com.example.ui.theme.CyberWarning
import com.example.ui.theme.CyberWarningContainer
import com.example.ui.theme.MonoDataStyle

@Composable
fun CyberCard(
  modifier: Modifier = Modifier,
  borderColor: Color = MaterialTheme.colorScheme.outline,
  containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
  onClick: (() -> Unit)? = null,
  content: @Composable ColumnScope.() -> Unit,
) {
  if (onClick != null) {
    Card(
      onClick = onClick,
      modifier = modifier,
      shape = MaterialTheme.shapes.medium,
      colors = CardDefaults.cardColors(containerColor = containerColor),
      border = BorderStroke(1.dp, borderColor),
      content = content,
    )
  } else {
    Card(
      modifier = modifier,
      shape = MaterialTheme.shapes.medium,
      colors = CardDefaults.cardColors(containerColor = containerColor),
      border = BorderStroke(1.dp, borderColor),
      content = content,
    )
  }
}

@Composable
fun StatusBadge(
  status: ScanStatus,
  modifier: Modifier = Modifier,
) {
  val (bg, fg, icon) =
    when (status) {
      ScanStatus.NEEDS_VERIFICATION -> Triple(CyberWarningContainer, CyberWarning, Icons.Filled.Warning)
      ScanStatus.UNKNOWN -> Triple(CyberInfoContainer, CyberInfo, Icons.Filled.HelpOutline)
      ScanStatus.NEEDS_ATTENTION -> Triple(CyberErrorContainer, CyberWarning, Icons.Filled.Shield)
      ScanStatus.FAILED -> Triple(CyberErrorContainer, CyberError, Icons.Filled.Error)
    }

  Surface(
    modifier = modifier,
    shape = MaterialTheme.shapes.extraSmall,
    color = bg,
    border = BorderStroke(1.dp, fg.copy(alpha = 0.45f)),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Icon(
        imageVector = icon,
        contentDescription = status.labelId,
        tint = fg,
        modifier = Modifier.size(14.dp),
      )
      Text(
        text = status.labelId,
        style = MaterialTheme.typography.labelSmall,
        color = fg,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

@Composable
fun DemoBadge(modifier: Modifier = Modifier) {
  Surface(
    modifier = modifier,
    shape = MaterialTheme.shapes.extraSmall,
    color = CyberViolet.copy(alpha = 0.18f),
    border = BorderStroke(1.dp, CyberViolet.copy(alpha = 0.6f)),
  ) {
    Text(
      text = "DEMO PREVIEW",
      style = MaterialTheme.typography.labelSmall,
      color = CyberViolet,
      modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
    )
  }
}

@Composable
fun ProviderStatusPill(
  state: RecognitionProviderState,
  modifier: Modifier = Modifier,
) {
  val isReady = state == RecognitionProviderState.CONFIGURED_PROXY
  val fg = if (isReady) CyberSuccess else CyberWarning
  val bg = if (isReady) CyberSuccessContainer else CyberWarningContainer
  val icon = if (isReady) Icons.Filled.CheckCircle else Icons.Filled.Info

  Surface(
    modifier = modifier,
    shape = MaterialTheme.shapes.small,
    color = bg,
    border = BorderStroke(1.dp, fg.copy(alpha = 0.45f)),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Box(
        modifier =
          Modifier.size(8.dp)
            .clip(CircleShape)
            .background(fg)
      )
      Icon(
        imageVector = icon,
        contentDescription = "Status provider pengenalan musik",
        tint = fg,
        modifier = Modifier.size(16.dp),
      )
      Text(
        text = "AudD: ${state.labelId}",
        style = MonoDataStyle,
        color = fg,
      )
    }
  }
}

@Composable
fun MetricTile(
  label: String,
  value: String,
  subtitle: String,
  icon: ImageVector,
  accentColor: Color = CyberCyan,
  modifier: Modifier = Modifier,
) {
  CyberCard(
    modifier = modifier,
    borderColor = accentColor.copy(alpha = 0.35f),
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Text(
          text = label,
          style = MaterialTheme.typography.labelLarge,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        Icon(
          imageVector = icon,
          contentDescription = label,
          tint = accentColor,
          modifier = Modifier.size(20.dp),
        )
      }
      Spacer(modifier = Modifier.height(10.dp))
      Text(
        text = value,
        style = MaterialTheme.typography.displaySmall,
        color = MaterialTheme.colorScheme.onSurface,
        fontWeight = FontWeight.Bold,
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

@Composable
fun DisclaimerBanner(
  title: String = "Batasan Pemeriksaan Hak Cipta",
  body: String,
  modifier: Modifier = Modifier,
) {
  CyberCard(
    modifier = modifier.fillMaxWidth(),
    borderColor = CyberViolet.copy(alpha = 0.45f),
    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
  ) {
    Row(
      modifier = Modifier.padding(14.dp),
      verticalAlignment = Alignment.Top,
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Box(
        modifier =
          Modifier.size(36.dp)
            .clip(MaterialTheme.shapes.small)
            .background(CyberViolet.copy(alpha = 0.16f))
            .border(1.dp, CyberViolet.copy(alpha = 0.45f), MaterialTheme.shapes.small),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = Icons.Filled.Security,
          contentDescription = "Peringatan batasan copyright",
          tint = CyberViolet,
          modifier = Modifier.size(20.dp),
        )
      }
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = title,
          style = MaterialTheme.typography.titleSmall,
          color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = body,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
}

@Composable
fun EmptyStateView(
  icon: ImageVector,
  title: String,
  description: String,
  actionLabel: String? = null,
  onAction: (() -> Unit)? = null,
  actionTestTag: String? = null,
  secondaryActionLabel: String? = null,
  onSecondaryAction: (() -> Unit)? = null,
  secondaryTestTag: String? = null,
  modifier: Modifier = Modifier,
) {
  CyberCard(
    modifier = modifier.fillMaxWidth(),
    borderColor = MaterialTheme.colorScheme.outline,
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Box(
        modifier =
          Modifier.size(56.dp)
            .clip(CircleShape)
            .background(CyberCyan.copy(alpha = 0.12f))
            .border(1.dp, CyberCyan.copy(alpha = 0.35f), CircleShape),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = icon,
          contentDescription = title,
          tint = CyberCyan,
          modifier = Modifier.size(28.dp),
        )
      }
      Spacer(modifier = Modifier.height(14.dp))
      Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
      )
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = description,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      if (actionLabel != null && onAction != null) {
        Spacer(modifier = Modifier.height(16.dp))
        Button(
          onClick = onAction,
          modifier =
            Modifier.then(if (actionTestTag != null) Modifier.testTag(actionTestTag) else Modifier),
        ) {
          Text(actionLabel)
        }
      }
      if (secondaryActionLabel != null && onSecondaryAction != null) {
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
          onClick = onSecondaryAction,
          modifier =
            Modifier.then(
              if (secondaryTestTag != null) Modifier.testTag(secondaryTestTag) else Modifier
            ),
        ) {
          Text(secondaryActionLabel)
        }
      }
    }
  }
}

@Composable
fun RecognitionConsentDialog(
  fileName: String,
  providerName: String,
  onConfirm: () -> Unit,
  onDismiss: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    icon = {
      Icon(
        imageVector = Icons.Filled.VerifiedUser,
        contentDescription = "Persetujuan pengiriman sampel",
        tint = CyberCyan,
      )
    },
    title = { Text("Konfirmasi Pengiriman Sampel") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
          "Sebelum mengirim sampel audio ke layanan eksternal, pastikan Anda menyetujui pengiriman berikut:",
          style = MaterialTheme.typography.bodyMedium,
        )
        Text(
          "• File: $fileName\n• Sampel: Segmen audio pendek (bukan file video penuh)\n• Provider tujuan: $providerName\n• Tujuan: Pengenalan kandidat lagu pra-publikasi",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    },
    confirmButton = {
      Button(
        onClick = onConfirm,
        modifier = Modifier.testTag("confirm_upload_consent_button"),
      ) {
        Text("Lanjutkan Cek")
      }
    },
    dismissButton = {
      TextButton(
        onClick = onDismiss,
        modifier = Modifier.testTag("cancel_upload_consent_button"),
      ) {
        Text("Batal")
      }
    },
  )
}
