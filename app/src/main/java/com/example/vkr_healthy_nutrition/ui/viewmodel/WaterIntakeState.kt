package com.example.vkr_healthy_nutrition.ui.viewmodel

import com.example.vkr_healthy_nutrition.data.local.entities.WaterIntakeEntity

sealed class WaterIntakeState {
    object Idle : WaterIntakeState()
    object Loading : WaterIntakeState()
    data class Success(
        val total: Int,
        val records: List<WaterIntakeEntity>
    ) : WaterIntakeState()
    data class Error(val message: String) : WaterIntakeState()
}