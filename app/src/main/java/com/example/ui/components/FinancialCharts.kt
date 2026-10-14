package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CandlePoint
import com.example.ui.theme.*
import kotlin.math.max
import kotlin.math.min

enum class ChartType {
    CANDLESTICK,
    LINE
}

@Composable
fun InteractiveFinancialChart(
    candles: List<CandlePoint>,
    modifier: Modifier = Modifier,
    isPositiveTrend: Boolean = true,
    showVolume: Boolean = true
) {
    if (candles.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(SurfaceCard, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("No chart data available", color = TextMuted)
        }
        return
    }

    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    var chartType by remember { mutableStateOf(ChartType.CANDLESTICK) }

    val activeColor = if (isPositiveTrend) BullGreen else BearRed
    val activeColorMuted = if (isPositiveTrend) BullGreenBg else BearRedBg

    val minPrice = candles.minOf { it.low }
    val maxPrice = candles.maxOf { it.high }
    val priceRange = if (maxPrice - minPrice > 0) maxPrice - minPrice else 1.0
    val maxVolume = candles.maxOfOrNull { it.volume } ?: 1L

    val textMeasurer = rememberTextMeasurer()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceCard, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        // Chart Header with Selected Point Telemetry & Chart Type Switch
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val displayPoint = if (selectedIndex != null && selectedIndex in candles.indices) {
                candles[selectedIndex!!]
            } else {
                candles.last()
            }

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$${String.format(java.util.Locale.US, "%.2f", displayPoint.close)}",
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp),
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    val diff = displayPoint.close - displayPoint.open
                    val pct = (diff / displayPoint.open) * 100
                    Text(
                        text = "${if (diff >= 0) "+" else ""}${String.format(java.util.Locale.US, "%.2f", diff)} (${String.format(java.util.Locale.US, "%.2f", pct)}%)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (diff >= 0) BullGreen else BearRed
                    )
                }
                Text(
                    text = "O: ${displayPoint.open}  H: ${displayPoint.high}  L: ${displayPoint.low}  |  ${displayPoint.dateLabel}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }

            // Mode Selector: Candlestick vs Line
            Row(
                modifier = Modifier
                    .background(BackgroundDark, RoundedCornerShape(8.dp))
                    .padding(2.dp)
            ) {
                Surface(
                    onClick = { chartType = ChartType.CANDLESTICK },
                    color = if (chartType == ChartType.CANDLESTICK) SurfaceCardElevated else Color.Transparent,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "Candles",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (chartType == ChartType.CANDLESTICK) CyanAccent else TextMuted
                    )
                }
                Surface(
                    onClick = { chartType = ChartType.LINE },
                    color = if (chartType == ChartType.LINE) SurfaceCardElevated else Color.Transparent,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "Line",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (chartType == ChartType.LINE) CyanAccent else TextMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Canvas for Drawing Candlesticks / Line + Volume
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .pointerInput(candles) {
                    detectTapGestures(
                        onPress = { offset ->
                            val xFraction = (offset.x / size.width).coerceIn(0f, 1f)
                            val idx = (xFraction * (candles.size - 1)).toInt().coerceIn(0, candles.size - 1)
                            selectedIndex = idx
                        }
                    )
                }
        ) {
            val width = size.width
            val height = size.height
            val chartHeight = if (showVolume) height * 0.76f else height
            val volumeHeight = if (showVolume) height * 0.20f else 0f
            val spacing = width / candles.size.toFloat()
            val candleBarWidth = max(2.5f, spacing * 0.65f)

            // Grid lines
            val gridLevels = 4
            for (i in 0..gridLevels) {
                val y = (chartHeight / gridLevels) * i
                drawLine(
                    color = SurfaceCardBorder.copy(alpha = 0.5f),
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1f
                )
                val priceAtLevel = maxPrice - (i.toFloat() / gridLevels) * priceRange
                drawText(
                    textMeasurer = textMeasurer,
                    text = String.format(java.util.Locale.US, "$%.1f", priceAtLevel),
                    topLeft = Offset(width - 50.dp.toPx(), y + 2.dp.toPx()),
                    style = TextStyle(color = TextMuted, fontSize = 9.sp)
                )
            }

            if (chartType == ChartType.CANDLESTICK) {
                // Candlestick rendering
                candles.forEachIndexed { index, candle ->
                    val x = index * spacing + spacing / 2f
                    val isUp = candle.close >= candle.open
                    val candleColor = if (isUp) BullGreen else BearRed

                    // Normalizing Y coordinates
                    val highY = chartHeight - ((candle.high - minPrice) / priceRange * chartHeight).toFloat()
                    val lowY = chartHeight - ((candle.low - minPrice) / priceRange * chartHeight).toFloat()
                    val openY = chartHeight - ((candle.open - minPrice) / priceRange * chartHeight).toFloat()
                    val closeY = chartHeight - ((candle.close - minPrice) / priceRange * chartHeight).toFloat()

                    // Draw Wick
                    drawLine(
                        color = candleColor,
                        start = Offset(x, highY),
                        end = Offset(x, lowY),
                        strokeWidth = 1.5f
                    )

                    // Draw Candle Body
                    val topBodyY = min(openY, closeY)
                    val bodyHeight = max(2f, kotlin.math.abs(closeY - openY))

                    drawRect(
                        color = candleColor,
                        topLeft = Offset(x - candleBarWidth / 2f, topBodyY),
                        size = Size(candleBarWidth, bodyHeight)
                    )

                    // Volume Bar at Bottom
                    if (showVolume) {
                        val volHeightFraction = (candle.volume.toFloat() / maxVolume.toFloat()).coerceIn(0f, 1f)
                        val vY = height - (volHeightFraction * volumeHeight)
                        drawRect(
                            color = candleColor.copy(alpha = 0.45f),
                            topLeft = Offset(x - candleBarWidth / 2f, vY),
                            size = Size(candleBarWidth, height - vY)
                        )
                    }
                }
            } else {
                // Smooth Area & Line rendering
                val linePath = Path()
                val areaPath = Path()

                candles.forEachIndexed { index, candle ->
                    val x = index * spacing + spacing / 2f
                    val y = chartHeight - ((candle.close - minPrice) / priceRange * chartHeight).toFloat()
                    if (index == 0) {
                        linePath.moveTo(x, y)
                        areaPath.moveTo(x, chartHeight)
                        areaPath.lineTo(x, y)
                    } else {
                        linePath.lineTo(x, y)
                        areaPath.lineTo(x, y)
                    }
                }

                if (candles.isNotEmpty()) {
                    val lastX = (candles.size - 1) * spacing + spacing / 2f
                    areaPath.lineTo(lastX, chartHeight)
                    areaPath.close()

                    // Draw Gradient Fill
                    drawPath(
                        path = areaPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                activeColor.copy(alpha = 0.35f),
                                Color.Transparent
                            ),
                            startY = 0f,
                            endY = chartHeight
                        )
                    )

                    // Draw Trend Line
                    drawPath(
                        path = linePath,
                        color = activeColor,
                        style = Stroke(width = 2.5.dp.toPx())
                    )
                }
            }

            // Crosshair overlay if touched
            selectedIndex?.let { idx ->
                if (idx in candles.indices) {
                    val crossX = idx * spacing + spacing / 2f
                    drawLine(
                        color = CyanAccent.copy(alpha = 0.7f),
                        start = Offset(crossX, 0f),
                        end = Offset(crossX, height),
                        strokeWidth = 1.2.dp.toPx()
                    )
                    val candle = candles[idx]
                    val crossY = chartHeight - ((candle.close - minPrice) / priceRange * chartHeight).toFloat()
                    drawCircle(
                        color = CyanAccent,
                        radius = 4.5.dp.toPx(),
                        center = Offset(crossX, crossY)
                    )
                }
            }
        }
    }
}
