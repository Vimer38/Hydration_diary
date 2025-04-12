package com.example.vkr_healthy_nutrition

import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.InputType
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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.vkr_healthy_nutrition.data.AppDatabase
import com.example.vkr_healthy_nutrition.data.WaterIntakeRepository
import com.example.vkr_healthy_nutrition.data.WaterIntakeViewModel
import com.example.vkr_healthy_nutrition.data.WaterIntakeViewModelFactory
import com.example.vkr_healthy_nutrition.data.WaterRecord
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var waterIntakeTextView: TextView
    private lateinit var addWaterButton: Button
    private lateinit var dailyStatsTextView: TextView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var waterProgressBar: ProgressBar
    private var waterGoal: Int = 2000

    private lateinit var viewModel: WaterIntakeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val db = AppDatabase.getDatabase(applicationContext)
        val waterIntakeDao = db.waterIntakeDao()

        // 2. Создаем репозиторий
        val repository = WaterIntakeRepository(waterIntakeDao)

        // 3. Создаем фабрику ViewModel
        val factory = WaterIntakeViewModelFactory(repository)

        // 4. Получаем ViewModel с фабрикой
        viewModel = ViewModelProvider(this, factory).get(WaterIntakeViewModel::class.java)

        // Инициализация ViewModel
        viewModel = ViewModelProvider(this)[WaterIntakeViewModel::class.java]

        val waterGoalFromIntent = intent.getIntExtra("waterGoal", 2000)
        waterGoal = waterGoalFromIntent

        val sharedPreferences: SharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val savedUsername = sharedPreferences.getString("username", null)

        if (savedUsername == null) {
            startActivity(Intent(this, WelcomeActivity::class.java))
            finish()
            return
        }

        waterProgressBar = findViewById(R.id.water_progress_bar)
        waterIntakeTextView = findViewById(R.id.water_intake_text_view)
        addWaterButton = findViewById(R.id.add_water_button)
        dailyStatsTextView = findViewById(R.id.daily_stats_text_view)
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)

        waterProgressBar.max = waterGoal
        updateUI(0)

        setSupportActionBar(findViewById(R.id.toolbarMain))

        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        addWaterButton.setOnClickListener {
            showAddWaterDialog()
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_stats -> {
                    val statsIntent = Intent(this, StatsActivity::class.java)
                    startActivity(statsIntent)
                }
                R.id.nav_notification -> {
                    val notificationIntent = Intent(this, NotificationActivity::class.java)
                    startActivity(notificationIntent)
                }
                R.id.nav_goals -> {
                    val waterIntake = Intent(this, WaterIntakeActivity::class.java)
                    startActivity(waterIntake)
                }
                R.id.nav_settings -> {
                    val settingsIntent = Intent(this, SettingsActivity::class.java)
                    startActivity(settingsIntent)
                }
                R.id.nav_logout -> {
                    showLogoutConfirmationDialog()
                }
            }
            drawerLayout.closeDrawers()
            true
        }

        // Подписка на изменения данных
        lifecycleScope.launch {
            viewModel.todayTotal.collect { total ->
                updateUI(total ?: 0)
            }
        }

        lifecycleScope.launch {
            viewModel.todayRecords.collect { records ->
                updateDailyStats(records)
            }
        }
    }

    private fun updateUI(total: Int) {
        waterProgressBar.progress = total
        waterIntakeTextView.text = "Общее потребление воды: ${total} мл / ${waterGoal} мл"
    }

    private fun updateDailyStats(records: List<WaterRecord>) {
        val statsBuilder = StringBuilder("Статистика потребления воды:\n")
        var totalToday = 0

        // Сортируем записи по времени (новые сверху)
        val sortedRecords = records.sortedByDescending { it.date }

        for (record in sortedRecords) {
            val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(record.date)
            statsBuilder.append("$time: ${record.amount} мл\n")
            totalToday += record.amount
        }

        statsBuilder.append("\nВсего за сегодня: $totalToday мл")
        dailyStatsTextView.text = statsBuilder.toString()
    }

    private fun showAddWaterDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Добавить потребление воды(мл)")

        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_NUMBER
        builder.setView(input)

        builder.setPositiveButton("Добавить") { dialog, _ ->
            val waterAmountString = input.text.toString()
            val waterAmount = waterAmountString.toIntOrNull()

            if (waterAmount != null && waterAmount > 0) {
                lifecycleScope.launch {
                    viewModel.addWater(waterAmount)
                }
            } else {
                Toast.makeText(this, "Введите корректное количество воды", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Отмена") { dialog, _ -> dialog.cancel() }

        builder.show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            findViewById<View>(R.id.drawer_layout).visibility = View.VISIBLE
            super.onBackPressed()
        }
    }

    private fun showLogoutConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Подтверждение выхода")
        builder.setMessage("Вы уверены, что хотите выйти?")
        builder.setPositiveButton("Да") { dialog: DialogInterface, _: Int ->
            val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.clear()
            editor.apply()
            startActivity(Intent(this, WelcomeActivity::class.java))
            finish()
        }
        builder.setNegativeButton("Нет") { dialog: DialogInterface, _: Int ->
            dialog.dismiss()
        }
        builder.show()
    }
}