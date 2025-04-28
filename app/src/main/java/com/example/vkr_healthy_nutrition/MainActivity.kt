package com.example.vkr_healthy_nutrition

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.vkr_healthy_nutrition.data.WaterIntakeViewModelFactory
import com.example.vkr_healthy_nutrition.data.WaterViewModel
import com.example.vkr_healthy_nutrition.data.WaterUiState
import com.example.vkr_healthy_nutrition.data.network.WaterRecordResponse
import com.example.vkr_healthy_nutrition.data.repository.WaterRepository
import com.google.android.material.navigation.NavigationView
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class MainActivity : AppCompatActivity() {

    private lateinit var waterIntakeTextView: TextView
    private lateinit var addWaterButton: Button
    private lateinit var dailyStatsTextView: TextView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var waterProgressBar: ProgressBar
    private lateinit var mainProgressBar: ProgressBar

    private lateinit var viewModel: WaterViewModel

    private val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    private val displayTimeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val authPrefs = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val token = authPrefs.getString("jwt_token", null)

        if (token == null) {
            startActivity(Intent(this, WelcomeActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_main)

        val repository = WaterRepository(this)
        val factory = WaterIntakeViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory).get(WaterViewModel::class.java)

        initViews()

        setupNavigation()

        addWaterButton.setOnClickListener {
            showAddWaterDialog()
        }

        updateTotalUI(0, 2000)

        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadTodayWaterData()
    }

    private fun initViews() {
        waterProgressBar = findViewById(R.id.water_progress_bar)
        waterIntakeTextView = findViewById(R.id.water_intake_text_view)
        addWaterButton = findViewById(R.id.add_water_button)
        dailyStatsTextView = findViewById(R.id.daily_stats_text_view)
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        mainProgressBar = findViewById(R.id.main_progress_bar)
    }

    private fun setupNavigation() {
        setSupportActionBar(findViewById(R.id.toolbarMain))
        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        navigationView.setNavigationItemSelectedListener { menuItem ->
            handleNavigationItemSelected(menuItem)
            true
        }
    }

    private fun handleNavigationItemSelected(menuItem: MenuItem) {
        when (menuItem.itemId) {
            R.id.nav_stats -> startActivity(Intent(this, StatsActivity::class.java))
            R.id.nav_notification -> startActivity(Intent(this, NotificationActivity::class.java))
            R.id.nav_goals -> startActivity(Intent(this, WaterIntakeActivity::class.java))
            R.id.nav_settings -> startActivity(Intent(this, SettingsActivity::class.java))
            R.id.nav_logout -> showLogoutConfirmationDialog()
        }
        drawerLayout.closeDrawers()
    }

    private fun observeViewModel() {
        viewModel.todayWaterState.observe(this, Observer { state: WaterUiState ->
            if (state !is WaterUiState.Idle) mainProgressBar.visibility = View.GONE

            when (state) {
                is WaterUiState.Loading -> {
                    mainProgressBar.visibility = View.VISIBLE
                    addWaterButton.isEnabled = false
                }
                is WaterUiState.Success -> {
                    addWaterButton.isEnabled = true
                    updateTotalUI(state.total, state.goal)
                    updateDailyStats(state.records)
                }
                is WaterUiState.Error -> {
                    addWaterButton.isEnabled = true
                    Toast.makeText(this, "Ошибка загрузки данных: ${state.message}", Toast.LENGTH_LONG).show()
                    updateTotalUI(0, 2000)
                    updateDailyStats(emptyList())
                }
                is WaterUiState.Idle -> {
                    addWaterButton.isEnabled = true
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

    private fun updateTotalUI(total: Int, goal: Int) {
        waterProgressBar.max = goal
        waterProgressBar.progress = total
        waterIntakeTextView.text = "Общее потребление воды: ${total} мл / ${goal} мл"
    }

    private fun updateDailyStats(records: List<WaterRecordResponse>) {
        val statsBuilder = StringBuilder("Статистика потребления воды:\n")
        var totalToday = 0

        val sortedRecords = records.sortedByDescending { parseIsoDate(it.record_time) }

        for (record in sortedRecords) {
            val date = parseIsoDate(record.record_time)
            val timeString = date?.let { displayTimeFormat.format(it) } ?: "??:??:??"
            statsBuilder.append("$timeString: ${record.amount} мл\n")
            totalToday += record.amount
        }

        statsBuilder.append("\nВсего за сегодня: $totalToday мл")
        dailyStatsTextView.text = statsBuilder.toString()
    }

    private fun parseIsoDate(dateString: String?): java.util.Date? {
        return if (dateString != null) {
            try {
                isoFormat.parse(dateString)
            } catch (e: ParseException) {
                Log.e("MainActivity", "Error parsing date: $dateString", e)
                null
            }
        } else {
            null
        }
    }

    private fun showAddWaterDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Добавить потребление воды (мл)")

        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_NUMBER
        builder.setView(input)

        builder.setPositiveButton("Добавить") { _, _ ->
            val waterAmountString = input.text.toString()
            val waterAmount = waterAmountString.toIntOrNull()

            if (waterAmount != null && waterAmount > 0) {
                viewModel.addWater(waterAmount)
            } else {
                Toast.makeText(this, "Введите корректное количество воды", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Отмена") { _, _ -> }

        builder.show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showLogoutConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Подтверждение выхода")
        builder.setMessage("Вы уверены, что хотите выйти?")
        builder.setPositiveButton("Да") { _, _ ->
            val authPrefs = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
            val editor = authPrefs.edit()
            editor.remove("jwt_token")
            editor.apply()

            val userPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
            userPrefs.edit().clear().apply()

            startActivity(Intent(this, WelcomeActivity::class.java))
            finish()
        }
        builder.setNegativeButton("Нет") { _, _ -> }
        builder.show()
    }
}