package com.example.data.local

import androidx.room.*
import com.example.data.model.PortfolioHolding
import com.example.data.model.PriceAlert
import com.example.data.model.WatchlistItem
import kotlinx.coroutines.flow.Flow

@Dao
interface PortfolioDao {
    @Query("SELECT * FROM portfolio_holdings ORDER BY ticker ASC")
    fun getAllHoldings(): Flow<List<PortfolioHolding>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHolding(holding: PortfolioHolding): Long

    @Update
    suspend fun updateHolding(holding: PortfolioHolding)

    @Delete
    suspend fun deleteHolding(holding: PortfolioHolding)

    @Query("DELETE FROM portfolio_holdings WHERE id = :id")
    suspend fun deleteHoldingById(id: Long)
}

@Dao
interface WatchlistDao {
    @Query("SELECT * FROM watchlists ORDER BY addedDateMillis DESC")
    fun getAllWatchlistItems(): Flow<List<WatchlistItem>>

    @Query("SELECT * FROM watchlists WHERE listName = :listName ORDER BY addedDateMillis DESC")
    fun getItemsByListName(listName: String): Flow<List<WatchlistItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatchlistItem(item: WatchlistItem): Long

    @Query("DELETE FROM watchlists WHERE ticker = :ticker AND listName = :listName")
    suspend fun deleteWatchlistItem(ticker: String, listName: String)

    @Query("SELECT DISTINCT listName FROM watchlists")
    fun getAllListNames(): Flow<List<String>>
}

@Dao
interface AlertDao {
    @Query("SELECT * FROM price_alerts ORDER BY createdAtMillis DESC")
    fun getAllAlerts(): Flow<List<PriceAlert>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: PriceAlert): Long

    @Update
    suspend fun updateAlert(alert: PriceAlert)

    @Delete
    suspend fun deleteAlert(alert: PriceAlert)

    @Query("DELETE FROM price_alerts WHERE id = :id")
    suspend fun deleteAlertById(id: Long)
}
