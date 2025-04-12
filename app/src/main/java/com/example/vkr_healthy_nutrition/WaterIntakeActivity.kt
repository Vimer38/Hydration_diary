package com.example.vkr_healthy_nutrition

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class WaterIntakeActivity : AppCompatActivity() {

    private lateinit var weightInput: EditText
    private lateinit var weatherSpinner: Spinner
    private lateinit var exerciseLevelSpinner: Spinner
    private lateinit var resultTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_water_intake)

        weightInput = findViewById(R.id.weightInput)
        weatherSpinner = findViewById(R.id.weatherSpinner)
        exerciseLevelSpinner = findViewById(R.id.exerciseLevelSpinner)
        resultTextView = findViewById(R.id.resultTextView)

        // Настройка адаптера для Spinner с погодой
        val weatherConditions = arrayOf("Холодно", "Прохладно","Тепло", "Жарко")
        val weatherAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, weatherConditions)
        weatherAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        weatherSpinner.adapter = weatherAdapter

        // Настройка адаптера для Spinner с уровнями спортивной нагрузки
        val exerciseLevels = arrayOf("Пассивный", "Обычный","Активный",)
        val exerciseAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, exerciseLevels)
        exerciseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        exerciseLevelSpinner.adapter = exerciseAdapter

        val calculateButton: Button = findViewById(R.id.calculateButton)
        calculateButton.setOnClickListener { calculateWaterIntake() }

        val appBar: Toolbar = findViewById(R.id.toolbar_set)
        setSupportActionBar(appBar)

        supportActionBar?.title = "Цели по потреблению"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish() // Закрываем текущую активность
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun calculateWaterIntake() {
        val weight = weightInput.text.toString().toFloatOrNull()
        val weatherCondition = weatherSpinner.selectedItem.toString()
        val exerciseLevel = exerciseLevelSpinner.selectedItem.toString()

        if (weight != null && weight?.toInt() != 0) {
            var baseWaterIntake = weight * 30 // Базовое потребление воды в мл

            // Учитываем погодные условия
            val weatherAdjustment = when (weatherCondition) {
                "Холодно" -> -130 // Уменьшаем потребление воды в холодную погоду
                "Прохладно" -> 0
                "Тепло" -> 267
                "Жарко" -> 530 // Увеличиваем потребление воды в жаркую погоду
                else -> 0
            }

            // Учитываем уровень спортивной нагрузки
            val exerciseAdjustment = when (exerciseLevel) {
                "Пассивный" -> 0 // Увеличиваем потребление воды для умеренной нагрузки
                "Обычный" -> 260 // Увеличиваем потребление воды для тяжелой нагрузки
                "Активный" -> 540
                else -> 0
            }

            // Рассчитываем общее потребление воды
            val totalWaterIntake = baseWaterIntake + weatherAdjustment + exerciseAdjustment
            resultTextView.text = "Рекомендуемое количество воды: ${totalWaterIntake.toInt()} мл"

            // Передаем рассчитанное значение в MainActivity
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("waterGoal", totalWaterIntake.toInt())
            startActivity(intent)
        } else {
            resultTextView.text = "Пожалуйста, введите корректный вес."
        }
    }
}