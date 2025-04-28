package com.example.vkr_healthy_nutrition.data.repository

import android.content.Context
// Импорты для работы с сетью и хелперами
import com.example.vkr_healthy_nutrition.data.network.AddWaterRequest
import com.example.vkr_healthy_nutrition.data.network.ApiService
import com.example.vkr_healthy_nutrition.data.network.WaterRecordResponse
import com.example.vkr_healthy_nutrition.data.network.TodayWaterTotalResponse
import com.example.vkr_healthy_nutrition.data.network.UpdateWaterGoalRequest
import com.example.vkr_healthy_nutrition.data.network.WaterGoalResponse
import com.example.vkr_healthy_nutrition.data.network.RetrofitInstance
// SafeApiCall теперь импортируется из своего файла

// Новый репозиторий для работы с API воды
class WaterRepository(private val context: Context) : SafeApiCall() {

    private val api = ApiService.create(context)

    // Добавление записи о воде через API
    suspend fun addWaterRecord(amount: Int): Result<WaterRecordResponse> {
        return safeApiCall {
            api.addWaterRecord(AddWaterRequest(amount))
        }
    }

    // Получение записей о воде за сегодня через API
    suspend fun getTodayWaterRecords(): Result<List<WaterRecordResponse>> {
        return safeApiCall {
            api.getTodayWaterRecords()
        }
    }

    // Получение суммарного потребления за сегодня через API
    suspend fun getTodayWaterTotal(): Result<TodayWaterTotalResponse> {
        return safeApiCall {
            api.getTodayWaterTotal()
        }
    }

    // --- Цель по воде ---
    suspend fun getUserWaterGoal(): Result<WaterGoalResponse> {
        return safeApiCall {
            api.getUserWaterGoal()
        }
    }

    suspend fun updateUserWaterGoal(newGoal: Int): Result<WaterGoalResponse> {
        return safeApiCall {
            api.updateUserWaterGoal(UpdateWaterGoalRequest(newGoal))
        }
    }

    suspend fun getWeekWaterRecords(): Result<List<WaterRecordResponse>> {
        return safeApiCall {
            api.getWeekWaterRecords()
        }
    }

    suspend fun getMonthWaterRecords(): Result<List<WaterRecordResponse>> {
        return safeApiCall {
            api.getMonthWaterRecords()
        }
    }
} 