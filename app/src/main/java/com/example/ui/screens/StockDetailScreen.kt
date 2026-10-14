package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.data.repository.MarketRepository
import com.example.ui.components.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun StockDetailScreen(
    ticker: String,
    repository: MarketRepository,
    onBack: () -> Unit,
    onOpenCompareWithStock: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val coroutineScope = rememberCoroutineScope()
    val quote = remember(ticker) {
        repository.getAllStockQuotes().find { it.ticker.equals(ticker, ignoreCase = true) }
            ?: repository.getAllStockQuotes().first()
    }

    var selectedTimeframe by remember { mutableStateOf(Timeframe.ONE_MONTH) }
    val candles = remember(ticker, selectedTimeframe) {
        repository.getHistoricalCandles(quote.ticker, selectedTimeframe)
    }

    val forecasts = remember(ticker) { repository.getStockForecast(quote.ticker) }
    var selectedHorizon by remember { mutableStateOf(ForecastHorizon.TWENTY_DAYS) }
    val currentForecast = remember(selectedHorizon, forecasts) {
        forecasts.find { it.horizon == selectedHorizon } ?: forecasts.first()
    }

    val entryExitPlan = remember(ticker) { repository.getEntryExitPlan(quote.ticker) }
    val financialHealth = remember(ticker) { repository.getFinancialHealth(quote.ticker) }
    val technicals = remember(ticker) { repository.getTechnicalIndicators(quote.ticker) }

    // Interactive Position Sizing State
    var accountSizeInput by remember { mutableStateOf("25000") }
    var riskPercentInput by remember { mutableStateOf("1.5") }

    val snackbarHostState = remember { SnackbarHostState() }

    // Dialog state for quick portfolio/alert adds
    var showAddHoldingDialog by remember { mutableStateOf(false) }
    var showAddAlertDialog by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BackgroundDark)
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = quote.ticker,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = SurfaceCardElevated,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = quote.exchange,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = CyanAccent
                                )
                            }
                        }
                        Text(
                            text = quote.companyName,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted,
                            maxLines = 1
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                repository.addWatchlistItem(quote.ticker)
                                snackbarHostState.showSnackbar("Added ${quote.ticker} to Watchlist")
                            }
                        },
                        modifier = Modifier.testTag("watchlist_add_btn")
                    ) {
                        Icon(Icons.Default.BookmarkAdd, contentDescription = "Watchlist", tint = GoldAccent)
                    }
                    IconButton(
                        onClick = { showAddAlertDialog = true },
                        modifier = Modifier.testTag("alert_add_btn")
                    ) {
                        Icon(Icons.Default.AddAlert, contentDescription = "Alert", tint = CyanAccent)
                    }
                    IconButton(
                        onClick = { showAddHoldingDialog = true },
                        modifier = Modifier.testTag("portfolio_add_btn")
                    ) {
                        Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Holdings", tint = BullGreen)
                    }
                }
            }
        },
        containerColor = BackgroundDark
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp)
        ) {
            // Price & Change Hero Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "$${String.format(java.util.Locale.US, "%.2f", quote.price)}",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                            color = TextPrimary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            PriceChangeChip(
                                change = quote.change,
                                changePercent = quote.changePercent
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Today",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Market Cap",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                        Text(
                            text = "$${quote.marketCapBillions}B",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                    }
                }
            }

            // Timeframe Selector Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(Timeframe.values()) { tf ->
                        FilterChip(
                            selected = selectedTimeframe == tf,
                            onClick = { selectedTimeframe = tf },
                            label = { Text(tf.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanAccentBg,
                                selectedLabelColor = CyanAccent,
                                containerColor = SurfaceCard,
                                labelColor = TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedTimeframe == tf,
                                borderColor = if (selectedTimeframe == tf) CyanAccent else SurfaceCardBorder
                            )
                        )
                    }
                }
            }

            // Interactive Candlestick / Line Chart
            item {
                InteractiveFinancialChart(
                    candles = candles,
                    isPositiveTrend = quote.changePercent >= 0,
                    showVolume = true
                )
            }

            // Key Valuation & Multiple Ratios Grid
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SurfaceCardBorder))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Key Valuation & Factor Ratios",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            MetricCell("P/E Ratio", "${quote.peRatio}x", Modifier.weight(1f))
                            MetricCell("Forward P/E", "${quote.forwardPe}x", Modifier.weight(1f))
                            MetricCell("PEG Ratio", "${quote.pegRatio}", Modifier.weight(1f))
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            MetricCell("Price/Sales", "${quote.priceToSales}x", Modifier.weight(1f))
                            MetricCell("EV/EBITDA", "${quote.evToEbitda}x", Modifier.weight(1f))
                            MetricCell("Beta (Vol)", "${quote.beta}", Modifier.weight(1f))
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            MetricCell("52W Range", "$${quote.low52W} - $${quote.high52W}", Modifier.weight(1.5f))
                            MetricCell("Div Yield", "${quote.dividendYield}%", Modifier.weight(1f))
                            MetricCell("RSI (14)", "${quote.rsi14.toInt()}", Modifier.weight(0.8f))
                        }
                    }
                }
            }

            // Stock Price Movement Prediction Engine (Multi-Horizon Scenarios)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(CyanAccent.copy(alpha = 0.5f))
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.QueryStats, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "AI Quantitative Price Forecast",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimary
                                )
                            }
                            Surface(
                                color = CyanAccentBg,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "Confidence: ${currentForecast.confidenceScore}%",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = CyanAccent
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Horizon selector
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(ForecastHorizon.values()) { hor ->
                                FilterChip(
                                    selected = selectedHorizon == hor,
                                    onClick = { selectedHorizon = hor },
                                    label = { Text(hor.label) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = CyanAccentBg,
                                        selectedLabelColor = CyanAccent,
                                        containerColor = SurfaceCardElevated,
                                        labelColor = TextSecondary
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Reference Price: $${currentForecast.referencePrice}  •  Uncertainty Band: ${currentForecast.uncertaintyRange}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // 3 Scenarios Grid: Bear, Base, Bull
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ScenarioCard(
                                title = "BEARISH (${currentForecast.bearish.probabilityPercent}%)",
                                price = currentForecast.bearish.targetPrice,
                                returnPct = currentForecast.bearish.percentageReturn,
                                color = BearRed,
                                bgColor = BearRedBg,
                                modifier = Modifier.weight(1f)
                            )
                            ScenarioCard(
                                title = "BASE CASE (${currentForecast.baseCase.probabilityPercent}%)",
                                price = currentForecast.baseCase.targetPrice,
                                returnPct = currentForecast.baseCase.percentageReturn,
                                color = CyanAccent,
                                bgColor = CyanAccentBg,
                                modifier = Modifier.weight(1f)
                            )
                            ScenarioCard(
                                title = "BULLISH (${currentForecast.bullish.probabilityPercent}%)",
                                price = currentForecast.bullish.targetPrice,
                                returnPct = currentForecast.bullish.percentageReturn,
                                color = BullGreen,
                                bgColor = BullGreenBg,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Drivers & Invalidation
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Technical Driver: ${currentForecast.primaryTechnicalDrivers}", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = TextSecondary)
                            Text("Fundamental Driver: ${currentForecast.primaryFundamentalDrivers}", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = TextSecondary)
                            Text("Invalidation Level: Below $${currentForecast.bearish.invalidationLevel}", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold), color = GoldAccent)
                        }
                    }
                }
            }

            // Entry, Exit & Position Sizing Calculator
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SurfaceCardBorder))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Entry, Exit & Risk Framework",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Surface(
                                color = BullGreenBg,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "R:R ${entryExitPlan.riskRewardRatio}",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = BullGreen
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            MetricCell("Suggested Entry", "$${entryExitPlan.entryZoneMin} - $${entryExitPlan.entryZoneMax}", Modifier.weight(1.3f))
                            MetricCell("Stop-Loss", "$${entryExitPlan.stopLossLevel} (${entryExitPlan.stopLossPercentage}%)", Modifier.weight(1.3f))
                            MetricCell("Target (TP1)", "$${entryExitPlan.takeProfit1}", Modifier.weight(1f))
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Rationale: ${entryExitPlan.entryRationale}",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = SurfaceCardBorder)
                        Spacer(modifier = Modifier.height(12.dp))

                        // Interactive Position-Sizing Calculator
                        Text(
                            text = "Position Sizing Calculator",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = CyanAccent
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = accountSizeInput,
                                onValueChange = { accountSizeInput = it },
                                label = { Text("Account Equity ($)", fontSize = 11.sp) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CyanAccent,
                                    unfocusedBorderColor = SurfaceCardBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )
                            OutlinedTextField(
                                value = riskPercentInput,
                                onValueChange = { riskPercentInput = it },
                                label = { Text("Max Risk %", fontSize = 11.sp) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CyanAccent,
                                    unfocusedBorderColor = SurfaceCardBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        val equity = accountSizeInput.toDoubleOrNull() ?: 25000.0
                        val riskPct = riskPercentInput.toDoubleOrNull() ?: 1.5
                        val dollarRisk = equity * (riskPct / 100.0)
                        val perShareRisk = kotlin.math.max(0.5, quote.price - entryExitPlan.stopLossLevel)
                        val maxShares = (dollarRisk / perShareRisk).toInt()
                        val totalCapital = maxShares * quote.price

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = SurfaceCardElevated,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Max Shares", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                    Text("$maxShares shares", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                                }
                                Column {
                                    Text("Position Capital", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                    Text("$${String.format(java.util.Locale.US, "%,.0f", totalCapital)}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = CyanAccent)
                                }
                                Column {
                                    Text("Max Loss at Stop", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                    Text("-$${String.format(java.util.Locale.US, "%.0f", dollarRisk)}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = BearRed)
                                }
                            }
                        }
                    }
                }
            }

            // Financial Statements & Secular Health
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SurfaceCardBorder))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Financial Statements & Growth Trends",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            MetricCell("Revenue YoY", "+${financialHealth.revenueYoY}%", Modifier.weight(1f))
                            MetricCell("Net Margin", "${financialHealth.netMarginPercent}%", Modifier.weight(1f))
                            MetricCell("Free Cash Flow", "$${financialHealth.freeCashFlowBillions}B", Modifier.weight(1f))
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            MetricCell("Debt / Equity", "${financialHealth.debtToEquity}x", Modifier.weight(1f))
                            MetricCell("Return on Equity", "${financialHealth.returnOnEquity}%", Modifier.weight(1f))
                            MetricCell("Next Earnings", financialHealth.nextEarningsDate, Modifier.weight(1.5f))
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "SEC Filing Summary: ${financialHealth.secFiling10KSummary}",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 16.sp),
                            color = TextSecondary
                        )
                    }
                }
            }

            // Technical Indicators Summary
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SurfaceCardBorder))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Technical Indicators & Signals",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Surface(
                                color = CyanAccentBg,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = technicals.technicalVerdict,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = CyanAccent
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            MetricCell("50-Day SMA", "$${technicals.sma50}", Modifier.weight(1f))
                            MetricCell("200-Day SMA", "$${technicals.sma200}", Modifier.weight(1f))
                            MetricCell("MACD Hist", "${technicals.macdHist}", Modifier.weight(1f))
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            MetricCell("Bollinger Upper", "$${technicals.bollingerUpper}", Modifier.weight(1f))
                            MetricCell("Bollinger Lower", "$${technicals.bollingerLower}", Modifier.weight(1f))
                            MetricCell("Support / Resist", "$${technicals.nearestSupport} / $${technicals.nearestResistance}", Modifier.weight(1.5f))
                        }
                    }
                }
            }

            // Side-by-side comparison action
            item {
                Button(
                    onClick = { onOpenCompareWithStock(quote.ticker) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceCardElevated),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CompareArrows, contentDescription = null, tint = CyanAccent)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Compare ${quote.ticker} with Peers Side-by-Side", color = TextPrimary)
                }
            }
        }
    }

    // Add to Portfolio Dialog
    if (showAddHoldingDialog) {
        var sharesStr by remember { mutableStateOf("10") }
        var priceStr by remember { mutableStateOf(quote.price.toString()) }

        AlertDialog(
            onDismissRequest = { showAddHoldingDialog = false },
            title = { Text("Add ${quote.ticker} to Portfolio", color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = sharesStr,
                        onValueChange = { sharesStr = it },
                        label = { Text("Number of Shares") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                    )
                    OutlinedTextField(
                        value = priceStr,
                        onValueChange = { priceStr = it },
                        label = { Text("Purchase Price ($)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val shares = sharesStr.toDoubleOrNull() ?: 1.0
                        val price = priceStr.toDoubleOrNull() ?: quote.price
                        coroutineScope.launch {
                            repository.addHolding(quote.ticker, quote.companyName, shares, price)
                            showAddHoldingDialog = false
                            snackbarHostState.showSnackbar("Holding added to Portfolio!")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BullGreen)
                ) {
                    Text("Save Holding", color = BackgroundDark)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddHoldingDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = SurfaceDark
        )
    }

    // Add Alert Dialog
    if (showAddAlertDialog) {
        var targetStr by remember { mutableStateOf((quote.price * 1.05).toInt().toString()) }
        var condition by remember { mutableStateOf("ABOVE") }

        AlertDialog(
            onDismissRequest = { showAddAlertDialog = false },
            title = { Text("Set Price Alert for ${quote.ticker}", color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = condition == "ABOVE",
                            onClick = { condition = "ABOVE" },
                            label = { Text("Price Rises Above") },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = BullGreenBg, selectedLabelColor = BullGreen)
                        )
                        FilterChip(
                            selected = condition == "BELOW",
                            onClick = { condition = "BELOW" },
                            label = { Text("Price Drops Below") },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = BearRedBg, selectedLabelColor = BearRed)
                        )
                    }
                    OutlinedTextField(
                        value = targetStr,
                        onValueChange = { targetStr = it },
                        label = { Text("Target Price ($)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val target = targetStr.toDoubleOrNull() ?: quote.price
                        coroutineScope.launch {
                            repository.addAlert(quote.ticker, target, condition, "Alert when ${quote.ticker} crosses $target")
                            showAddAlertDialog = false
                            snackbarHostState.showSnackbar("Alert configured for ${quote.ticker}!")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
                ) {
                    Text("Create Alert", color = BackgroundDark)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddAlertDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = SurfaceDark
        )
    }
}

@Composable
fun MetricCell(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextMuted)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold), color = TextPrimary)
    }
}

@Composable
fun ScenarioCard(
    title: String,
    price: Double,
    returnPct: Double,
    color: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = bgColor,
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold), color = color)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "$${String.format(java.util.Locale.US, "%.1f", price)}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
            Text(
                text = "${if (returnPct >= 0) "+" else ""}${returnPct}%",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                color = color
            )
        }
    }
}
