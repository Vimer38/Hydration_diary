package com.example.vkr_healthy_nutrition.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.vkr_healthy_nutrition.data.repository.WaterRepository

// WaterIntakeViewModelFactory.kt
class WaterIntakeViewModelFactory(private val repository: WaterRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WaterViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WaterViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class for WaterIntakeViewModelFactory")
    }
}