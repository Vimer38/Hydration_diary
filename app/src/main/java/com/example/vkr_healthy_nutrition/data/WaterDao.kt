package com.example.vkr_healthy_nutrition.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WaterIntakeDao {
    @Insert
    suspend fun insert(record: WaterRecord)

    @Query("SELECT SUM(amount) FROM water_records WHERE date(date/1000, 'unixepoch') = date('now')")
    fun getTodayTotal(): Flow<Int?>

    @Query("SELECT * FROM water_records WHERE date(date/1000, 'unixepoch') = date('now') ORDER BY date DESC")
    fun getTodayRecords(): Flow<List<WaterRecord>>
}