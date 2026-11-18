package com.example.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Radar
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

enum class TopLevelTab(
  val route: String,
  val label: String,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector,
  val testTag: String,
) {
  DASHBOARD(
    route = "dashboard",
    label = "Dashboard",
    selectedIcon = Icons.Filled.Dashboard,
    unselectedIcon = Icons.Outlined.Dashboard,
    testTag = "nav_dashboard",
  ),
  SCAN(
    route = "scan",
    label = "Scan",
    selectedIcon = Icons.Filled.Radar,
    unselectedIcon = Icons.Outlined.Radar,
    testTag = "nav_scan",
  ),
  HISTORY(
    route = "history",
    label = "History",
    selectedIcon = Icons.Filled.History,
    unselectedIcon = Icons.Outlined.History,
    testTag = "nav_history",
  ),
  REPORTS(
    route = "reports",
    label = "Reports",
    selectedIcon = Icons.Filled.Description,
    unselectedIcon = Icons.Outlined.Description,
    testTag = "nav_reports",
  ),
  SETTINGS(
    route = "settings",
    label = "Settings",
    selectedIcon = Icons.Filled.Settings,
    unselectedIcon = Icons.Outlined.Settings,
    testTag = "nav_settings",
  ),
}

object MusicGuardRoutes {
  const val SCAN_WITH_MODE = "scan?mode={mode}"
  const val RESULT_DETAIL = "result/{scanId}"

  fun scanRoute(mode: String? = null): String =
    if (mode.isNullOrBlank()) TopLevelTab.SCAN.route else "scan?mode=$mode"

  fun resultDetailRoute(scanId: String): String = "result/$scanId"
}
