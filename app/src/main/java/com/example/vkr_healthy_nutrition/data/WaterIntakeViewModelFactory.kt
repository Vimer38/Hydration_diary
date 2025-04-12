package com.example.vkr_healthy_nutrition.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

// WaterIntakeViewModelFactory.kt
class WaterIntakeViewModelFactory(
    private val repository: WaterIntakeRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WaterIntakeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WaterIntakeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}