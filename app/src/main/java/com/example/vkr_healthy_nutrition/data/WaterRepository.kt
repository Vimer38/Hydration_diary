package com.example.vkr_healthy_nutrition.data

import kotlinx.coroutines.flow.Flow

class WaterIntakeRepository(private val dao: WaterIntakeDao) {
    val todayTotal: Flow<Int?> = dao.getTodayTotal()
    val todayRecords: Flow<List<WaterRecord>> = dao.getTodayRecords()

    suspend fun addWater(amount: Int) {
        dao.insert(WaterRecord(amount = amount))
    }
}