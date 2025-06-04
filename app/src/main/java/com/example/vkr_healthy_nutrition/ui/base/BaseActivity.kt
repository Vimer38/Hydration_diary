package com.example.vkr_healthy_nutrition.ui.base

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.vkr_healthy_nutrition.R // Импортируем R для доступа к ресурсам
import com.example.vkr_healthy_nutrition.data.repository.ThemeSettingsRepository
import com.example.vkr_healthy_nutrition.data.local.AppDatabase
import com.example.vkr_healthy_nutrition.auth.FirebaseAuthManager


abstract class BaseActivity : AppCompatActivity() {
    // Получаем репозиторий напрямую в Activity, чтобы иметь синхронный доступ к SharedPreferences
    protected lateinit var themeSettingsRepository: ThemeSettingsRepository
    protected lateinit var authManager: FirebaseAuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Инициализируем authManager
        authManager = FirebaseAuthManager()

        // Инициализируем репозиторий
        val database = AppDatabase.getDatabase(application)
        themeSettingsRepository = ThemeSettingsRepository(database.themeSettingsDao(), application, authManager)

        // Получаем последний выбранный ID цветовой схемы из SharedPreferences
        val latestColorSchemeId = themeSettingsRepository.getLatestColorSchemeId()
        // Применяем соответствующий стиль темы
        applyColorScheme(latestColorSchemeId)

        // Получаем последний выбранный ID темы (светлая/темная/системная) из SharedPreferences
        val latestThemeId = themeSettingsRepository.getLatestThemeId()
        // Применяем режим дня/ночи
        applyNightMode(latestThemeId)
    }

    // Метод для применения цветовой схемы (теперь вызывается до super.onCreate)
    private fun applyColorScheme(colorSchemeId: Int) {
        val newThemeStyle = when (colorSchemeId) {
            R.id.radio_default -> R.style.Theme_HealthyNutrition_Default
            R.id.radio_blue -> R.style.Theme_HealthyNutrition_Blue
            R.id.radio_green -> R.style.Theme_HealthyNutrition_Green
            R.id.radio_purple -> R.style.Theme_HealthyNutrition_Purple
            // Если нет сохраненной цветовой схемы, используем схему по умолчанию
            else -> R.style.Theme_HealthyNutrition_Default
        }
        // Применяем стиль цветовой схемы
        setTheme(newThemeStyle)
    }

    // Метод для применения режима дня/ночи (вызывается до super.onCreate)
    private fun applyNightMode(themeId: Int) {
        when (themeId) {
            R.id.radio_light -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            R.id.radio_dark -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            R.id.radio_system -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            // Если нет сохраненных настроек, используем режим по умолчанию
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    // ViewModel по-прежнему доступен в SettingsActivity через delegation
    // animateToolbarColorChange() может быть использован, если вы решите анимировать смену цвета Toolbar вручную,
    // но для простого применения темы на старте активности он не нужен.

    // Анимация цвета для Toolbar (опционально, если хотите анимировать смену цвета)
    protected fun animateToolbarColorChange(toolbar: androidx.appcompat.widget.Toolbar, startColor: Int, endColor: Int) {
        val colorAnimator = ValueAnimator.ofObject(
            ArgbEvaluator(),
            startColor, // Начальный цвет (текущий цвет Toolbar)
            endColor // Конечный цвет (новый цвет схемы)
        )
        colorAnimator.duration = 300 // Длительность анимации в миллисекундах
        colorAnimator.interpolator = AccelerateDecelerateInterpolator() // Интерполятор для плавности

        colorAnimator.addUpdateListener { animator ->
            val animatedColor = animator.animatedValue as Int
            toolbar.setBackgroundColor(animatedColor) // Устанавливаем анимированный цвет для Toolbar
            window.statusBarColor = animatedColor // Опционально: анимируем цвет статус-бара
        }

        colorAnimator.start() // Запускаем анимацию
    }
}
