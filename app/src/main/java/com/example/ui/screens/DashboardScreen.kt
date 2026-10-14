package com.example.ui.screens

import kotlinx.coroutines.launch
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
import com.example.data.model.*
import com.example.data.repository.MarketRepository
import com.example.ui.components.*
import com.example.ui.theme.*

enum class DashboardTab(val label: String) {
    GAINERS("Top Gainers"),
    LOSERS("Top Losers"),
    ACTIVE("Most Active"),
    UNUSUAL_VOL("Unusual Vol"),
    NEAR_52W_HIGH("52W Highs"),
    NEAR_52W_LOW("52W Lows")
}

@Composable
fun DashboardScreen(
    repository: MarketRepository,
    onStockClick: (String) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenAlerts: () -> Unit,
    onOpenBacktest: () -> Unit,
    onCompareClick: () -> Unit,
    onOpenIndiaHub: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val liveQuotes by repository.liveQuotes.collectAsState()
    val indices by repository.liveIndices.collectAsState()
    val isRefreshing by repository.isRefreshing.collectAsState()
    val lastRefreshTime by repository.lastRefreshTime.collectAsState()

    val sectors = remember { repository.getSectorPerformance() }
    val economicEvents = remember { repository.getTodayEconomicEvents() }
    val dailySummary = remember { repository.getDailyMarketSummary() }
    val newsList = remember { repository.getMarketNews() }

    // Live 1-second clock ticker for real-time ET & IST countdown
    var currentMarketStatus by remember { mutableStateOf(repository.getMarketStatus()) }
    LaunchedEffect(Unit) {
        while (true) {
            currentMarketStatus = repository.getMarketStatus()
            kotlinx.coroutines.delay(1000)
        }
    }

    // Auto-fetch real-time market data on launch and periodically
    LaunchedEffect(Unit) {
        repository.refreshLiveMarketData()
        while (true) {
            kotlinx.coroutines.delay(20000)
            repository.refreshLiveMarketData()
        }
    }

    var selectedTab by remember { mutableStateOf(DashboardTab.GAINERS) }
    var summaryExpanded by remember { mutableStateOf(false) }

    val displayedStocks = remember(selectedTab, liveQuotes) {
        when (selectedTab) {
            DashboardTab.GAINERS -> liveQuotes.sortedByDescending { it.changePercent }.take(5)
            DashboardTab.LOSERS -> liveQuotes.sortedBy { it.changePercent }.take(5)
            DashboardTab.ACTIVE -> liveQuotes.sortedByDescending { it.volume }.take(5)
            DashboardTab.UNUSUAL_VOL -> liveQuotes.filter { it.volume > it.avgVolume30D * 1.3 }
            DashboardTab.NEAR_52W_HIGH -> liveQuotes.filter { (it.high52W - it.price) / it.high52W <= 0.05 }
            DashboardTab.NEAR_52W_LOW -> liveQuotes.filter { (it.price - it.low52W) / it.low52W <= 0.08 }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
    ) {
        // Top App Bar Controls
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "MARKET",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            ),
                            color = TextPrimary
                        )
                        Text(
                            text = "PULSE",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            ),
                            color = CyanAccent
                        )
                    }
                    Text(
                        text = "US Market Intelligence & Quantitative AI",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(
                        onClick = onCompareClick,
                        modifier = Modifier
                            .background(SurfaceCard, RoundedCornerShape(10.dp))
                            .size(38.dp)
                            .testTag("compare_button")
                    ) {
                        Icon(Icons.Default.CompareArrows, contentDescription = "Compare", tint = CyanAccent)
                    }
                    IconButton(
                        onClick = onOpenBacktest,
                        modifier = Modifier
                            .background(SurfaceCard, RoundedCornerShape(10.dp))
                            .size(38.dp)
                            .testTag("backtest_button")
                    ) {
                        Icon(Icons.Default.QueryStats, contentDescription = "Model Backtesting", tint = GoldAccent)
                    }
                    IconButton(
                        onClick = onOpenAlerts,
                        modifier = Modifier
                            .background(SurfaceCard, RoundedCornerShape(10.dp))
                            .size(38.dp)
                            .testTag("alerts_button")
                    ) {
                        Icon(Icons.Default.NotificationsNone, contentDescription = "Alerts", tint = TextPrimary)
                    }
                    IconButton(
                        onClick = onOpenIndiaHub,
                        modifier = Modifier
                            .background(SurfaceCard, RoundedCornerShape(10.dp))
                            .size(38.dp)
                            .testTag("india_hub_button")
                    ) {
                        Text("🇮🇳", fontSize = 16.sp)
                    }
                    IconButton(
                        onClick = onOpenSearch,
                        modifier = Modifier
                            .background(SurfaceCard, RoundedCornerShape(10.dp))
                            .size(38.dp)
                            .testTag("search_button")
                    ) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = CyanAccent)
                    }
                }
            }
        }

        // Market Session Status Card
        item {
            MarketStatusHeader(
                marketStatus = currentMarketStatus,
                onRefreshClick = {
                    coroutineScope.launch {
                        repository.refreshLiveMarketData()
                    }
                },
                isRefreshing = isRefreshing
            )
        }

        // India Investor Action Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenIndiaHub)
                    .testTag("india_investor_banner"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCardElevated),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(GoldAccent.copy(alpha = 0.5f)))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🇮🇳", fontSize = 26.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Investing in US Markets from India?",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(color = GoldAccent.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp)) {
                                Text("LRS & Tax", modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp), style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold), color = GoldAccent)
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "RBI $250k LRS limit, 20% TCS credit, 0% US tax (W-8BEN) & 7 PM IST hours guide",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = TextSecondary
                        )
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = CyanAccent)
                }
            }
        }

        // Major Indices Carousel
        item {
            Column {
                Text(
                    text = "MAJOR US INDICES",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(indices) { index ->
                        Card(
                            modifier = Modifier
                                .width(168.dp)
                                .clip(RoundedCornerShape(14.dp)),
                            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(SurfaceCardBorder)
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = index.name,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = TextSecondary,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = String.format(java.util.Locale.US, "%,.2f", index.currentPrice),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    PriceChangeChip(
                                        change = index.change,
                                        changePercent = index.changePercent
                                    )
                                    MiniSparkline(
                                        data = index.sparkline,
                                        isUp = index.changePercent >= 0
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // AI Daily Market Summary
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(CyanAccent.copy(alpha = 0.35f))
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = CyanAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "AI Daily Market Intelligence Brief",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                        }
                        TextButton(
                            onClick = { summaryExpanded = !summaryExpanded }
                        ) {
                            Text(
                                text = if (summaryExpanded) "Collapse" else "Expand",
                                color = CyanAccent,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (summaryExpanded) dailySummary else dailySummary.take(240) + "...",
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 19.sp),
                        color = TextSecondary
                    )
                }
            }
        }

        // Market Screen Movers Tabs
        item {
            Column {
                Text(
                    text = "MARKET MOVERS & SCREENERS",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(DashboardTab.values()) { tab ->
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
        }

        // Stocks in selected category
        items(displayedStocks) { quote ->
            StockQuoteCard(
                quote = quote,
                onClick = { onStockClick(quote.ticker) }
            )
        }

        // US Sector Performance Heatmap
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
                            text = "Sector Breadth & Performance",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Text(
                            text = "11 Sectors",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    sectors.forEach { sec ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1.5f)) {
                                Text(
                                    text = sec.sectorName,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    color = TextPrimary
                                )
                                Text(
                                    text = "${sec.etfSymbol} • Top: ${sec.topContributer}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = TextMuted
                                )
                            }

                            // Breadth progress indicator
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(SurfaceCardElevated)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(sec.marketBreadthAdvancing / 100f)
                                        .background(if (sec.changePercent >= 0) BullGreen else BearRed)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = "${if (sec.changePercent >= 0) "+" else ""}${sec.changePercent}%",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = if (sec.changePercent >= 0) BullGreen else BearRed
                            )
                        }
                    }
                }
            }
        }

        // Today's Economic Events Calendar
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
                            text = "Today's Economic Calendar",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Surface(
                            color = Color(0x26FFB300),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "US Macro",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = GoldAccent
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    economicEvents.forEach { event ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = event.title,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = TextPrimary
                                )
                                Text(
                                    text = event.timeET,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CyanAccent
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Consensus: ${event.consensus}  |  Prev: ${event.previous}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    color = TextMuted
                                )
                                if (event.actual != null) {
                                    Text(
                                        text = "Actual: ${event.actual}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = BullGreen
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = event.impactNote,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            HorizontalDivider(color = SurfaceCardBorder.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }

        // Breaking News Section
        item {
            Column {
                Text(
                    text = "LATEST US FINANCIAL HEADLINES & AI SYNTHESIS",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(8.dp))

                newsList.take(3).forEach { article ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SurfaceCardBorder))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${article.publisher} • ${article.publishedTimeAgo}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted
                                )
                                Surface(
                                    color = when (article.impact) {
                                        NewsImpact.POSITIVE -> BullGreenBg
                                        NewsImpact.NEGATIVE -> BearRedBg
                                        else -> Color(0x26FFB300)
                                    },
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = article.impact.name,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                        color = when (article.impact) {
                                            NewsImpact.POSITIVE -> BullGreen
                                            NewsImpact.NEGATIVE -> BearRed
                                            else -> GoldAccent
                                        }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = article.headline,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = TextPrimary
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = article.summary,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Affected Tickers
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Tickers: ", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                article.affectedTickers.forEach { tick ->
                                    Surface(
                                        color = SurfaceCardElevated,
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier.clickable { onStockClick(tick) }
                                    ) {
                                        Text(
                                            text = tick,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = CyanAccent
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
