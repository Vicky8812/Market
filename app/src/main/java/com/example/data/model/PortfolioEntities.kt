package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "portfolio_holdings")
data class PortfolioHolding(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ticker: String,
    val companyName: String,
    val shares: Double,
    val avgBuyPrice: Double,
    val purchaseDateMillis: Long = System.currentTimeMillis(),
    val notes: String = ""
)

@Entity(tableName = "watchlists")
data class WatchlistItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val listName: String = "Main Watchlist",
    val ticker: String,
    val addedDateMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "price_alerts")
data class PriceAlert(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ticker: String,
    val targetPrice: Double,
    val condition: String, // "ABOVE" or "BELOW"
    val isTriggered: Boolean = false,
    val isEnabled: Boolean = true,
    val note: String = "",
    val createdAtMillis: Long = System.currentTimeMillis()
)
