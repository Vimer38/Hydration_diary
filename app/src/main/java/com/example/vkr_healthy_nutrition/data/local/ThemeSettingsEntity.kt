package com.example.vkr_healthy_nutrition.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "theme_settings")
data class ThemeSettingsEntity(
    @PrimaryKey
    val firebaseUid: String, // Уникальный ID пользователя Firebase
    val selectedThemeId: Int, // ID выбранной RadioButton из theme_radio_group (R.id.radio_light, R.id.radio_dark, R.id.radio_system)
    val selectedColorSchemeId: Int, // ID выбранной RadioButton из color_scheme_radio_group (R.id.radio_default, R.id.radio_blue, R.id.radio_green, R.id.radio_purple)
    val lastUpdated: Long = System.currentTimeMillis() // Время последнего обновления (опционально)
)

