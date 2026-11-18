package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.model.ThemePreference
import com.example.navigation.MusicGuardRoutes
import com.example.navigation.TopLevelTab
import com.example.ui.MusicGuardViewModel
import com.example.ui.dashboard.DashboardScreen
import com.example.ui.history.HistoryScreen
import com.example.ui.reports.ReportsScreen
import com.example.ui.result.ResultDetailScreen
import com.example.ui.scan.ScanScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.MusicGuardTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    val app = application as MusicGuardApplication
    val factory =
      MusicGuardViewModel.Factory(
        repository = app.repository,
        settingsDataStore = app.settingsDataStore,
        mediaInspector = app.mediaInspector,
        recognitionProvider = app.recognitionProvider,
      )

    setContent {
      val viewModel: MusicGuardViewModel = viewModel(factory = factory)
      val settings by viewModel.settings.collectAsStateWithLifecycle()
      val isDark =
        when (settings.themePreference) {
          ThemePreference.CYBER_DARK -> true
          ThemePreference.LIGHT -> false
          ThemePreference.SYSTEM -> isSystemInDarkTheme()
        }

      MusicGuardTheme(darkTheme = isDark) {
        MusicGuardAppRoot(viewModel = viewModel)
      }
    }
  }
}

@Composable
private fun MusicGuardAppRoot(viewModel: MusicGuardViewModel) {
  val navController = rememberNavController()
  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = navBackStackEntry?.destination?.route

  val scans by viewModel.scans.collectAsStateWithLifecycle()
  val settings by viewModel.settings.collectAsStateWithLifecycle()
  val scanState by viewModel.scanState.collectAsStateWithLifecycle()
  val selectedMode by viewModel.selectedMediaMode.collectAsStateWithLifecycle()
  val showConsentDialog by viewModel.showConsentDialog.collectAsStateWithLifecycle()
  val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()
  val providerState = viewModel.providerState(settings)

  val snackbarHostState = remember { SnackbarHostState() }
  LaunchedEffect(statusMessage) {
    val msg = statusMessage
    if (!msg.isNullOrBlank()) {
      snackbarHostState.showSnackbar(msg)
      viewModel.clearStatusMessage()
    }
  }

  val isTopLevelRoute =
    currentRoute == TopLevelTab.DASHBOARD.route ||
      currentRoute == TopLevelTab.SCAN.route ||
      currentRoute == MusicGuardRoutes.SCAN_WITH_MODE ||
      currentRoute == TopLevelTab.HISTORY.route ||
      currentRoute == TopLevelTab.REPORTS.route ||
      currentRoute == TopLevelTab.SETTINGS.route

  fun navigateToTopLevel(tab: TopLevelTab) {
    val target = if (tab == TopLevelTab.SCAN) MusicGuardRoutes.scanRoute() else tab.route
    navController.navigate(target) {
      popUpTo(navController.graph.findStartDestination().id) { saveState = true }
      launchSingleTop = true
      restoreState = true
    }
  }

  BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
    val useNavigationRail = maxWidth >= 600.dp

    Row(modifier = Modifier.fillMaxSize()) {
      if (useNavigationRail && isTopLevelRoute) {
        NavigationRail(
          containerColor = MaterialTheme.colorScheme.surface,
        ) {
          TopLevelTab.entries.forEach { tab ->
            val selected =
              currentRoute == tab.route ||
                (tab == TopLevelTab.SCAN && currentRoute == MusicGuardRoutes.SCAN_WITH_MODE)
            NavigationRailItem(
              selected = selected,
              onClick = { navigateToTopLevel(tab) },
              icon = {
                Icon(
                  imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                  contentDescription = tab.label,
                )
              },
              label = { Text(tab.label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
              modifier = Modifier.testTag(tab.testTag),
            )
          }
        }
      }

      Scaffold(
        modifier = Modifier.weight(1f),
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
          if (!useNavigationRail && isTopLevelRoute) {
            NavigationBar(
              containerColor = MaterialTheme.colorScheme.surface,
            ) {
              TopLevelTab.entries.forEach { tab ->
                val selected =
                  currentRoute == tab.route ||
                    (tab == TopLevelTab.SCAN && currentRoute == MusicGuardRoutes.SCAN_WITH_MODE)
                NavigationBarItem(
                  selected = selected,
                  onClick = { navigateToTopLevel(tab) },
                  icon = {
                    Icon(
                      imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                      contentDescription = tab.label,
                    )
                  },
                  label = { Text(tab.label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                  modifier = Modifier.testTag(tab.testTag),
                )
              }
            }
          }
        },
      ) { innerPadding ->
        NavHost(
          navController = navController,
          startDestination = TopLevelTab.DASHBOARD.route,
          modifier = Modifier.fillMaxSize().padding(innerPadding),
        ) {
          composable(TopLevelTab.DASHBOARD.route) {
            DashboardScreen(
              scans = scans,
              providerState = providerState,
              onScanVideoClick = {
                viewModel.setPreferredScanMode("VIDEO")
                navController.navigate(MusicGuardRoutes.scanRoute("VIDEO")) {
                  popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                  launchSingleTop = true
                }
              },
              onScanAudioClick = {
                viewModel.setPreferredScanMode("AUDIO")
                navController.navigate(MusicGuardRoutes.scanRoute("AUDIO")) {
                  popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                  launchSingleTop = true
                }
              },
              onOpenScanDetail = { scanId ->
                navController.navigate(MusicGuardRoutes.resultDetailRoute(scanId))
              },
              onViewAllHistory = { navigateToTopLevel(TopLevelTab.HISTORY) },
              onLoadDemoPreview = { viewModel.seedDemoPreview() },
            )
          }

          composable(
            route = MusicGuardRoutes.SCAN_WITH_MODE,
            arguments =
              listOf(
                navArgument("mode") {
                  type = NavType.StringType
                  nullable = true
                  defaultValue = null
                }
              ),
          ) { entry ->
            val modeArg = entry.arguments?.getString("mode")
            LaunchedEffect(modeArg) {
              if (!modeArg.isNullOrBlank()) {
                viewModel.setPreferredScanMode(modeArg)
              }
            }
            ScanScreen(
              scanState = scanState,
              selectedMode = selectedMode,
              providerState = providerState,
              showConsentDialog = showConsentDialog,
              onModeSelected = viewModel::setPreferredScanMode,
              onMediaPicked = viewModel::inspectMediaUri,
              onCancelProcess = viewModel::cancelActiveProcess,
              onResetScan = viewModel::resetScanState,
              onRequestRecognition = viewModel::requestRecognitionCheck,
              onConfirmConsent = viewModel::confirmConsentAndRunRecognition,
              onDismissConsent = viewModel::dismissConsentDialog,
              onSaveToHistoryAndOpenDetail = {
                viewModel.saveInspectedMediaToHistory { savedId ->
                  navController.navigate(MusicGuardRoutes.resultDetailRoute(savedId))
                }
              },
            )
          }

          composable(TopLevelTab.HISTORY.route) {
            HistoryScreen(
              scans = scans,
              onOpenScanDetail = { scanId ->
                navController.navigate(MusicGuardRoutes.resultDetailRoute(scanId))
              },
              onDeleteScan = viewModel::deleteScan,
              onDeleteAllScans = viewModel::deleteAllScans,
              onGoToScan = { navigateToTopLevel(TopLevelTab.SCAN) },
              onLoadDemoPreview = viewModel::seedDemoPreview,
            )
          }

          composable(TopLevelTab.REPORTS.route) {
            ReportsScreen(
              scans = scans,
              onGoToScan = { navigateToTopLevel(TopLevelTab.SCAN) },
              onLoadDemoPreview = viewModel::seedDemoPreview,
              onRecordExport = viewModel::recordReportExport,
            )
          }

          composable(TopLevelTab.SETTINGS.route) {
            SettingsScreen(
              settings = settings,
              providerState = providerState,
              onSaveProxyUrl = viewModel::setBackendProxyUrl,
              onThemeChange = viewModel::setThemePreference,
              onToggleConsent = viewModel::setRequireUploadConsent,
              onClearDemoData = viewModel::clearDemoPreview,
            )
          }

          composable(
            route = MusicGuardRoutes.RESULT_DETAIL,
            arguments = listOf(navArgument("scanId") { type = NavType.StringType }),
          ) { backStackEntry ->
            val scanId = backStackEntry.arguments?.getString("scanId") ?: ""
            val scanDetail by
              viewModel.observeScan(scanId).collectAsStateWithLifecycle(initialValue = null)
            ResultDetailScreen(
              scan = scanDetail,
              onBack = { navController.popBackStack() },
              onSaveLicenseEvidence = viewModel::saveLicenseEvidence,
              onRecordExport = viewModel::recordReportExport,
            )
          }
        }
      }
    }
  }
}
