package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.CyberBackgroundWrapper
import com.example.ui.components.CyberBottomBar
import com.example.ui.components.CyberMenuDrawerSheet
import com.example.ui.components.NavTab
import com.example.ui.components.PinLockOverlay
import com.example.ui.components.TopGsmHeader
import com.example.ui.screens.AiScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.ChartMarkerScreen
import com.example.ui.screens.DataMarketScreen
import com.example.ui.screens.FormulaScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.New1FormulaScreen
import com.example.ui.screens.OtcScreen
import com.example.ui.screens.ReportScreen
import com.example.ui.screens.SettingsPdfScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScreen()
            }
        }
    }
}

@Composable
fun MainAppScreen(mainViewModel: MainViewModel = viewModel()) {
    var currentTab by remember { mutableStateOf(NavTab.HOME) }
    var isMenuDrawerOpen by remember { mutableStateOf(false) }

    val selectedMarket by mainViewModel.selectedMarket.collectAsState()
    val availableMarkets by mainViewModel.availableMarkets.collectAsState()
    val wallpaperPath by mainViewModel.wallpaperPath.collectAsState()
    val bgDimLevel by mainViewModel.bgDimLevel.collectAsState()

    val userName by mainViewModel.userName.collectAsState()
    val userRole by mainViewModel.userRole.collectAsState()

    // Hide Top Header and Bottom Bar on feature screens to provide 100% full screen space
    val isHomeScreen = (currentTab == NavTab.HOME)

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                if (isHomeScreen) {
                    Box(modifier = Modifier.navigationBarsPadding()) {
                        CyberBottomBar(
                            currentTab = currentTab,
                            onTabSelected = { currentTab = it }
                        )
                    }
                }
            }
        ) { innerPadding ->
            CyberBackgroundWrapper(
                wallpaperPath = wallpaperPath,
                dimLevel = bgDimLevel
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .padding(bottom = if (isHomeScreen) innerPadding.calculateBottomPadding() else 0.dp)
                ) {
                    // Top Header
                    if (isHomeScreen) {
                        TopGsmHeader(
                            selectedMarket = selectedMarket,
                            availableMarkets = availableMarkets,
                            showMarketsBar = false,
                            showBackButton = false,
                            onMarketSelect = { market -> mainViewModel.selectMarket(market) },
                            onMenuClick = { isMenuDrawerOpen = true },
                            onProfileClick = { currentTab = NavTab.PROFILE }
                        )
                    } else {
                        // For non-home sub-screens (Formula, Data, History, AI, Settings, Profile), show Top Header with Back button to return to Home
                        TopGsmHeader(
                            selectedMarket = selectedMarket,
                            availableMarkets = availableMarkets,
                            showMarketsBar = false,
                            showBackButton = true,
                            onBackClick = { currentTab = NavTab.HOME },
                            onMarketSelect = { market -> mainViewModel.selectMarket(market) },
                            onMenuClick = { isMenuDrawerOpen = true },
                            onProfileClick = { currentTab = NavTab.PROFILE }
                        )
                    }

                    // Tab Content
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                    ) {
                        when (currentTab) {
                            NavTab.HOME -> HomeScreen(viewModel = mainViewModel)
                            NavTab.FORMULA -> FormulaScreen(viewModel = mainViewModel)
                            NavTab.DATA_MARKET -> DataMarketScreen(viewModel = mainViewModel)
                            NavTab.HISTORY -> HistoryScreen(viewModel = mainViewModel)
                            NavTab.AI_PREDICT -> AiScreen(viewModel = mainViewModel, onOpenSettings = { currentTab = NavTab.SETTINGS })
                            NavTab.SETTINGS -> SettingsPdfScreen(viewModel = mainViewModel)
                            NavTab.PROFILE -> AuthScreen(viewModel = mainViewModel)
                        }
                    }
                }
            }
        }

        // Side Navigation Drawer Menu Sheet
        CyberMenuDrawerSheet(
            isOpen = isMenuDrawerOpen,
            onClose = { isMenuDrawerOpen = false },
            currentTab = currentTab,
            onTabSelected = { currentTab = it },
            selectedMarket = selectedMarket,
            availableMarkets = availableMarkets,
            onMarketSelect = { market -> mainViewModel.selectMarket(market) },
            userName = userName,
            userRole = userRole
        )

        // Full Screen 4-Digit Security PIN Lock Overlay
        PinLockOverlay(viewModel = mainViewModel)
    }
}
