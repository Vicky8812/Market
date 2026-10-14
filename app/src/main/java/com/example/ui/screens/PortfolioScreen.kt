package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PortfolioHolding
import com.example.data.model.WatchlistItem
import com.example.data.repository.MarketRepository
import com.example.ui.components.StockSearchDialog
import com.example.ui.theme.*
import kotlinx.coroutines.launch

enum class PortfolioTab(val label: String) {
    HOLDINGS("Holdings"),
    WATCHLISTS("Watchlists"),
    ALLOCATION("Allocation & Health"),
    STRESS_TEST("Stress Testing")
}

@Composable
fun PortfolioScreen(
    repository: MarketRepository,
    onStockClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val allStocks = remember { repository.getAllStockQuotes() }

    val holdings by repository.allHoldings.collectAsState(initial = emptyList())
    val watchlistItems by repository.allWatchlistItems.collectAsState(initial = emptyList())

    var selectedTab by remember { mutableStateOf(PortfolioTab.HOLDINGS) }
    var showAddHoldingDialog by remember { mutableStateOf(false) }
    var showSearchForWatchlist by remember { mutableStateOf(false) }

    // Seed initial demo holdings if empty on first run
    LaunchedEffect(holdings) {
        if (holdings.isEmpty()) {
            repository.addHolding("NVDA", "NVIDIA Corporation", 25.0, 118.50)
            repository.addHolding("AAPL", "Apple Inc.", 40.0, 215.20)
            repository.addHolding("MSFT", "Microsoft Corporation", 15.0, 402.10)
            repository.addHolding("GOOGL", "Alphabet Inc.", 30.0, 155.40)
        }
    }

    // Seed initial watchlist items if empty
    LaunchedEffect(watchlistItems) {
        if (watchlistItems.isEmpty()) {
            repository.addWatchlistItem("PLTR")
            repository.addWatchlistItem("TSLA")
            repository.addWatchlistItem("AMD")
            repository.addWatchlistItem("JPM")
        }
    }

    // Portfolio Calculations
    val holdingsWithQuotes = holdings.map { holding ->
        val quote = allStocks.find { it.ticker.equals(holding.ticker, ignoreCase = true) }
        val currentPrice = quote?.price ?: holding.avgBuyPrice
        val totalCost = holding.shares * holding.avgBuyPrice
        val currentValue = holding.shares * currentPrice
        val gainLoss = currentValue - totalCost
        val gainLossPct = if (totalCost > 0) (gainLoss / totalCost) * 100 else 0.0
        val dayChange = (quote?.change ?: 0.0) * holding.shares
        HoldingDisplay(
            holding = holding,
            currentPrice = currentPrice,
            currentValue = currentValue,
            gainLoss = gainLoss,
            gainLossPct = gainLossPct,
            dayChange = dayChange,
            sector = quote?.sector ?: "Other"
        )
    }

    val totalPortfolioValue = holdingsWithQuotes.sumOf { it.currentValue }
    val totalCostBasis = holdingsWithQuotes.sumOf { it.holding.shares * it.holding.avgBuyPrice }
    val totalUnrealizedGain = totalPortfolioValue - totalCostBasis
    val totalUnrealizedGainPct = if (totalCostBasis > 0) (totalUnrealizedGain / totalCostBasis) * 100 else 0.0
    val totalDayGain = holdingsWithQuotes.sumOf { it.dayChange }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
    ) {
        // Portfolio Total Equity Header Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SurfaceCardBorder))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Total Portfolio Equity",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$${String.format(java.util.Locale.US, "%,.2f", totalPortfolioValue)}",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Total Return", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            Text(
                                text = "${if (totalUnrealizedGain >= 0) "+" else ""}$${String.format(java.util.Locale.US, "%,.2f", totalUnrealizedGain)} (${String.format(java.util.Locale.US, "%.2f", totalUnrealizedGainPct)}%)",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (totalUnrealizedGain >= 0) BullGreen else BearRed
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Today's P&L", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            Text(
                                text = "${if (totalDayGain >= 0) "+" else ""}$${String.format(java.util.Locale.US, "%,.2f", totalDayGain)}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (totalDayGain >= 0) BullGreen else BearRed
                            )
                        }
                    }
                }
            }
        }

        // Sub-Navigation Tabs
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(PortfolioTab.values()) { tab ->
                    FilterChip(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        label = { Text(tab.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyanAccentBg,
                            selectedLabelColor = CyanAccent,
                            containerColor = SurfaceCard,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedTab == tab,
                            borderColor = if (selectedTab == tab) CyanAccent else SurfaceCardBorder
                        )
                    )
                }
            }
        }

        when (selectedTab) {
            PortfolioTab.HOLDINGS -> {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ACTIVE HOLDINGS (${holdings.size})",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                            color = TextMuted
                        )
                        Button(
                            onClick = { showAddHoldingDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("add_holding_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = BackgroundDark, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Stock", color = BackgroundDark, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }

                items(holdingsWithQuotes) { item ->
                    HoldingCard(
                        holdingDisplay = item,
                        onClick = { onStockClick(item.holding.ticker) },
                        onDelete = {
                            coroutineScope.launch {
                                repository.deleteHolding(item.holding.id)
                            }
                        }
                    )
                }
            }

            PortfolioTab.WATCHLISTS -> {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "MONITORED WATCHLIST",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                            color = TextMuted
                        )
                        Button(
                            onClick = { showSearchForWatchlist = true },
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceCardElevated),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Ticker", color = TextPrimary, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                items(watchlistItems) { watchItem ->
                    val quote = allStocks.find { it.ticker.equals(watchItem.ticker, ignoreCase = true) }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onStockClick(watchItem.ticker) },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SurfaceCardBorder))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(watchItem.ticker, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                                Text(quote?.companyName ?: "", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (quote != null) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("$${quote.price}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                                        Text(
                                            "${if (quote.changePercent >= 0) "+" else ""}${quote.changePercent}%",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (quote.changePercent >= 0) BullGreen else BearRed
                                        )
                                    }
                                }
                                IconButton(onClick = {
                                    coroutineScope.launch { repository.removeWatchlistItem(watchItem.ticker) }
                                }) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = TextMuted)
                                }
                            }
                        }
                    }
                }
            }

            PortfolioTab.ALLOCATION -> {
                item {
                    AllocationSection(holdings = holdingsWithQuotes, totalValue = totalPortfolioValue)
                }
            }

            PortfolioTab.STRESS_TEST -> {
                item {
                    StressTestSection(totalPortfolioValue = totalPortfolioValue, holdings = holdingsWithQuotes)
                }
            }
        }
    }

    if (showAddHoldingDialog) {
        AddHoldingModal(
            allStocks = allStocks,
            onDismiss = { showAddHoldingDialog = false },
            onSave = { ticker, shares, price ->
                val name = allStocks.find { it.ticker.equals(ticker, ignoreCase = true) }?.companyName ?: ticker
                coroutineScope.launch {
                    repository.addHolding(ticker, name, shares, price)
                    showAddHoldingDialog = false
                }
            }
        )
    }

    if (showSearchForWatchlist) {
        StockSearchDialog(
            allStocks = allStocks,
            onDismiss = { showSearchForWatchlist = false },
            onStockSelected = { quote ->
                coroutineScope.launch {
                    repository.addWatchlistItem(quote.ticker)
                    showSearchForWatchlist = false
                }
            }
        )
    }
}

