package com.example.vkr_healthy_nutrition.ui.activities

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.example.vkr_healthy_nutrition.ui.base.BaseActivity
import com.example.vkr_healthy_nutrition.R
import com.example.vkr_healthy_nutrition.ui.viewmodel.SettingsViewModel
import com.example.vkr_healthy_nutrition.ui.viewmodel.SettingsViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SettingsActivity : BaseActivity() {
    private lateinit var rootView: View
    private lateinit var toolbar: Toolbar
    private lateinit var themeRadioGroup: RadioGroup
    private lateinit var colorSchemeRadioGroup: RadioGroup
    private var isInitialLoad = true // Флаг для отслеживания начальной загрузки

    private val viewModel: SettingsViewModel by viewModels {
        SettingsViewModelFactory(this.themeSettingsRepository, this.authManager)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        initViews()
        setupToolbar()
        observeViewModel() // Сначала наблюдаем за изменениями
        setupListeners() // Затем устанавливаем слушатели
        isInitialLoad = false
    }

    private fun initViews() {
        rootView = findViewById(R.id.settings)
        toolbar = findViewById(R.id.toolbar_set)
        themeRadioGroup = findViewById(R.id.theme_radio_group)
        colorSchemeRadioGroup = findViewById(R.id.color_scheme_radio_group)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Настройки"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setupListeners() {
        // Слушатель для группы выбора темы
        themeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radio_light -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
                R.id.radio_dark -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                }
                R.id.radio_system -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                }
            }
            // Сохраняем настройки темы после применения
            saveThemeSettings()
        }

        // Устанавливаем слушатели для каждой радио-кнопки цветовой схемы
        findViewById<RadioButton>(R.id.radio_default).setOnClickListener {
            colorSchemeRadioGroup.check(R.id.radio_default)
            saveThemeSettings()
            showRestartMessage()
        }
        findViewById<RadioButton>(R.id.radio_blue).setOnClickListener {
            colorSchemeRadioGroup.check(R.id.radio_blue)
            saveThemeSettings()
            showRestartMessage()
        }
        findViewById<RadioButton>(R.id.radio_green).setOnClickListener {
            colorSchemeRadioGroup.check(R.id.radio_green)
            saveThemeSettings()
            showRestartMessage()
        }
        findViewById<RadioButton>(R.id.radio_purple).setOnClickListener {
            colorSchemeRadioGroup.check(R.id.radio_purple)
            saveThemeSettings()
            showRestartMessage()
        }
    }

    private fun showRestartMessage() {
        android.widget.Toast.makeText(
            this,
            "Для применения цветовой схемы необходимо перезапустить приложение",
            android.widget.Toast.LENGTH_LONG
        ).show()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.themeSettings.collectLatest { settings ->
                settings?.let {
                    // Устанавливаем сохраненные значения только при первом запуске или если они изменились извне
                    // Избегаем повторной установки, которая может вызвать мигание
                    if (themeRadioGroup.checkedRadioButtonId != it.selectedThemeId) {
                        themeRadioGroup.check(it.selectedThemeId)
                    }
                    if (colorSchemeRadioGroup.checkedRadioButtonId != it.selectedColorSchemeId) {
                        colorSchemeRadioGroup.check(it.selectedColorSchemeId)
                    }
                }
            }
        }
    }

    private fun saveThemeSettings() {
        val selectedThemeId = themeRadioGroup.checkedRadioButtonId
        val selectedColorSchemeId = colorSchemeRadioGroup.checkedRadioButtonId

        if (selectedThemeId != -1 && selectedColorSchemeId != -1) {
            viewModel.saveThemeSettings(selectedThemeId, selectedColorSchemeId)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}