package com.example.data.repository

import com.example.data.dao.MarketRecordDao
import com.example.data.model.MarketRecordEntity
import com.example.network.GitHubSyncManager
import kotlinx.coroutines.flow.Flow

class MarketRepository(private val dao: MarketRecordDao) {

    val allRecords: Flow<List<MarketRecordEntity>> = dao.getAllRecords()
    val allMarkets: Flow<List<String>> = dao.getAllMarkets()

    fun getRecordsForMarketAsc(marketName: String): Flow<List<MarketRecordEntity>> {
        return dao.getRecordsByMarketAsc(marketName)
    }

    fun getRecordsForMarketDesc(marketName: String): Flow<List<MarketRecordEntity>> {
        return dao.getRecordsByMarketDesc(marketName)
    }

    suspend fun saveRecord(record: MarketRecordEntity) {
        dao.insertRecord(record)
    }

    suspend fun deleteRecord(id: Int) {
        dao.deleteRecordById(id)
    }

    suspend fun syncWithGitHub(customUrl: String? = null): Pair<Boolean, String> {
        return GitHubSyncManager.syncDataFromGitHub(dao, customUrl)
    }

    suspend fun seedDefaultsIfEmpty() {
        GitHubSyncManager.seedDefaultData(dao)
    }

    suspend fun clearAll() {
        dao.deleteAll()
    }
}