data class HoldingDisplay(
    val holding: PortfolioHolding,
    val currentPrice: Double,
    val currentValue: Double,
    val gainLoss: Double,
    val gainLossPct: Double,
    val dayChange: Double,
    val sector: String
)

@Composable
fun HoldingCard(
    holdingDisplay: HoldingDisplay,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SurfaceCardBorder))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1.2f)) {
                Text(
                    text = holdingDisplay.holding.ticker,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Text(
                    text = "${holdingDisplay.holding.shares} shares @ $${holdingDisplay.holding.avgBuyPrice}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Text(
                    text = "Sector: ${holdingDisplay.sector}",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = TextMuted
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$${String.format(java.util.Locale.US, "%,.2f", holdingDisplay.currentValue)}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Text(
                    text = "${if (holdingDisplay.gainLoss >= 0) "+" else ""}$${String.format(java.util.Locale.US, "%.2f", holdingDisplay.gainLoss)} (${String.format(java.util.Locale.US, "%.2f", holdingDisplay.gainLossPct)}%)",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (holdingDisplay.gainLoss >= 0) BullGreen else BearRed
                )
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Close, contentDescription = "Remove", tint = TextMuted, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun AllocationSection(holdings: List<HoldingDisplay>, totalValue: Double) {
    val sectorGroups = holdings.groupBy { it.sector }
        .mapValues { (_, list) -> list.sumOf { it.currentValue } }
        .toList()
        .sortedByDescending { it.second }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SurfaceCardBorder))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Portfolio Allocation & Health Diagnosis",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))

            sectorGroups.forEach { (sector, value) ->
                val pct = if (totalValue > 0) (value / totalValue) * 100 else 0.0
                Column(modifier = Modifier.padding(vertical = 5.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(sector, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                        Text("${String.format(java.util.Locale.US, "%.1f", pct)}% ($${String.format(java.util.Locale.US, "%,.0f", value)})", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = CyanAccent)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(SurfaceCardElevated)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth((pct / 100f).toFloat().coerceIn(0f, 1f))
                                .background(CyanAccent)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = SurfaceCardBorder)
            Spacer(modifier = Modifier.height(12.dp))

            // Concentration Risk Check
            val highestStock = holdings.maxByOrNull { it.currentValue }
            if (highestStock != null && totalValue > 0) {
                val stockPct = (highestStock.currentValue / totalValue) * 100
                if (stockPct > 35.0) {
                    Surface(
                        color = Color(0x26FF9100),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Concentration Alert: ${highestStock.holding.ticker} accounts for ${String.format(java.util.Locale.US, "%.1f", stockPct)}% of your portfolio. Consider rebalancing.",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = GoldAccent
                            )
                        }
                    }
                } else {
                    Surface(
                        color = BullGreenBg,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = BullGreen, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Diversification Healthy: Largest position is ${highestStock.holding.ticker} at ${String.format(java.util.Locale.US, "%.1f", stockPct)}%.",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = BullGreen
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StressTestSection(totalPortfolioValue: Double, holdings: List<HoldingDisplay>) {
    var selectedScenarioIndex by remember { mutableStateOf(0) }

    val scenarios = listOf(
        Pair("Broad Market Pullback (-10% S&P 500)", -0.10),
        Pair("Tech Sector Surge (+15% Nasdaq)", 0.15),
        Pair("Stagflation Shock (-18% Equities)", -0.18),
        Pair("Fed Aggressive Easing (+12% Equities)", 0.12)
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SurfaceCardBorder))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Hypothetical Stress Testing Simulator",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Model simulated macro shocks across your holdings using weighted beta and sector sensitivity.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            scenarios.forEachIndexed { index, (label, _) ->
                FilterChip(
                    selected = selectedScenarioIndex == index,
                    onClick = { selectedScenarioIndex = index },
                    label = { Text(label) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyanAccentBg,
                        selectedLabelColor = CyanAccent
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            val (_, shockPct) = scenarios[selectedScenarioIndex]
            val simulatedChange = totalPortfolioValue * shockPct * 1.15 // average beta ~1.15
            val projectedValue = totalPortfolioValue + simulatedChange

            Surface(
                color = SurfaceCardElevated,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Projected Portfolio Value", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Text("$${String.format(java.util.Locale.US, "%,.2f", projectedValue)}", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Estimated Impact: ${if (simulatedChange >= 0) "+" else ""}$${String.format(java.util.Locale.US, "%,.2f", simulatedChange)} (${if (shockPct >= 0) "+" else ""}${String.format(java.util.Locale.US, "%.1f", shockPct * 115)}%)",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (simulatedChange >= 0) BullGreen else BearRed
                    )
                }
            }
        }
    }
}

@Composable
fun AddHoldingModal(
    allStocks: List<com.example.data.model.StockQuote>,
    onDismiss: () -> Unit,
    onSave: (String, Double, Double) -> Unit
) {
    var ticker by remember { mutableStateOf("NVDA") }
    var shares by remember { mutableStateOf("10") }
    var price by remember { mutableStateOf("138.00") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Position to Portfolio", color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = ticker,
                    onValueChange = { ticker = it.uppercase() },
                    label = { Text("Ticker Symbol (e.g. AAPL)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                )
                OutlinedTextField(
                    value = shares,
                    onValueChange = { shares = it },
                    label = { Text("Shares Count") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                )
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Purchase Price ($)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val s = shares.toDoubleOrNull() ?: 1.0
                    val p = price.toDoubleOrNull() ?: 100.0
                    onSave(ticker, s, p)
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
            ) {
                Text("Save Holding", color = BackgroundDark)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        },
        containerColor = SurfaceDark
    )
}
