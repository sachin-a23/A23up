package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.MarketRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MarketRecordDao {
    @Query("SELECT * FROM market_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<MarketRecordEntity>>

    @Query("SELECT * FROM market_records WHERE marketName = :marketName ORDER BY timestamp ASC")
    fun getRecordsByMarketAsc(marketName: String): Flow<List<MarketRecordEntity>>

    @Query("SELECT * FROM market_records WHERE marketName = :marketName ORDER BY timestamp DESC")
    fun getRecordsByMarketDesc(marketName: String): Flow<List<MarketRecordEntity>>

    @Query("SELECT DISTINCT marketName FROM market_records")
    fun getAllMarkets(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: MarketRecordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<MarketRecordEntity>)

    @Update
    suspend fun updateRecord(record: MarketRecordEntity)

    @Query("DELETE FROM market_records WHERE id = :id")
    suspend fun deleteRecordById(id: Int)

    @Query("DELETE FROM market_records WHERE marketName = :marketName")
    suspend fun deleteMarketRecords(marketName: String)

    @Query("DELETE FROM market_records")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM market_records")
    suspend fun getRecordCount(): Int

    @Query("SELECT COUNT(*) FROM market_records WHERE openPanel = '100' AND closePanel = '100'")
    suspend fun getBadRecordCount(): Int

    @Query("DELETE FROM market_records WHERE openPanel = '100' AND closePanel = '100'")
    suspend fun deleteBadRecords()
}
