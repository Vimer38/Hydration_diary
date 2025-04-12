package com.example.vkr_healthy_nutrition.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class WaterIntakeViewModel(private val repository: WaterIntakeRepository) : ViewModel() {
    val todayTotal = repository.todayTotal
    val todayRecords = repository.todayRecords

    fun addWater(amount: Int) {
        viewModelScope.launch {
            repository.addWater(amount)
        }
    }
}