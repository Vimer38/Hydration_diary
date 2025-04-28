package com.example.vkr_healthy_nutrition

import android.app.Application
import com.example.vkr_healthy_nutrition.data.network.RetrofitInstance

class HealthyNutritionApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Применяем сохраненную тему
        if (ThemeManager.isDarkTheme(this)) {
            ThemeManager.setTheme(this, ThemeManager.THEME_DARK)
        }

        // Инициализация RetrofitInstance при старте приложения
        RetrofitInstance.initialize(this)

        // Здесь можно добавить другую инициализацию, если нужно
        // Например, настройку EncryptedSharedPreferences
    }
}