package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.ui.AutoOpenEvent
import com.example.ui.MainViewModel
import com.example.ui.detail.InAppBrowserScreen
import com.example.ui.detail.ProductDetailSheet
import com.example.ui.history.HistoryScreen
import com.example.ui.imei.ImeiBatchScreen
import com.example.ui.scanner.ScannerScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.MyApplicationTheme

enum class MainTab(val title: String) {
    SCANNER("スキャナー"),
    IMEI("IMEI一括"),
    HISTORY("履歴・保存"),
    SETTINGS("設定")
}

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                MainApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(viewModel: MainViewModel) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }

    val scanHistory by viewModel.scanHistory.collectAsState()
    val activeDetailItem by viewModel.activeDetailItem.collectAsState()
    val aiSummaryState by viewModel.aiSummaryState.collectAsState()
    val browserUrlState by viewModel.browserUrlState.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val historySearchQuery by viewModel.historySearchQuery.collectAsState()
    val historyFavoritesOnly by viewModel.historyFavoritesOnly.collectAsState()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Handle auto-open events (e.g. immediately opening external browser upon scan)
    LaunchedEffect(Unit) {
        viewModel.autoOpenEvents.collect { event ->
            when (event) {
                is AutoOpenEvent.OpenBrowser -> {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(event.url))
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "ブラウザを開けませんでした", Toast.LENGTH_SHORT).show()
                    }
                }
                is AutoOpenEvent.OpenInAppWeb -> {
                    viewModel.openInAppBrowser(event.url, event.title)
                }
            }
        }
    }

    // Back button handling
    BackHandler(enabled = browserUrlState != null || activeDetailItem != null || selectedTab != 0) {
        when {
            browserUrlState != null -> viewModel.closeInAppBrowser()
            activeDetailItem != null -> viewModel.closeDetailSheet()
            selectedTab != 0 -> selectedTab = 0
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (selectedTab != 0) {
                TopAppBar(
                    title = {
                        Text(
                            text = when (selectedTab) {
                                1 -> "IMEI一括判定・買取検索"
                                2 -> "スキャン履歴"
                                3 -> "設定"
                                else -> "バーコード検索"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("main_bottom_nav_bar")
            ) {
                // Tab 0: Scanner
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 0) Icons.Filled.QrCodeScanner else Icons.Outlined.QrCodeScanner,
                            contentDescription = "スキャナー"
                        )
                    },
                    label = { Text("スキャナー") },
                    modifier = Modifier.testTag("nav_tab_scanner")
                )

                // Tab 1: IMEI Batch
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 1) Icons.Filled.PhoneAndroid else Icons.Outlined.PhoneAndroid,
                            contentDescription = "IMEI一括"
                        )
                    },
                    label = { Text("IMEI一括") },
                    modifier = Modifier.testTag("nav_tab_imei")
                )

                // Tab 2: History
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (scanHistory.isNotEmpty()) {
                                    Badge {
                                        Text(if (scanHistory.size > 99) "99+" else scanHistory.size.toString())
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (selectedTab == 2) Icons.Filled.History else Icons.Outlined.History,
                                contentDescription = "履歴"
                            )
                        }
                    },
                    label = { Text("履歴") },
                    modifier = Modifier.testTag("nav_tab_history")
                )

                // Tab 3: Settings
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 3) Icons.Filled.Settings else Icons.Outlined.Settings,
                            contentDescription = "設定"
                        )
                    },
                    label = { Text("設定") },
                    modifier = Modifier.testTag("nav_tab_settings")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> {
                    ScannerScreen(
                        modifier = Modifier.fillMaxSize(),
                        isScanningActive = activeDetailItem == null && browserUrlState == null,
                        onBarcodeScanned = { barcode, format ->
                            viewModel.onBarcodeScanned(barcode, format)
                        },
                        onSelectSampleBarcode = { barcode ->
                            viewModel.onBarcodeScanned(barcode, "JAN-13")
                        },
                        sampleBarcodes = viewModel.sampleBarcodes
                    )
                }

                1 -> {
                    ImeiBatchScreen(
                        onOpenUrl = { url ->
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "ブラウザを起動できませんでした", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onOpenInAppBrowser = { url, title ->
                            viewModel.openInAppBrowser(url, title)
                        },
                        onNavigateToContinuousScanner = {
                            selectedTab = 0
                        }
                    )
                }

                2 -> {
                    HistoryScreen(
                        items = scanHistory,
                        searchQuery = historySearchQuery,
                        onSearchQueryChange = { viewModel.setHistorySearchQuery(it) },
                        showFavoritesOnly = historyFavoritesOnly,
                        onFilterFavoritesChange = { viewModel.setHistoryFavoritesOnly(it) },
                        onItemClick = { viewModel.openDetailSheet(it) },
                        onToggleFavorite = { viewModel.toggleFavorite(it) },
                        onDeleteItem = { viewModel.deleteItem(it) },
                        onClearAll = { viewModel.clearAllHistory() },
                        onNavigateToScanner = { selectedTab = 0 }
                    )
                }

                3 -> {
                    SettingsScreen(
                        settings = settings,
                        onUpdateSettings = { viewModel.updateSettings(it) }
                    )
                }
            }

            // Product Detail & Shopping Comparison Sheet
            activeDetailItem?.let { item ->
                ProductDetailSheet(
                    scanItem = item,
                    sheetState = sheetState,
                    aiSummaryState = aiSummaryState,
                    onRequestAiAnalysis = { viewModel.requestAiAnalysis(it) },
                    onDismiss = { viewModel.closeDetailSheet() },
                    onToggleFavorite = { viewModel.toggleFavorite(it) },
                    onUpdateNote = { scanItem, note -> viewModel.updateNote(scanItem, note) },
                    onOpenSearchUrl = { url, title ->
                        viewModel.openInAppBrowser(url, title)
                    },
                    onOpenExternalBrowser = { url ->
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "ブラウザを起動できませんでした", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }

            // In-App Browser Fullscreen Overlay
            AnimatedVisibility(
                visible = browserUrlState != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                browserUrlState?.let { (url, title) ->
                    InAppBrowserScreen(
                        url = url,
                        title = title,
                        onClose = { viewModel.closeInAppBrowser() },
                        onOpenExternal = { externalUrl ->
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(externalUrl))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "ブラウザを起動できませんでした", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }
        }
    }
}
