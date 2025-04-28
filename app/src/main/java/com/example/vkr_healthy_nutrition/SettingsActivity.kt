package com.example.vkr_healthy_nutrition

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SettingsActivity : AppCompatActivity() {
    private lateinit var rootView: View
    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        rootView = findViewById(R.id.settings)
        toolbar = findViewById(R.id.toolbar_set)
        setSupportActionBar(toolbar)

        supportActionBar?.title = "Настройки"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Инициализация выбора темы
        val themeRadioGroup: RadioGroup = findViewById(R.id.theme_radio_group)
        val currentTheme = ThemeManager.getThemeMode(this)
        
        when (currentTheme) {
            ThemeManager.THEME_LIGHT -> themeRadioGroup.check(R.id.radio_light)
            ThemeManager.THEME_DARK -> themeRadioGroup.check(R.id.radio_dark)
            ThemeManager.THEME_SYSTEM -> themeRadioGroup.check(R.id.radio_system)
        }

        themeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val themeMode = when (checkedId) {
                R.id.radio_light -> ThemeManager.THEME_LIGHT
                R.id.radio_dark -> ThemeManager.THEME_DARK
                R.id.radio_system -> ThemeManager.THEME_SYSTEM
                else -> ThemeManager.THEME_SYSTEM
            }
            animateThemeChange(themeMode)
        }

        // Инициализация выбора цветовой схемы
        val colorSchemeRadioGroup: RadioGroup = findViewById(R.id.color_scheme_radio_group)
        val currentColorScheme = ThemeManager.getColorScheme(this)

        when (currentColorScheme) {
            ThemeManager.COLOR_SCHEME_DEFAULT -> colorSchemeRadioGroup.check(R.id.radio_default)
            ThemeManager.COLOR_SCHEME_BLUE -> colorSchemeRadioGroup.check(R.id.radio_blue)
            ThemeManager.COLOR_SCHEME_GREEN -> colorSchemeRadioGroup.check(R.id.radio_green)
            ThemeManager.COLOR_SCHEME_PURPLE -> colorSchemeRadioGroup.check(R.id.radio_purple)
        }

        colorSchemeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val colorScheme = when (checkedId) {
                R.id.radio_default -> ThemeManager.COLOR_SCHEME_DEFAULT
                R.id.radio_blue -> ThemeManager.COLOR_SCHEME_BLUE
                R.id.radio_green -> ThemeManager.COLOR_SCHEME_GREEN
                R.id.radio_purple -> ThemeManager.COLOR_SCHEME_PURPLE
                else -> ThemeManager.COLOR_SCHEME_DEFAULT
            }
            animateColorSchemeChange(colorScheme)
        }
    }

    private fun animateThemeChange(themeMode: Int) {
        val currentColor = ThemeManager.getPrimaryColor(this)
        ThemeManager.setTheme(this, themeMode)
        val newColor = ThemeManager.getPrimaryColor(this)

        val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), currentColor, newColor)
        colorAnimation.duration = 300
        colorAnimation.interpolator = AccelerateDecelerateInterpolator()
        colorAnimation.addUpdateListener { animator ->
            val color = animator.animatedValue as Int
            toolbar.setBackgroundColor(color)
        }
        colorAnimation.start()
    }

    private fun animateColorSchemeChange(colorScheme: Int) {
        val currentColor = ThemeManager.getPrimaryColor(this)
        ThemeManager.setColorScheme(this, colorScheme)
        val newColor = ThemeManager.getPrimaryColor(this)

        val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), currentColor, newColor)
        colorAnimation.duration = 300
        colorAnimation.interpolator = AccelerateDecelerateInterpolator()
        colorAnimation.addUpdateListener { animator ->
            val color = animator.animatedValue as Int
            toolbar.setBackgroundColor(color)
        }
        colorAnimation.start()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Обработка нажатия на кнопку "Назад" в AppBar
        if (item.itemId == android.R.id.home) {
            // Возврат на главную активность
            finish() // Закрываем текущую активность
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}