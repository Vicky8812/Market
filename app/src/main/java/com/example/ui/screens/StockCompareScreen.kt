package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StockQuote
import com.example.data.repository.MarketRepository
import com.example.ui.components.StockSearchDialog
import com.example.ui.theme.*

@Composable
fun StockCompareScreen(
    initialTicker: String?,
    repository: MarketRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val allStocks = remember { repository.getAllStockQuotes() }
    val defaultTickers = remember {
        val list = mutableListOf("NVDA", "AAPL", "MSFT")
        if (initialTicker != null && !list.contains(initialTicker)) {
            list.add(0, initialTicker)
        }
        list.take(5)
    }

    val selectedTickers = remember { mutableStateListOf<String>().apply { addAll(defaultTickers) } }
    var showAddDialog by remember { mutableStateOf(false) }

    val comparedStocks = selectedTickers.mapNotNull { ticker ->
        allStocks.find { it.ticker.equals(ticker, ignoreCase = true) }
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BackgroundDark)
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
                Text(
                    text = "Side-by-Side Stock Comparison",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
            }
        },
        containerColor = BackgroundDark
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp)
        ) {
            item {
                Text(
                    text = "Select up to 5 US stocks to benchmark financial health, valuation multiples, and technical momentum side by side.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            // Horizontal row of ticker chips with Remove and Add button
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    selectedTickers.forEach { ticker ->
                        InputChip(
                            selected = true,
                            onClick = { },
                            label = { Text(ticker, fontWeight = FontWeight.Bold) },
                            trailingIcon = {
                                if (selectedTickers.size > 2) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Remove",
                                        tint = TextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            },
                            colors = InputChipDefaults.inputChipColors(
                                selectedContainerColor = SurfaceCardElevated,
                                selectedLabelColor = CyanAccent
                            )
                        )
                    }

                    if (selectedTickers.size < 5) {
                        IconButton(
                            onClick = { showAddDialog = true },
                            modifier = Modifier.background(SurfaceCard, RoundedCornerShape(8.dp))
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add stock", tint = CyanAccent)
                        }
                    }
                }
            }

            // Comparison Matrix Table (Horizontally Scrollable)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SurfaceCardBorder))
                ) {
                    val scrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                            .horizontalScroll(scrollState)
                    ) {
                        // Header Row (Tickers)
                        Row(modifier = Modifier.padding(vertical = 8.dp)) {
                            TableCell("Metric", isHeader = true, width = 130.dp)
                            comparedStocks.forEach { stock ->
                                TableCell(stock.ticker, isHeader = true, width = 110.dp, color = CyanAccent)
                            }
                        }
                        HorizontalDivider(color = SurfaceCardBorder)

                        CompareRow("Current Price", comparedStocks) { "$${it.price}" }
                        CompareRow("Daily Change", comparedStocks) { "${if (it.changePercent >= 0) "+" else ""}${it.changePercent}%" }
                        CompareRow("Market Cap", comparedStocks) { "$${it.marketCapBillions}B" }
                        CompareRow("P/E Ratio", comparedStocks) { "${it.peRatio}x" }
                        CompareRow("Forward P/E", comparedStocks) { "${it.forwardPe}x" }
                        CompareRow("PEG Ratio", comparedStocks) { "${it.pegRatio}" }
                        CompareRow("Price/Sales", comparedStocks) { "${it.priceToSales}x" }
                        CompareRow("EV/EBITDA", comparedStocks) { "${it.evToEbitda}x" }
                        CompareRow("RSI (14)", comparedStocks) { "${it.rsi14.toInt()}" }
                        CompareRow("Beta (Risk)", comparedStocks) { "${it.beta}" }
                        CompareRow("Sector", comparedStocks) { it.sector }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        StockSearchDialog(
            allStocks = allStocks,
            onDismiss = { showAddDialog = false },
            onStockSelected = { selected ->
                if (!selectedTickers.contains(selected.ticker) && selectedTickers.size < 5) {
                    selectedTickers.add(selected.ticker)
                }
            }
        )
    }
}

@Composable
fun CompareRow(
    metricName: String,
    stocks: List<StockQuote>,
    valueExtractor: (StockQuote) -> String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TableCell(metricName, width = 130.dp, color = TextMuted)
        stocks.forEach { stock ->
            TableCell(valueExtractor(stock), width = 110.dp, color = TextPrimary)
        }
    }
    HorizontalDivider(color = SurfaceCardBorder.copy(alpha = 0.5f))
}

@Composable
fun TableCell(
    text: String,
    width: androidx.compose.ui.unit.Dp,
    isHeader: Boolean = false,
    color: androidx.compose.ui.graphics.Color = TextPrimary
) {
    Box(
        modifier = Modifier
            .width(width)
            .padding(horizontal = 6.dp)
    ) {
        Text(
            text = text,
            style = if (isHeader) MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            else MaterialTheme.typography.bodySmall,
            color = color,
            maxLines = 1
        )
    }
}
