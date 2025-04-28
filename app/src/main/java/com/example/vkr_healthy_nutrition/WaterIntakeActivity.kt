package com.example.vkr_healthy_nutrition

// import android.content.Intent // Больше не нужен для перехода в MainActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View // Для ProgressBar
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar // Для индикации сохранения
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast // Для сообщений
import androidx.activity.viewModels // Для ViewModel
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer // Для LiveData
// Импортируем ViewModel, Factory и Repository
import com.example.vkr_healthy_nutrition.data.WaterIntakeViewModelFactory
import com.example.vkr_healthy_nutrition.data.WaterViewModel
import com.example.vkr_healthy_nutrition.data.WaterUiState
import com.example.vkr_healthy_nutrition.data.network.WaterRecordResponse
import com.example.vkr_healthy_nutrition.data.repository.WaterRepository
import com.example.vkr_healthy_nutrition.data.network.WaterGoalResponse // Для типа результата

class WaterIntakeActivity : AppCompatActivity() {

    private lateinit var weightInput: EditText
    private lateinit var weatherSpinner: Spinner
    private lateinit var exerciseLevelSpinner: Spinner
    private lateinit var resultTextView: TextView
    private lateinit var calculateButton: Button
    private lateinit var saveProgressBar: ProgressBar // Индикатор сохранения

    // Получаем ViewModel
    private val viewModel: WaterViewModel by viewModels {
        WaterIntakeViewModelFactory(WaterRepository(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_water_intake)

        weightInput = findViewById(R.id.weightInput)
        weatherSpinner = findViewById(R.id.weatherSpinner)
        exerciseLevelSpinner = findViewById(R.id.exerciseLevelSpinner)
        resultTextView = findViewById(R.id.resultTextView)
        calculateButton = findViewById(R.id.calculateButton)
        // Инициализация ProgressBar (убедитесь, что ID = save_goal_progress_bar в layout)
        saveProgressBar = findViewById(R.id.save_goal_progress_bar)

        // Настройка адаптеров для Spinner (без изменений)
        val weatherConditions = arrayOf("Холодно", "Прохладно","Тепло", "Жарко")
        val weatherAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, weatherConditions)
        weatherAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        weatherSpinner.adapter = weatherAdapter

        val exerciseLevels = arrayOf("Пассивный", "Обычный","Активный")
        val exerciseAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, exerciseLevels)
        exerciseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        exerciseLevelSpinner.adapter = exerciseAdapter

        calculateButton.setOnClickListener { calculateAndSaveWaterIntake() } // Переименовали функцию

        // Настройка Toolbar (без изменений)
        val appBar: Toolbar = findViewById(R.id.toolbar_set)
        setSupportActionBar(appBar)
        supportActionBar?.title = "Цели по потреблению"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Подписка на результат сохранения цели
        observeViewModel()
    }

    // Подписка на LiveData для результата сохранения
    private fun observeViewModel() {
        viewModel.updateGoalResult.observe(this, Observer { result: Result<WaterGoalResponse>? ->
            saveProgressBar.visibility = View.GONE
            calculateButton.isEnabled = true

            if (result == null) return@Observer

            when {
                result.isSuccess -> {
                    val waterGoal = result.getOrNull()?.waterGoal ?: return@Observer
                    Toast.makeText(this, "Цель ($waterGoal мл) успешно сохранена!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                result.isFailure -> {
                    Toast.makeText(this, "Ошибка сохранения цели: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                }
            }
        })

        viewModel.addWaterResult.observe(this, Observer { result: Result<WaterRecordResponse>? ->
            if (result == null) return@Observer

            when {
                result.isSuccess -> {
                    Toast.makeText(this, "Вода добавлена!", Toast.LENGTH_SHORT).show()
                }
                result.isFailure -> {
                    Toast.makeText(this, "Ошибка добавления: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                }
            }
        })
    }

    // Переименованная функция для расчета и сохранения
    private fun calculateAndSaveWaterIntake() {
        val weight = weightInput.text.toString().toFloatOrNull()
        val weatherCondition = weatherSpinner.selectedItem.toString()
        val exerciseLevel = exerciseLevelSpinner.selectedItem.toString()

        if (weight != null && weight > 0) { // Добавлена проверка weight > 0
            var baseWaterIntake = weight * 30

            val weatherAdjustment = when (weatherCondition) {
                "Холодно" -> -130
                "Прохладно" -> 0
                "Тепло" -> 267
                "Жарко" -> 530
                else -> 0
            }

            val exerciseAdjustment = when (exerciseLevel) {
                "Пассивный" -> 0
                "Обычный" -> 260
                "Активный" -> 540
                else -> 0
            }

            val totalWaterIntake = baseWaterIntake + weatherAdjustment + exerciseAdjustment
            val finalGoal = totalWaterIntake.toInt()

            // Отображаем рассчитанный результат
            resultTextView.text = "Рекомендуемое количество воды: $finalGoal мл"

            // Показываем ProgressBar и блокируем кнопку
            saveProgressBar.visibility = View.VISIBLE
            calculateButton.isEnabled = false

            // Вызываем ViewModel для сохранения цели на сервере
            viewModel.updateWaterGoal(finalGoal)

            // Убираем переход на MainActivity
            // val intent = Intent(this, MainActivity::class.java)
            // intent.putExtra("waterGoal", finalGoal)
            // startActivity(intent)
        } else {
            resultTextView.text = "Пожалуйста, введите корректный вес."
        }
    }

    // Обработка кнопки "Назад" в Toolbar (без изменений)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}