package com.example.vkr_healthy_nutrition.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.example.vkr_healthy_nutrition.R
import com.example.vkr_healthy_nutrition.auth.FirebaseAuthManager
import com.example.vkr_healthy_nutrition.data.local.ThemeSettingsDao
import com.example.vkr_healthy_nutrition.data.local.ThemeSettingsEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

class ThemeSettingsRepository(private val themeSettingsDao: ThemeSettingsDao, private val context: Context, private val authManager: FirebaseAuthManager) {
    private val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    // Получить последний выбранный ID цветовой схемы
    fun getLatestColorSchemeId(): Int {
        return sharedPreferences.getInt("selected_color_scheme_id", R.id.radio_default)
    }

    // Получить последний выбранный ID темы
    fun getLatestThemeId(): Int {
        return sharedPreferences.getInt("selected_theme_id", R.id.radio_system)
    }

    // Получить настройки темы для пользователя из Room по Firebase UID
    fun getThemeSettings(firebaseUid: String): Flow<ThemeSettingsEntity?> {
        return themeSettingsDao.getThemeSettings(firebaseUid)
    }

    // Сохранить настройки темы в Room
    suspend fun saveThemeSettings(
        firebaseUid: String,
        selectedThemeId: Int,
        selectedColorSchemeId: Int
    ) = withContext(Dispatchers.IO) {
        val settings = ThemeSettingsEntity(
            firebaseUid = firebaseUid,
            selectedThemeId = selectedThemeId,
            selectedColorSchemeId = selectedColorSchemeId
        )
        themeSettingsDao.saveThemeSettings(settings)

        // Сохраняем также в SharedPreferences для быстрого доступа
        sharedPreferences.edit().apply {
            putInt("selected_theme_id", selectedThemeId)
            putInt("selected_color_scheme_id", selectedColorSchemeId)
            apply()
        }
    }

    // Удалить настройки темы для пользователя из Room по Firebase UID
    suspend fun deleteThemeSettings(firebaseUid: String) = withContext(Dispatchers.IO) {
        themeSettingsDao.deleteThemeSettings(firebaseUid)
    }
}

