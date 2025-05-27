package com.example.vkr_healthy_nutrition.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.vkr_healthy_nutrition.R
import com.example.vkr_healthy_nutrition.auth.FirebaseAuthManager
import com.example.vkr_healthy_nutrition.data.local.ThemeSettingsDao
import com.example.vkr_healthy_nutrition.data.local.ThemeSettingsEntity
import com.example.vkr_healthy_nutrition.data.repository.ThemeSettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: ThemeSettingsRepository,
    private val authManager: FirebaseAuthManager
) : ViewModel() {
    private val _themeSettings = MutableStateFlow<ThemeSettingsEntity?>(null)
    val themeSettings: StateFlow<ThemeSettingsEntity?> = _themeSettings.asStateFlow()

    init {
        // Загружаем настройки темы при инициализации для текущего пользователя Firebase
        viewModelScope.launch {
            val firebaseUid = authManager.currentUser?.uid
            val savedSettings = firebaseUid?.let { repository.getThemeSettings(it).first() } // Используем first() для получения первого значения

            if (savedSettings == null) {
                // Если сохраненных настроек нет, устанавливаем значения по умолчанию
                val defaultThemeId = R.id.radio_light
                val defaultColorSchemeId = R.id.radio_default
                val defaultSettings = ThemeSettingsEntity(
                    firebaseUid ?: "default_user", // Используем UID Firebase или 'default_user' для неавторизованных
                    defaultThemeId,
                    defaultColorSchemeId
                )
                _themeSettings.value = defaultSettings
                // Сохраняем настройки по умолчанию, если пользователь авторизован
                firebaseUid?.let { repository.saveThemeSettings(it, defaultThemeId, defaultColorSchemeId) }
            } else {
                // Если сохраненные настройки есть, загружаем их
                _themeSettings.value = savedSettings
            }
        }
    }

    // Сохранить настройки темы для текущего пользователя Firebase
    fun saveThemeSettings(selectedThemeId: Int, selectedColorSchemeId: Int) {
        viewModelScope.launch {
            authManager.currentUser?.uid?.let { firebaseUid ->
                repository.saveThemeSettings(firebaseUid, selectedThemeId, selectedColorSchemeId)
            }
            // Если пользователь не авторизован, настройки не сохраняются
        }
    }
}

// ViewModelFactory для SettingsViewModel
class SettingsViewModelFactory(private val repository: ThemeSettingsRepository, private val authManager: FirebaseAuthManager) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(repository, authManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

