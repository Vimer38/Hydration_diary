package com.example.vkr_healthy_nutrition.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.vkr_healthy_nutrition.data.repository.WaterIntakeRepository
import com.example.vkr_healthy_nutrition.data.repository.UserRepository

class WaterIntakeViewModelFactory(
    private val repository: WaterIntakeRepository,
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WaterIntakeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WaterIntakeViewModel(repository, userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}