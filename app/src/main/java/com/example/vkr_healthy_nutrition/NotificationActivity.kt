package com.example.vkr_healthy_nutrition

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.lifecycleScope
import com.example.vkr_healthy_nutrition.ui.viewmodel.NotificationViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.*
import com.example.vkr_healthy_nutrition.HealthyNutritionApp

class NotificationActivity : BaseActivity() {
    private val CHANNEL_ID = "water_reminder_channel"
    private val NOTIFICATION_ID = 1001
    private val PERMISSION_REQUEST_CODE = 100

    private lateinit var spinnerType: Spinner
    private val viewModel: NotificationViewModel by viewModels {
        (application as HealthyNutritionApp).notificationViewModelFactory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)

        val appBar: Toolbar = findViewById(R.id.toolbar_set)
        setSupportActionBar(appBar)

        supportActionBar?.title = "Напоминания"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        spinnerType = findViewById(R.id.spinner_notification_type)
        val btnTest: Button = findViewById(R.id.btn_test_notification)
        val spinnerInterval: Spinner = findViewById(R.id.spinner_interval)
        val timePickerStart = findViewById<android.widget.TimePicker>(R.id.timepicker_start)
        val timePickerEnd = findViewById<android.widget.TimePicker>(R.id.timepicker_end)
        timePickerStart.setIs24HourView(true)
        timePickerEnd.setIs24HourView(true)
        val btnEnableReminder: Button = findViewById(R.id.btn_enable_reminder)

        createNotificationChannel()

        // Загружаем сохраненные настройки
        lifecycleScope.launch {
            viewModel.notificationSettings.collectLatest { settings ->
                settings?.let {
                    Log.d("NotificationActivity", "Loaded settings: isEnabled = ${it.isEnabled}")
                    spinnerType.setSelection(it.notificationType)
                    spinnerInterval.setSelection(getIntervalPosition(it.intervalMinutes))
                    timePickerStart.hour = it.startHour
                    timePickerStart.minute = it.startMinute
                    timePickerEnd.hour = it.endHour
                    timePickerEnd.minute = it.endMinute
                    btnEnableReminder.text = if (it.isEnabled) "Отключить напоминания" else "Включить напоминания"
                }
            }
        }

        // Собираем время первого запланированного уведомления из ViewModel
        lifecycleScope.launch {
            viewModel.firstAlarmScheduledTime.collectLatest { timeInMillis ->
                val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss a", Locale.getDefault())
                val formattedTime = sdf.format(Date(timeInMillis))
                Snackbar.make(
                    findViewById(R.id.notification), // Используйте корневой layout или другой View в вашем Activity
                    "Первое уведомление запланировано на: $formattedTime",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }

        btnTest.setOnClickListener {
            if (!checkNotificationPermission()) {
                requestNotificationPermission()
                return@setOnClickListener
            }
            val type = spinnerType.selectedItemPosition
            showWaterNotification(type)
        }

        btnEnableReminder.setOnClickListener {
            if (!checkNotificationPermission()) {
                requestNotificationPermission()
                return@setOnClickListener
            }

            val intervalMin = spinnerInterval.selectedItem.toString().toInt()
            val startHour = timePickerStart.hour
            val startMinute = timePickerStart.minute
            val endHour = timePickerEnd.hour
            val endMinute = timePickerEnd.minute

            // Сохраняем настройки
            viewModel.saveNotificationSettings(
                notificationType = spinnerType.selectedItemPosition,
                intervalMinutes = intervalMin,
                startHour = startHour,
                startMinute = startMinute,
                endHour = endHour,
                endMinute = endMinute
            )

            // Обновляем статус уведомлений
            val isCurrentlyEnabled = viewModel.notificationSettings.value?.isEnabled ?: false
            val newStatus = !isCurrentlyEnabled

            viewModel.updateNotificationStatus(newStatus)

            if (newStatus) {
                // Если включили напоминания, планируем их
                // Ждем, пока настройки обновятся в ViewModel
                lifecycleScope.launch {
                    viewModel.notificationSettings.collectLatest { settings ->
                         if (settings?.isEnabled == true) {
                             Log.d("NotificationActivity", "Settings updated to enabled, scheduling notifications.")
                             viewModel.scheduleNotifications(this@NotificationActivity)
                             // Прекращаем сбор, так как мы запланировали уведомления
                             // Если вы хотите перепланировать при каждом изменении, уберите return@collectLatest
                             return@collectLatest
                         }
                    }
                }
            } else {
                // Если отключили напоминания, отменяем их
                val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val intent = Intent(this, WaterReminderReceiver::class.java)
                val pendingIntent = PendingIntent.getBroadcast(
                    this,
                    0,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
                alarmManager.cancel(pendingIntent)

                Toast.makeText(
                    this,
                    "Напоминания отключены!",
                    Toast.LENGTH_SHORT
                ).show()
            }

            // Обновляем текст кнопки
            btnEnableReminder.text = if (newStatus) "Отключить напоминания" else "Включить напоминания"
        }
    }

    private fun checkNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun getIntervalPosition(intervalMinutes: Int): Int {
        return when (intervalMinutes) {
            30 -> 0
            45 -> 1
            60 -> 2
            90 -> 3
            120 -> 4
            else -> 2
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Разрешение на уведомления получено", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Для работы уведомлений необходимо разрешение", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showWaterNotification(type: Int) {
        if (!checkNotificationPermission()) {
            Log.d("NotificationActivity", "Нет разрешения на уведомления, запрашиваем...")
            requestNotificationPermission()
            return
        }

        Log.d("NotificationActivity", "Показываем тестовое уведомление типа: $type")

        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("Напоминание")
            .setContentText("Пора выпить воду!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        when (type) {
            1 -> { // Звуковое
                Log.d("NotificationActivity", "Устанавливаем звуковое уведомление")
                builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            }
            2 -> { // Вибрация
                Log.d("NotificationActivity", "Устанавливаем уведомление с вибрацией")
                builder.setVibrate(longArrayOf(0, 500, 300, 500))
            }
            3 -> { // Звуковое + Вибрация
                Log.d("NotificationActivity", "Устанавливаем уведомление со звуком и вибрацией")
                builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                builder.setVibrate(longArrayOf(0, 500, 300, 500))
            }
            else -> {
                Log.d("NotificationActivity", "Устанавливаем обычное уведомление")
                builder.setSound(null)
                builder.setVibrate(null)
            }
        }

        try {
            with(NotificationManagerCompat.from(this)) {
                notify(NOTIFICATION_ID, builder.build())
                Log.d("NotificationActivity", "Уведомление успешно показано")
            }
        } catch (e: Exception) {
            Log.e("NotificationActivity", "Ошибка при показе уведомления", e)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Напоминания о воде"
            val descriptionText = "Канал для напоминаний о необходимости выпить воду"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
                val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build()
                setSound(soundUri, audioAttributes)
            }
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.d("NotificationActivity", "Канал уведомлений создан: $CHANNEL_ID")
        }
    }

    companion object {
        fun showStaticWaterNotification(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    return
                }
            }

            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

            val builder = NotificationCompat.Builder(context, "water_reminder_channel")
                .setSmallIcon(android.R.drawable.ic_popup_reminder)
                .setContentTitle("Напоминание")
                .setContentText("Пора выпить воду!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)

            with(NotificationManagerCompat.from(context)) {
                notify(1002, builder.build())
            }
        }
    }
}
