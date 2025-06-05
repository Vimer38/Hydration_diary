package com.example.vkr_healthy_nutrition.ui.activities

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import com.example.vkr_healthy_nutrition.HealthyNutritionApp
import com.example.vkr_healthy_nutrition.ui.base.BaseActivity
import com.example.vkr_healthy_nutrition.R
import com.example.vkr_healthy_nutrition.data.local.entities.WaterIntakeEntity
import com.example.vkr_healthy_nutrition.ui.viewmodel.WaterIntakeViewModel
import com.example.vkr_healthy_nutrition.ui.viewmodel.WaterIntakeState
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import com.example.vkr_healthy_nutrition.ui.dialogs.EditProfileDialog
import com.example.vkr_healthy_nutrition.ui.viewmodel.NotificationViewModel

class MainActivity : BaseActivity() {

    private lateinit var waterIntakeTextView: TextView
    private lateinit var addWaterButton: Button
    private lateinit var dailyStatsTextView: TextView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var waterProgressBar: ProgressBar
    private lateinit var mainProgressBar: ProgressBar
    private lateinit var goalTextView: TextView
    private lateinit var userEmailText: TextView
    private lateinit var userNameText: TextView
    private val userRepository by lazy { (application as HealthyNutritionApp).userRepository }

    private val viewModel: WaterIntakeViewModel by viewModels {
        (application as HealthyNutritionApp).waterIntakeViewModelFactory
    }

    private val notificationViewModel: NotificationViewModel by viewModels {
        (application as HealthyNutritionApp).notificationViewModelFactory
    }

    private val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    private val displayTimeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!userRepository.isUserLoggedIn) {
            startActivity(Intent(this, WelcomeActivity::class.java))
            finish()
            return
        }
        setContentView(R.layout.activity_main)

        initViews()
        setupNavigation()
        setupClickListeners()
        observeViewModel()
        observeUserData()

        // Синхронизация данных с Firestore
        viewModel.syncWaterIntakesFromFirestore()
        notificationViewModel.syncNotificationSettingsFromFirestore()
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
        goalTextView = findViewById(R.id.water_intake_text_view)

        // Инициализация views для информации о пользователе
        userEmailText = navigationView.getHeaderView(0).findViewById(R.id.user_email_text)
        userNameText = navigationView.getHeaderView(0).findViewById(R.id.user_name_text)

        // Добавляем обработчик нажатия на имя пользователя
        userNameText.setOnClickListener {
            showEditProfileDialog()
        }
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

    private fun setupClickListeners() {
        addWaterButton.setOnClickListener {
            showAddWaterDialog()
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
        lifecycleScope.launch {
            viewModel.state.collectLatest { state ->
                when (state) {
                    is WaterIntakeState.Loading -> {
                        mainProgressBar.visibility = View.VISIBLE
                        addWaterButton.isEnabled = false
                    }
                    is WaterIntakeState.Success -> {
                        mainProgressBar.visibility = View.GONE
                        addWaterButton.isEnabled = true
                        updateDailyStats(state.records)
                        updateTotalUI(state.total)
                    }
                    is WaterIntakeState.Error -> {
                        mainProgressBar.visibility = View.GONE
                        addWaterButton.isEnabled = true
                        Toast.makeText(this@MainActivity, "Ошибка: ${state.message}", Toast.LENGTH_LONG).show()
                        updateDailyStats(emptyList())
                        updateTotalUI(0)
                    }
                    else -> {
                        mainProgressBar.visibility = View.GONE
                        addWaterButton.isEnabled = true
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.userWaterGoal.collect { userGoalEntity ->
                if (userGoalEntity != null) {
                    goalTextView.text = "Ваша цель: ${userGoalEntity.waterGoal} мл"
                    waterProgressBar.max = userGoalEntity.waterGoal
                } else {
                    goalTextView.text = "Цель не установлена"
                    waterProgressBar.max = 0
                }
            }
        }
    }

    private fun updateTotalUI(total: Int) {
        waterProgressBar.progress = total
    }

    private fun updateDailyStats(records: List<WaterIntakeEntity>) {
        val statsBuilder = StringBuilder("Статистика потребления воды:\n")
        var totalToday = 0

        for (record in records.sortedByDescending { it.timestamp }) {
            val timeString = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(record.timestamp))
            statsBuilder.append("$timeString: ${record.amount} мл\n")
            totalToday += record.amount
        }

        statsBuilder.append("\nВсего за сегодня: $totalToday мл")
        dailyStatsTextView.text = statsBuilder.toString()
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

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Подтверждение выхода")
            .setMessage("Вы уверены, что хотите выйти?")
            .setPositiveButton("Да") { dialog, which ->
                // Выполняем выход
                lifecycleScope.launch {
                    userRepository.signOut()
                    startActivity(Intent(this@MainActivity, WelcomeActivity::class.java))
                    finish()
                }
            }
            .setNegativeButton("Отмена") { dialog, which ->
                // Отменяем выход
                dialog.dismiss()
            }
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

    private fun observeUserData() {
        lifecycleScope.launch {
            userRepository.getUserFlow().collect { user ->
                user?.let {
                    userEmailText.text = it.email
                    userNameText.text = it.displayName ?: "Пользователь"
                }
            }
        }
    }

    private fun showEditProfileDialog() {
        EditProfileDialog(this, userNameText.text.toString()) { newName ->
            lifecycleScope.launch {
                userRepository.updateProfile(newName)
                    .onSuccess {
                        Toast.makeText(this@MainActivity, "Профиль обновлен", Toast.LENGTH_SHORT).show()
                    }
                    .onFailure {
                        Toast.makeText(this@MainActivity, "Ошибка обновления профиля: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }.show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}