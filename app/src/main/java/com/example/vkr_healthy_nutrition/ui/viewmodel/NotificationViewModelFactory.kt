package com.example.vkr_healthy_nutrition.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.vkr_healthy_nutrition.data.repository.NotificationSettingsRepository
import com.example.vkr_healthy_nutrition.data.repository.UserRepository

class NotificationViewModelFactory(
    private val notificationSettingsRepository: NotificationSettingsRepository,
    private val userRepository: UserRepository // Assuming NotificationViewModel might need UserRepository for user ID
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotificationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NotificationViewModel(notificationSettingsRepository, userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}