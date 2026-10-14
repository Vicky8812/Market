package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StockSuggestion
import com.example.data.model.SuggestionCategory
import com.example.data.repository.MarketRepository
import com.example.ui.components.PriceChangeChip
import com.example.ui.components.RiskBadge
import com.example.ui.theme.*

@Composable
fun DiscoveryScreen(
    repository: MarketRepository,
    onStockClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf<SuggestionCategory?>(null) }
    val allSuggestions = remember(selectedCategory) {
        repository.getStockSuggestions(selectedCategory)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
    ) {
        item {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = CyanAccent,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AI Stock Discovery Engine",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Quantitative multi-factor rankings evaluated across fundamentals, momentum, valuation, cash flow, and catalysts.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        // Category Filter Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { selectedCategory = null },
                        label = { Text("All Strategies") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyanAccentBg,
                            selectedLabelColor = CyanAccent,
                            containerColor = SurfaceCard,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedCategory == null,
                            borderColor = if (selectedCategory == null) CyanAccent else SurfaceCardBorder
                        )
                    )
                }
                items(SuggestionCategory.values()) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text("${category.icon} ${category.displayName}") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyanAccentBg,
                            selectedLabelColor = CyanAccent,
                            containerColor = SurfaceCard,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedCategory == category,
                            borderColor = if (selectedCategory == category) CyanAccent else SurfaceCardBorder
                        )
                    )
                }
            }
        }

        // Suggestion Cards
        items(allSuggestions) { item ->
            StockSuggestionCard(
                suggestion = item,
                onClick = { onStockClick(item.ticker) }
            )
        }

        // Institutional Methodology Footnote
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard.copy(alpha = 0.6f)),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SurfaceCardBorder))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = GoldAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Every recommendation is supported by empirical factor scoring. We never recommend a stock solely based on positive social media headlines or short-term momentum. Past model performance is no guarantee of future returns.",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 16.sp),
                        color = TextMuted
                    )
                }
            }
        }
    }
}

@Composable
fun StockSuggestionCard(
    suggestion: StockSuggestion,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("suggestion_card_${suggestion.ticker}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SurfaceCardBorder))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Ticker, Name, Price, Change, Rank Score Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = suggestion.ticker,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = CyanAccentBg,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "AI Score ${suggestion.rankScore}/100",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = CyanAccent
                                )
                            )
                        }
                    }
                    Text(
                        text = suggestion.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$${String.format(java.util.Locale.US, "%.2f", suggestion.price)}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    PriceChangeChip(
                        change = suggestion.price * (suggestion.changePercent / 100),
                        changePercent = suggestion.changePercent
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Badges row: Category, Valuation, Risk
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = SurfaceCardElevated,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "${suggestion.category.icon} ${suggestion.category.displayName}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        color = TextPrimary
                    )
                }

                Surface(
                    color = SurfaceCardElevated,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = suggestion.valuationStatus,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (suggestion.valuationStatus.contains("Undervalued", ignoreCase = true)) BullGreen else GoldAccent
                    )
                }

                RiskBadge(riskLevel = suggestion.riskLevel)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Why Ranked Section
            Text(
                text = "EVIDENCE & RANKING REASON",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                color = CyanAccent
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = suggestion.rankingReason,
                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Fundamental Health summary
            Text(
                text = "Fundamentals: ${suggestion.fundamentalStrength}",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Catalysts vs Downside Risks preview
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, tint = BullGreen, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Key Catalysts", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = BullGreen)
                    }
                    suggestion.keyCatalysts.take(2).forEach {
                        Text("• $it", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = TextSecondary, maxLines = 1)
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Filled.TrendingDown, contentDescription = null, tint = BearRed, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Downside Risks", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = BearRed)
                    }
                    suggestion.downsideRisks.take(2).forEach {
                        Text("• $it", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = TextSecondary, maxLines = 1)
                    }
                }
            }
        }
    }
}
