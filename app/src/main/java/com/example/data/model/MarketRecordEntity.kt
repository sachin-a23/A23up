package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "market_records",
    indices = [Index(value = ["marketName", "date"], unique = true)]
)
data class MarketRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val marketName: String, // e.g. "SHRIDEVI", "KALYAN", "MILAN", "TIME BAZAR"
    val date: String, // e.g. "27-07-2026"
    val openPanel: String, // e.g. "160"
    val jodi: String, // e.g. "05" or "71"
    val closePanel: String, // e.g. "2026" or "100"
    val isHoliday: Boolean = false,
    val dayOfWeek: String = "", // e.g. "Somvaar (Mon)"
    val timestamp: Long = System.currentTimeMillis()
)
