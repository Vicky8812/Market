package com.example.data.model

data class MarketStatus(
    val session: MarketSession,
    val timeET: String,
    val timeIST: String = "",
    val isHolidayOrWeekend: Boolean,
    val statusDescription: String,
    val isRealTime: Boolean,
    val countdownText: String = "",
    val sessionProgressFraction: Float = 0f
)

enum class CurrencyMode(val symbol: String, val code: String) {
    USD("$", "USD"),
    INR("₹", "INR")
}

const val DEFAULT_USD_TO_INR_RATE = 84.20

data class RemittanceCalculation(
    val inrAmount: Double,
    val exchangeRate: Double,
    val estimatedBankSpreadPercent: Double = 1.0,
    val effectiveRate: Double,
    val usdReceived: Double,
    val tcsRatePercent: Double,
    val tcsAmountInr: Double,
    val totalInrDeducted: Double,
    val isTcsApplicable: Boolean,
    val tcsRefundNote: String
)

enum class MarketSession {
    PRE_MARKET,
    REGULAR_SESSION,
    AFTER_HOURS,
    CLOSED
}

data class MarketIndex(
    val symbol: String,
    val name: String,
    val currentPrice: Double,
    val change: Double,
    val changePercent: Double,
    val sparkline: List<Double>
)

data class StockQuote(
    val ticker: String,
    val companyName: String,
    val price: Double,
    val change: Double,
    val changePercent: Double,
    val open: Double,
    val high: Double,
    val low: Double,
    val high52W: Double,
    val low52W: Double,
    val volume: Long,
    val avgVolume30D: Long,
    val marketCapBillions: Double,
    val peRatio: Double,
    val forwardPe: Double,
    val pegRatio: Double,
    val priceToSales: Double,
    val evToEbitda: Double,
    val dividendYield: Double,
    val beta: Double,
    val rsi14: Double,
    val sector: String,
    val exchange: String, // NYSE, NASDAQ
    val timestamp: String,
    val isRealTime: Boolean = true
)

data class CandlePoint(
    val timestamp: Long,
    val dateLabel: String,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Long
)

enum class Timeframe(val label: String) {
    ONE_DAY("1D"),
    FIVE_DAYS("5D"),
    ONE_MONTH("1M"),
    THREE_MONTHS("3M"),
    ONE_YEAR("1Y"),
    FIVE_YEARS("5Y")
}

enum class SuggestionCategory(val displayName: String, val icon: String) {
    SHORT_TERM("Short-Term Momentum", "🚀"),
    SWING("Swing Trading", "⚡"),
    LONG_TERM_GROWTH("Long-Term Growth", "🌱"),
    UNDERVALUED("Undervalued Value", "💎"),
    DIVIDENDS("High Dividend", "💰"),
    HIGH_QUALITY("High Quality Moat", "🏰"),
    HIGH_RISK("High Volatility / Speculative", "🔥"),
    AVOID("Stocks to Avoid / Review", "⚠️")
}

data class StockSuggestion(
    val ticker: String,
    val name: String,
    val price: Double,
    val changePercent: Double,
    val category: SuggestionCategory,
    val rankScore: Int, // 1-100
    val valuationStatus: String, // "Severely Undervalued", "Fair Value", "Premium"
    val riskLevel: String, // "Low", "Medium", "High", "Extreme"
    val rankingReason: String,
    val keyCatalysts: List<String>,
    val downsideRisks: List<String>,
    val fundamentalStrength: String
)

enum class ForecastHorizon(val label: String, val tradingDays: Int) {
    NEXT_DAY("1 Day", 1),
    FIVE_DAYS("5 Days", 5),
    TWENTY_DAYS("20 Days (1M)", 20),
    THREE_MONTHS("3 Months", 63),
    SIX_TO_TWELVE_MONTHS("6-12 Months", 252)
}

enum class ScenarioCase {
    BEARISH,
    BASE,
    BULLISH
}

