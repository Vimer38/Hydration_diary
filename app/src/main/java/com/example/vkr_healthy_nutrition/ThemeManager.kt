package com.example.vkr_healthy_nutrition

import android.animation.ArgbEvaluator
import android.content.Context
import android.content.SharedPreferences
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.example.vkr_healthy_nutrition.data.repository.ProfileRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object ThemeManager {
    private const val PREF_NAME = "theme_pref"
    private const val KEY_THEME = "theme_mode"
    private const val KEY_COLOR_SCHEME = "color_scheme"
    private var repository: ProfileRepository? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    // Константы для режимов темы
    const val THEME_LIGHT = 0
    const val THEME_DARK = 1
    const val THEME_SYSTEM = 2

    // Константы для цветовых схем
    const val COLOR_SCHEME_DEFAULT = 0
    const val COLOR_SCHEME_BLUE = 1
    const val COLOR_SCHEME_GREEN = 2
    const val COLOR_SCHEME_PURPLE = 3

    // Длительность анимации в миллисекундах
    private const val ANIMATION_DURATION = 300L

    private fun getRepository(context: Context): ProfileRepository {
        if (repository == null) {
            repository = ProfileRepository(context.applicationContext)
        }
        return repository!!
    }

    fun setTheme(context: Context, themeMode: Int) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_THEME, themeMode).apply()
        
        when (themeMode) {
            THEME_LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            THEME_DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            THEME_SYSTEM -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    fun setColorScheme(context: Context, colorScheme: Int) {
        // Сохраняем локально
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_COLOR_SCHEME, colorScheme).apply()

        // Сохраняем на сервере
        scope.launch {
            try {
                getRepository(context).updateUserColorScheme(colorScheme)
            } catch (e: Exception) {
                // Ошибка сохранения на сервере - можно добавить обработку
            }
        }
    }

    fun getThemeMode(context: Context): Int {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_THEME, THEME_SYSTEM)
    }

    fun getColorScheme(context: Context): Int {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_COLOR_SCHEME, COLOR_SCHEME_DEFAULT)
    }

    fun loadColorSchemeFromServer(context: Context) {
        scope.launch {
            try {
                val result = getRepository(context).getUserColorScheme()
                result.onSuccess { colorScheme ->
                    val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                    prefs.edit().putInt(KEY_COLOR_SCHEME, colorScheme).apply()
                }
            } catch (e: Exception) {
                // Ошибка загрузки с сервера - используем локальные настройки
            }
        }
    }

    fun isDarkTheme(context: Context): Boolean {
        return when (getThemeMode(context)) {
            THEME_DARK -> true
            THEME_LIGHT -> false
            THEME_SYSTEM -> {
                val nightMode = context.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
                nightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES
            }
            else -> {
                val nightMode = context.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
                nightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES
            }
        }
    }

    fun getPrimaryColor(context: Context): Int {
        return when (getColorScheme(context)) {
            COLOR_SCHEME_BLUE -> ContextCompat.getColor(context, R.color.primary_blue)
            COLOR_SCHEME_GREEN -> ContextCompat.getColor(context, R.color.primary_green)
            COLOR_SCHEME_PURPLE -> ContextCompat.getColor(context, R.color.primary_purple)
            else -> ContextCompat.getColor(context, R.color.primary_default)
        }
    }

    fun getSecondaryColor(context: Context): Int {
        return when (getColorScheme(context)) {
            COLOR_SCHEME_BLUE -> ContextCompat.getColor(context, R.color.secondary_blue)
            COLOR_SCHEME_GREEN -> ContextCompat.getColor(context, R.color.secondary_green)
            COLOR_SCHEME_PURPLE -> ContextCompat.getColor(context, R.color.secondary_purple)
            else -> ContextCompat.getColor(context, R.color.secondary_default)
        }
    }
} 