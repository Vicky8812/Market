package com.example.data.remote

import android.util.Log
import com.example.data.model.CandlePoint
import com.example.data.model.MarketIndex
import com.example.data.model.StockQuote
import com.example.data.model.Timeframe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.round

class LiveMarketService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    // Fetch live quote metadata for a given symbol
    suspend fun fetchLiveQuote(symbol: String, defaultQuote: StockQuote): StockQuote = withContext(Dispatchers.IO) {
        try {
            val encodedSymbol = java.net.URLEncoder.encode(symbol, "UTF-8")
            val url = "https://query1.finance.yahoo.com/v8/finance/chart/$encodedSymbol?interval=1d&range=5d"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .header("Accept", "application/json")
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext defaultQuote

            val json = JSONObject(body)
            val chart = json.optJSONObject("chart") ?: return@withContext defaultQuote
            val resultArr = chart.optJSONArray("result") ?: return@withContext defaultQuote
            if (resultArr.length() == 0) return@withContext defaultQuote

            val resultObj = resultArr.getJSONObject(0)
            val meta = resultObj.optJSONObject("meta") ?: return@withContext defaultQuote

            val price = meta.optDouble("regularMarketPrice", defaultQuote.price)
            val prevClose = meta.optDouble("chartPreviousClose", defaultQuote.price - defaultQuote.change)
            val change = if (prevClose > 0) round((price - prevClose) * 100) / 100.0 else defaultQuote.change
            val changePct = if (prevClose > 0) round(((price - prevClose) / prevClose * 100) * 100) / 100.0 else defaultQuote.changePercent
            val high52 = meta.optDouble("fiftyTwoWeekHigh", defaultQuote.high52W)
            val low52 = meta.optDouble("fiftyTwoWeekLow", defaultQuote.low52W)
            val dayHigh = meta.optDouble("regularMarketDayHigh", defaultQuote.high)
            val dayLow = meta.optDouble("regularMarketDayLow", defaultQuote.low)
            val volume = meta.optLong("regularMarketVolume", defaultQuote.volume)

            val nowFormatted = SimpleDateFormat("HH:mm:ss 'ET'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("America/New_York")
            }.format(Date())

            defaultQuote.copy(
                price = round(price * 100) / 100.0,
                change = change,
                changePercent = changePct,
                high = dayHigh,
                low = dayLow,
                high52W = high52,
                low52W = low52,
                volume = volume,
                timestamp = nowFormatted,
                isRealTime = true
            )
        } catch (e: Exception) {
            Log.w("LiveMarketService", "Error fetching quote for $symbol: ${e.message}")
            defaultQuote
        }
    }

    // Fetch live data for index (e.g. ^GSPC, ^IXIC)
    suspend fun fetchLiveIndex(symbol: String, defaultIndex: MarketIndex): MarketIndex = withContext(Dispatchers.IO) {
        try {
            val encodedSymbol = java.net.URLEncoder.encode(symbol, "UTF-8")
            val url = "https://query1.finance.yahoo.com/v8/finance/chart/$encodedSymbol?interval=1d&range=5d"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0")
                .header("Accept", "application/json")
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext defaultIndex

            val json = JSONObject(body)
            val resultArr = json.optJSONObject("chart")?.optJSONArray("result") ?: return@withContext defaultIndex
            if (resultArr.length() == 0) return@withContext defaultIndex

            val resultObj = resultArr.getJSONObject(0)
            val meta = resultObj.optJSONObject("meta") ?: return@withContext defaultIndex

            val price = meta.optDouble("regularMarketPrice", defaultIndex.currentPrice)
            val prevClose = meta.optDouble("chartPreviousClose", defaultIndex.currentPrice - defaultIndex.change)
            val change = if (prevClose > 0) round((price - prevClose) * 100) / 100.0 else defaultIndex.change
            val changePct = if (prevClose > 0) round(((price - prevClose) / prevClose * 100) * 100) / 100.0 else defaultIndex.changePercent

            // Extract real sparkline closing prices if available
            val indicators = resultObj.optJSONObject("indicators")
            val quoteArr = indicators?.optJSONArray("quote")
            val sparkline = mutableListOf<Double>()
            if (quoteArr != null && quoteArr.length() > 0) {
                val closeArr = quoteArr.getJSONObject(0).optJSONArray("close")
                if (closeArr != null) {
                    for (i in 0 until closeArr.length()) {
                        if (!closeArr.isNull(i)) {
                            val c = closeArr.optDouble(i)
                            if (c > 0) sparkline.add(c)
                        }
                    }
                }
            }

            defaultIndex.copy(
                currentPrice = round(price * 100) / 100.0,
                change = change,
                changePercent = changePct,
                sparkline = if (sparkline.size >= 2) sparkline else defaultIndex.sparkline
            )
        } catch (e: Exception) {
            Log.w("LiveMarketService", "Error fetching index for $symbol: ${e.message}")
            defaultIndex
        }
    }

    // Fetch live USD to INR exchange rate
    suspend fun fetchLiveUsdInrRate(): Double = withContext(Dispatchers.IO) {
        try {
            val url = "https://query1.finance.yahoo.com/v8/finance/chart/USDINR=X?interval=1d&range=1d"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0")
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext 84.20

            val json = JSONObject(body)
            val resultArr = json.optJSONObject("chart")?.optJSONArray("result")
            if (resultArr != null && resultArr.length() > 0) {
                val meta = resultArr.getJSONObject(0).optJSONObject("meta")
                val rate = meta?.optDouble("regularMarketPrice", 84.20) ?: 84.20
                if (rate > 50.0) {
                    return@withContext round(rate * 100) / 100.0
                }
            }
            84.20
        } catch (e: Exception) {
            84.20
        }
    }

    // Fetch live real candles for given ticker and timeframe
    suspend fun fetchLiveCandles(symbol: String, timeframe: Timeframe): List<CandlePoint>? = withContext(Dispatchers.IO) {
        try {
            val (range, interval) = when (timeframe) {
                Timeframe.ONE_DAY -> Pair("1d", "15m")
                Timeframe.FIVE_DAYS -> Pair("5d", "30m")
                Timeframe.ONE_MONTH -> Pair("1mo", "1d")
                Timeframe.THREE_MONTHS -> Pair("3mo", "1d")
                Timeframe.ONE_YEAR -> Pair("1y", "1wk")
                Timeframe.FIVE_YEARS -> Pair("5y", "1mo")
            }

            val encodedSymbol = java.net.URLEncoder.encode(symbol, "UTF-8")
            val url = "https://query1.finance.yahoo.com/v8/finance/chart/$encodedSymbol?interval=$interval&range=$range"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0")
                .header("Accept", "application/json")
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext null

            val json = JSONObject(body)
            val resultArr = json.optJSONObject("chart")?.optJSONArray("result") ?: return@withContext null
            if (resultArr.length() == 0) return@withContext null

            val resultObj = resultArr.getJSONObject(0)
            val timestampArr = resultObj.optJSONArray("timestamp") ?: return@withContext null
            val indicators = resultObj.optJSONObject("indicators") ?: return@withContext null
            val quoteArr = indicators.optJSONArray("quote") ?: return@withContext null
            if (quoteArr.length() == 0) return@withContext null

            val quoteObj = quoteArr.getJSONObject(0)
            val openArr = quoteObj.optJSONArray("open") ?: return@withContext null
            val highArr = quoteObj.optJSONArray("high") ?: return@withContext null
            val lowArr = quoteObj.optJSONArray("low") ?: return@withContext null
            val closeArr = quoteObj.optJSONArray("close") ?: return@withContext null
            val volumeArr = quoteObj.optJSONArray("volume")

            val candles = mutableListOf<CandlePoint>()
            val dateFormat = when (timeframe) {
                Timeframe.ONE_DAY -> SimpleDateFormat("HH:mm", Locale.US).apply { timeZone = TimeZone.getTimeZone("America/New_York") }
                Timeframe.FIVE_DAYS -> SimpleDateFormat("EEE HH:mm", Locale.US).apply { timeZone = TimeZone.getTimeZone("America/New_York") }
                Timeframe.ONE_MONTH, Timeframe.THREE_MONTHS -> SimpleDateFormat("MMM dd", Locale.US)
                Timeframe.ONE_YEAR, Timeframe.FIVE_YEARS -> SimpleDateFormat("MMM yyyy", Locale.US)
            }

            for (i in 0 until timestampArr.length()) {
                if (openArr.isNull(i) || highArr.isNull(i) || lowArr.isNull(i) || closeArr.isNull(i)) continue
                val t = timestampArr.getLong(i) * 1000L
                val o = openArr.getDouble(i)
                val h = highArr.getDouble(i)
                val l = lowArr.getDouble(i)
                val c = closeArr.getDouble(i)
                val v = if (volumeArr != null && !volumeArr.isNull(i)) volumeArr.getLong(i) else 0L

                if (o > 0 && h > 0 && l > 0 && c > 0) {
                    candles.add(
                        CandlePoint(
                            timestamp = t,
                            dateLabel = dateFormat.format(Date(t)),
                            open = round(o * 100) / 100.0,
                            high = round(h * 100) / 100.0,
                            low = round(l * 100) / 100.0,
                            close = round(c * 100) / 100.0,
                            volume = v
                        )
                    )
                }
            }

            if (candles.size >= 5) candles else null
        } catch (e: Exception) {
            Log.w("LiveMarketService", "Failed live candles for $symbol: ${e.message}")
            null
        }
    }
}
