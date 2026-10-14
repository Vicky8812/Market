package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.repository.MarketRepository
import com.example.ui.components.StockSearchDialog
import com.example.ui.screens.*
import com.example.ui.theme.*

enum class AppDestination(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    DASHBOARD("dashboard", "Dashboard", Icons.Filled.Insights, Icons.Outlined.Insights),
    DISCOVERY("discovery", "Discovery", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome),
    PORTFOLIO("portfolio", "Portfolio", Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet),
    AI_ASSISTANT("assistant", "AI Research", Icons.Filled.SmartToy, Icons.Outlined.SmartToy)
}

sealed class ScreenState {
    data object MainTabs : ScreenState()
    data class StockDetail(val ticker: String) : ScreenState()
    data class StockCompare(val initialTicker: String? = null) : ScreenState()
    data object Backtest : ScreenState()
    data object IndiaHub : ScreenState()
}

@Composable
fun MarketPulseApp(
    repository: MarketRepository
) {
    var currentDestination by remember { mutableStateOf(AppDestination.DASHBOARD) }
    var screenState by remember { mutableStateOf<ScreenState>(ScreenState.MainTabs) }
    var pendingAssistantPrompt by remember { mutableStateOf<String?>(null) }

    var showSearchDialog by remember { mutableStateOf(false) }
    var showAlertsDialog by remember { mutableStateOf(false) }

    val allStocks = remember { repository.getAllStockQuotes() }

    BackHandler(enabled = screenState !is ScreenState.MainTabs) {
        screenState = ScreenState.MainTabs
    }

    Scaffold(
        bottomBar = {
            if (screenState is ScreenState.MainTabs) {
                NavigationBar(
                    containerColor = SurfaceDark,
                    tonalElevation = 8.dp,
                    windowInsets = WindowInsets.navigationBars,
                    modifier = Modifier.testTag("bottom_nav_bar")
                ) {
                    AppDestination.values().forEach { destination ->
                        val selected = currentDestination == destination
                        NavigationBarItem(
                            selected = selected,
                            onClick = { currentDestination = destination },
                            icon = {
                                Icon(
                                    imageVector = if (selected) destination.selectedIcon else destination.unselectedIcon,
                                    contentDescription = destination.title
                                )
                            },
                            label = { Text(destination.title) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = BackgroundDark,
                                selectedTextColor = CyanAccent,
                                indicatorColor = CyanAccent,
                                unselectedIconColor = TextMuted,
                                unselectedTextColor = TextMuted
                            )
                        )
                    }
                }
            }
        },
        containerColor = BackgroundDark,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = screenState) {
                is ScreenState.MainTabs -> {
                    when (currentDestination) {
                        AppDestination.DASHBOARD -> DashboardScreen(
                            repository = repository,
                            onStockClick = { ticker ->
                                screenState = ScreenState.StockDetail(ticker)
                            },
                            onOpenSearch = { showSearchDialog = true },
                            onOpenAlerts = { showAlertsDialog = true },
                            onOpenBacktest = { screenState = ScreenState.Backtest },
                            onCompareClick = { screenState = ScreenState.StockCompare() },
                            onOpenIndiaHub = { screenState = ScreenState.IndiaHub }
                        )
                        AppDestination.DISCOVERY -> DiscoveryScreen(
                            repository = repository,
                            onStockClick = { ticker ->
                                screenState = ScreenState.StockDetail(ticker)
                            }
                        )
                        AppDestination.PORTFOLIO -> PortfolioScreen(
                            repository = repository,
                            onStockClick = { ticker ->
                                screenState = ScreenState.StockDetail(ticker)
                            }
                        )
                        AppDestination.AI_ASSISTANT -> AiAssistantScreen(
                            repository = repository,
                            initialPrompt = pendingAssistantPrompt,
                            onStockClick = { ticker ->
                                screenState = ScreenState.StockDetail(ticker)
                            }
                        )
                    }
                }
                is ScreenState.StockDetail -> {
                    StockDetailScreen(
                        ticker = state.ticker,
                        repository = repository,
                        onBack = { screenState = ScreenState.MainTabs },
                        onOpenCompareWithStock = { tick ->
                            screenState = ScreenState.StockCompare(tick)
                        }
                    )
                }
                is ScreenState.StockCompare -> {
                    StockCompareScreen(
                        initialTicker = state.initialTicker,
                        repository = repository,
                        onBack = { screenState = ScreenState.MainTabs }
                    )
                }
                is ScreenState.Backtest -> {
                    BacktestScreen(
                        repository = repository,
                        onBack = { screenState = ScreenState.MainTabs }
                    )
                }
                is ScreenState.IndiaHub -> {
                    IndiaInvestorHubScreen(
                        repository = repository,
                        onBack = { screenState = ScreenState.MainTabs },
                        onNavigateToChatWithPrompt = { prompt ->
                            pendingAssistantPrompt = prompt
                            currentDestination = AppDestination.AI_ASSISTANT
                            screenState = ScreenState.MainTabs
                        }
                    )
                }
            }
        }
    }

    if (showSearchDialog) {
        StockSearchDialog(
            allStocks = allStocks,
            onDismiss = { showSearchDialog = false },
            onStockSelected = { quote ->
                screenState = ScreenState.StockDetail(quote.ticker)
            }
        )
    }

    if (showAlertsDialog) {
        AlertsDialog(
            repository = repository,
            onDismiss = { showAlertsDialog = false }
        )
    }
}
