package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.data.model.DEFAULT_USD_TO_INR_RATE
import com.example.data.repository.MarketRepository
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun IndiaInvestorHubScreen(
    repository: MarketRepository,
    onBack: () -> Unit,
    onNavigateToChatWithPrompt: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val marketStatus = remember { repository.getMarketStatus() }
    var inrInput by remember { mutableStateOf("500000") }
    var priorRemittedInput by remember { mutableStateOf("0") }

    val inrAmount = inrInput.toDoubleOrNull() ?: 500000.0
    val priorRemitted = priorRemittedInput.toDoubleOrNull() ?: 0.0

    val remittanceResult = remember(inrAmount, priorRemitted) {
        repository.calculateRemittance(inrAmount, priorRemitted)
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
                IconButton(onClick = onBack, modifier = Modifier.testTag("india_hub_back")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
                Text(
                    text = "🇮🇳 India to US Investing Hub",
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 96.dp)
        ) {
            // Market Hours Banner (ET vs IST)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CyanAccent.copy(alpha = 0.4f)))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "US Market Hours in Indian Standard Time (IST)",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Surface(
                                color = CyanAccentBg,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "IST Hub",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = CyanAccent
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("New York (ET)", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                Text(marketStatus.timeET, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("India Time (IST)", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                Text(marketStatus.timeIST, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color = CyanAccent)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = SurfaceCardBorder)
                        Spacer(modifier = Modifier.height(10.dp))

                        // IST Schedule Breakdown
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            ScheduleRow("Pre-Market Session", "1:30 PM - 7:00 PM IST", "Early order flow & news reactions")
                            ScheduleRow("Regular Trading Session", "7:00 PM - 1:30 AM IST", "Prime high-liquidity trading (After Indian office hours!)", isHighlight = true)
                            ScheduleRow("After-Hours Session", "1:30 AM - 5:30 AM IST", "Earnings announcements & guidance calls")
                        }
                    }
                }
            }

            // Interactive RBI LRS & TCS Remittance Calculator
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
                                text = "RBI LRS & TCS Remittance Calculator",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Surface(color = BullGreenBg, shape = RoundedCornerShape(6.dp)) {
                                Text(
                                    text = "1 USD ≈ ₹${String.format(Locale.US, "%.2f", DEFAULT_USD_TO_INR_RATE)}",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = BullGreen
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Under RBI's Liberalised Remittance Scheme (LRS), Indian residents can remit up to $250,000 USD (~₹2.1 Crore) per financial year for overseas equities.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Input fields
                        OutlinedTextField(
                            value = inrInput,
                            onValueChange = { inrInput = it },
                            label = { Text("INR Amount to Remit (₹)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("inr_remit_input"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanAccent,
                                unfocusedBorderColor = SurfaceCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = priorRemittedInput,
                            onValueChange = { priorRemittedInput = it },
                            label = { Text("Prior Remitted in Current FY (₹)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanAccent,
                                unfocusedBorderColor = SurfaceCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Calculation Results Card
                        Surface(
                            color = SurfaceCardElevated,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Estimated USD Received", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                    Text("$${String.format(Locale.US, "%,.2f", remittanceResult.usdReceived)} USD", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = BullGreen)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Effective FX Rate (incl. ~1% spread)", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                    Text("₹${remittanceResult.effectiveRate} / USD", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold), color = TextPrimary)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("TCS (Tax Collected at Source)", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                    Text(
                                        if (remittanceResult.isTcsApplicable) "20% (₹${String.format(Locale.US, "%,.0f", remittanceResult.tcsAmountInr)})" else "0% (Within ₹7L Exemption)",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (remittanceResult.isTcsApplicable) GoldAccent else BullGreen
                                    )
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Total Bank Outflow (Remittance + TCS)", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                    Text("₹${String.format(Locale.US, "%,.0f", remittanceResult.totalInrDeducted)}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                HorizontalDivider(color = SurfaceCardBorder)
                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = remittanceResult.tcsRefundNote,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 16.sp),
                                    color = if (remittanceResult.isTcsApplicable) GoldAccent else TextSecondary
                                )
                            }
                        }
                    }
                }
            }

            // India-US Taxation Guide
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SurfaceCardBorder))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "US & India Taxation Rules (Essential Guide)",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )

                        TaxItem(
                            title = "Form W-8BEN (0% US Capital Gains Tax)",
                            desc = "Your US broker automatically files Form W-8BEN to certify you as an Indian tax resident. You pay ZERO capital gains tax in the United States."
                        )

                        TaxItem(
                            title = "US Dividends & DTAA (Foreign Tax Credit)",
                            desc = "US companies deduct 25% withholding tax on dividends paid to Indian residents. You can claim this 25% back as Foreign Tax Credit (FTC) in India using Form 67 under the US-India DTAA treaty."
                        )

                        TaxItem(
                            title = "Indian Capital Gains (ITR Rules)",
                            desc = "• Long-Term Capital Gains (held > 24 months): Taxed at 12.5% (effective FY 2024-25 onwards).\n• Short-Term Capital Gains (held <= 24 months): Added to your total taxable income and taxed at your individual slab rate."
                        )

                        TaxItem(
                            title = "Mandatory Reporting: Schedule FA",
                            desc = "All foreign stock accounts and holdings must be declared in Schedule FA of your annual Indian Income Tax Return (ITR-2 or ITR-3) to avoid steep non-disclosure penalties under the Black Money Act."
                        )
                    }
                }
            }

            // Indian Investor Starter Strategy & AI Prompt
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCardElevated),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CyanAccent.copy(alpha = 0.5f)))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = CyanAccent)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Ask MarketPulse AI for Your Custom US Plan",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Get an intelligent multi-turn portfolio allocation plan taking into account your INR budget, risk tolerance, and USD compounding.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                onNavigateToChatWithPrompt("I am an Indian investor with ₹5,00,000 to invest in US stocks under LRS. Recommend an optimal diversified portfolio between S&P 500 ETFs and tech leaders like Nvidia and Microsoft.")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Create My Custom US Investment Plan", color = BackgroundDark, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScheduleRow(title: String, timeIST: String, subtitle: String, isHighlight: Boolean = false) {
    Surface(
        color = if (isHighlight) CyanAccentBg else SurfaceCardElevated,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(title, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = if (isHighlight) CyanAccent else TextPrimary)
                Text(timeIST, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = if (isHighlight) CyanAccent else GoldAccent)
            }
            Text(subtitle, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = TextMuted)
        }
    }
}

@Composable
fun TaxItem(title: String, desc: String) {
    Surface(
        color = SurfaceCardElevated,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = CyanAccent)
            Spacer(modifier = Modifier.height(4.dp))
            Text(desc, style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 16.sp), color = TextSecondary)
        }
    }
}
