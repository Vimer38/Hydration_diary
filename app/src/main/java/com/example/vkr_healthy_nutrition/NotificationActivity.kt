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
import android.view.MenuItem
import android.widget.Button
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.util.Calendar

class NotificationActivity : AppCompatActivity() {
    private val CHANNEL_ID = "water_reminder_channel"
    private val NOTIFICATION_ID = 1001

    private lateinit var spinnerType: Spinner

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

        btnTest.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 100)
                    return@setOnClickListener
                }
            }
            val type = spinnerType.selectedItemPosition
            showWaterNotification(type)
        }

        btnEnableReminder.setOnClickListener {
            val intervalMin = spinnerInterval.selectedItem.toString().toInt()
            val startHour = timePickerStart.hour
            val startMinute = timePickerStart.minute
            val endHour = timePickerEnd.hour
            val endMinute = timePickerEnd.minute

            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(this, WaterReminderReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

            // Считаем время первого срабатывания
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, startHour)
                set(Calendar.MINUTE, startMinute)
                set(Calendar.SECOND, 0)
                if (before(Calendar.getInstance())) {
                    add(Calendar.DAY_OF_MONTH, 1)
                }
            }

            // Устанавливаем повторяющийся Alarm
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                (intervalMin * 60 * 1000).toLong(),
                pendingIntent
            )
            android.widget.Toast.makeText(this, "Напоминания включены!", android.widget.Toast.LENGTH_SHORT).show()
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
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            val type = spinnerType.selectedItemPosition
            showWaterNotification(type)
        }
    }

    private fun showWaterNotification(type: Int) {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("Напоминание")
            .setContentText("Пора выпить воду!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // Настройка типа уведомления
        when (type) {
            1 -> { // Звуковое
                builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            }
            2 -> { // Вибрация
                builder.setVibrate(longArrayOf(0, 500, 300, 500))
            }
            3 -> { // Звуковое + Вибрация
                builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                builder.setVibrate(longArrayOf(0, 500, 300, 500))
            }
            else -> {
                builder.setSound(null)
                builder.setVibrate(null)
            }
        }

        with(NotificationManagerCompat.from(this)) {
            notify(NOTIFICATION_ID, builder.build())
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
        }
    }
    companion object {
        fun showStaticWaterNotification(context: Context) {
            val builder = NotificationCompat.Builder(context, "water_reminder_channel")
                .setSmallIcon(android.R.drawable.ic_popup_reminder)
                .setContentTitle("Напоминание")
                .setContentText("Пора выпить воду!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
            with(NotificationManagerCompat.from(context)) {
                notify(1002, builder.build())
            }
        }
    }
}
