package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.model.PriceAlert
import com.example.data.repository.MarketRepository
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun AlertsDialog(
    repository: MarketRepository,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val alerts by repository.allAlerts.collectAsState(initial = emptyList())

    var showCreateForm by remember { mutableStateOf(false) }
    var tickerInput by remember { mutableStateOf("NVDA") }
    var targetInput by remember { mutableStateOf("145.00") }
    var condition by remember { mutableStateOf("ABOVE") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.82f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = CyanAccent)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Market Alerts Manager",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (showCreateForm) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Create Price Target Alert", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                            OutlinedTextField(
                                value = tickerInput,
                                onValueChange = { tickerInput = it.uppercase() },
                                label = { Text("Ticker Symbol") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = condition == "ABOVE",
                                    onClick = { condition = "ABOVE" },
                                    label = { Text("Rises Above") },
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = BullGreenBg, selectedLabelColor = BullGreen)
                                )
                                FilterChip(
                                    selected = condition == "BELOW",
                                    onClick = { condition = "BELOW" },
                                    label = { Text("Drops Below") },
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = BearRedBg, selectedLabelColor = BearRed)
                                )
                            }
                            OutlinedTextField(
                                value = targetInput,
                                onValueChange = { targetInput = it },
                                label = { Text("Target Price ($)") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        val p = targetInput.toDoubleOrNull() ?: 100.0
                                        coroutineScope.launch {
                                            repository.addAlert(tickerInput, p, condition, "Alert when $tickerInput is $condition $p")
                                            showCreateForm = false
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
                                ) {
                                    Text("Save Alert", color = BackgroundDark)
                                }
                                TextButton(onClick = { showCreateForm = false }) {
                                    Text("Cancel", color = TextMuted)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                } else {
                    Button(
                        onClick = { showCreateForm = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceCardElevated),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = CyanAccent)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("New Price Alert", color = TextPrimary)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Text(
                    text = "ACTIVE ALERTS (${alerts.size})",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(6.dp))

                if (alerts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No active alerts set yet.", color = TextMuted)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(alerts) { alert ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(alert.ticker, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                                        Text(
                                            text = "Trigger when price is ${alert.condition} $${alert.targetPrice}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary
                                        )
                                    }
                                    IconButton(onClick = {
                                        coroutineScope.launch { repository.deleteAlert(alert.id) }
                                    }) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = TextMuted)
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
