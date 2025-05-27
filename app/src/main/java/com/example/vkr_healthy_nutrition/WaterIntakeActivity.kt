package com.example.vkr_healthy_nutrition

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

import androidx.lifecycle.ViewModelProvider
import com.example.vkr_healthy_nutrition.ui.viewmodel.WaterIntakeViewModel
import com.example.vkr_healthy_nutrition.ui.viewmodel.WaterIntakeViewModelFactory
import java.util.Timer

class WaterIntakeActivity : BaseActivity() {

    private lateinit var weightInput: EditText
    private lateinit var weatherSpinner: Spinner
    private lateinit var exerciseLevelSpinner: Spinner
    private lateinit var resultTextView: TextView
    private lateinit var calculateButton: Button

    private lateinit var waterIntakeViewModel: WaterIntakeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_water_intake)

        val application = application as HealthyNutritionApp
        val repository = application.waterIntakeRepository
        val viewModelFactory = application.waterIntakeViewModelFactory
        waterIntakeViewModel = ViewModelProvider(this, viewModelFactory)[WaterIntakeViewModel::class.java]

        weightInput = findViewById(R.id.weightInput)
        weatherSpinner = findViewById(R.id.weatherSpinner)
        exerciseLevelSpinner = findViewById(R.id.exerciseLevelSpinner)
        resultTextView = findViewById(R.id.resultTextView)
        calculateButton = findViewById(R.id.calculateButton)

        setupSpinners()
        setupToolbar()
        setupClickListeners()
    }

    private fun setupSpinners() {
        val weatherConditions = arrayOf("Холодно", "Прохладно", "Тепло", "Жарко")
        val weatherAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, weatherConditions)
        weatherAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        weatherSpinner.adapter = weatherAdapter

        val exerciseLevels = arrayOf("Пассивный", "Обычный", "Активный")
        val exerciseAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, exerciseLevels)
        exerciseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        exerciseLevelSpinner.adapter = exerciseAdapter
    }

    private fun setupToolbar() {
        val appBar: Toolbar = findViewById(R.id.toolbar_set)
        setSupportActionBar(appBar)
        supportActionBar?.title = "Цели по потреблению"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setupClickListeners() {
        calculateButton.setOnClickListener { calculateAndSaveWaterIntake() }
    }

    private fun calculateAndSaveWaterIntake() {
        val weight = weightInput.text.toString().toFloatOrNull()
        val weatherCondition = weatherSpinner.selectedItem.toString()
        val exerciseLevel = exerciseLevelSpinner.selectedItem.toString()

        if (weight != null && weight > 0) {
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

            resultTextView.text = "Рекомендуемое количество воды: $finalGoal мл"
            waterIntakeViewModel.saveWaterGoal(finalGoal)

            Toast.makeText(this, "Цель сохранена локально!", Toast.LENGTH_SHORT).show()
        } else {
            resultTextView.text = "Пожалуйста, введите корректный вес."
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