data class ForecastScenario(
    val scenario: ScenarioCase,
    val targetPrice: Double,
    val percentageReturn: Double,
    val probabilityPercent: Int,
    val keyDrivers: String,
    val invalidationLevel: Double
)

data class HorizonForecast(
    val horizon: ForecastHorizon,
    val referencePrice: Double,
    val targetDate: String,
    val bearish: ForecastScenario,
    val baseCase: ForecastScenario,
    val bullish: ForecastScenario,
    val confidenceScore: Int, // e.g. 82%
    val uncertaintyRange: String, // e.g. "+/- 3.8%"
    val primaryTechnicalDrivers: String,
    val primaryFundamentalDrivers: String,
    val invalidationCriteria: String
)

data class EntryExitPlan(
    val ticker: String,
    val currentPrice: Double,
    val entryZoneMin: Double,
    val entryZoneMax: Double,
    val entryRationale: String,
    val stopLossLevel: Double,
    val stopLossPercentage: Double,
    val takeProfit1: Double,
    val takeProfit2: Double,
    val riskRewardRatio: String, // "1 : 3.4"
    val suggestedPositionPercent: Double, // e.g. 4.5% of portfolio
    val riskWarnings: List<String>
)

data class FinancialHealth(
    val revenueYoY: Double,
    val netMarginPercent: Double,
    val freeCashFlowBillions: Double,
    val debtToEquity: Double,
    val returnOnEquity: Double,
    val nextEarningsDate: String,
    val lastEarningsSurprisePercent: Double,
    val secFiling10KSummary: String
)

data class TechnicalIndicators(
    val rsi14: Double,
    val macdLine: Double,
    val macdSignal: Double,
    val macdHist: Double,
    val sma50: Double,
    val sma200: Double,
    val bollingerUpper: Double,
    val bollingerLower: Double,
    val nearestSupport: Double,
    val nearestResistance: Double,
    val technicalVerdict: String // "Strong Buy", "Buy", "Neutral", "Sell"
)

enum class NewsImpact {
    POSITIVE,
    NEGATIVE,
    MIXED,
    NEUTRAL
}

enum class NewsHorizon {
    SHORT_TERM,
    LONG_TERM
}

data class NewsArticle(
    val id: String,
    val headline: String,
    val publisher: String,
    val publishedTimeAgo: String,
    val sourceUrl: String,
    val affectedTickers: List<String>,
    val category: String, // "Breaking", "Earnings", "SEC 10-Q", "M&A", "Fed/Macro"
    val summary: String,
    val whatHappened: String,
    val whyItMatters: String,
    val impact: NewsImpact,
    val horizon: NewsHorizon,
    val isPricedIn: String,
    val whatToMonitorNext: String,
    val isConfirmedFact: Boolean
)

data class EconomicEvent(
    val id: String,
    val title: String,
    val timeET: String,
    val importance: String, // "HIGH", "MED", "LOW"
    val consensus: String,
    val previous: String,
    val actual: String?,
    val impactNote: String
)

data class SectorPerformance(
    val sectorName: String,
    val etfSymbol: String,
    val changePercent: Double,
    val marketBreadthAdvancing: Int, // e.g. 74%
    val topContributer: String
)

data class ModelBacktest(
    val horizonLabel: String,
    val directionalAccuracy: Double, // e.g. 73.4%
    val meanAbsoluteErrorPercent: Double, // e.g. 2.1%
    val strategyCumulativeReturn: Double, // e.g. +31.8%
    val sp500BenchmarkReturn: Double, // e.g. +19.2%
    val maxDrawdownPercent: Double, // e.g. -11.4%
    val sharpeRatio: Double, // e.g. 1.84
    val sampleSizeTrades: Int
)

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val citedTickers: List<String> = emptyList(),
    val isThinking: Boolean = false
)

enum class MessageSender {
    USER,
    AI_ASSISTANT
}
