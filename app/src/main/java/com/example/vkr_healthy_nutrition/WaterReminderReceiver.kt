package com.example.vkr_healthy_nutrition

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.PendingIntent
import android.content.pm.PackageManager
import android.os.Build
import com.example.vkr_healthy_nutrition.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import android.util.Log
import com.google.firebase.auth.FirebaseAuth

class WaterReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Проверяем разрешение на уведомления
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.d("WaterReminderReceiver", "Нет разрешения на уведомления")
                return
            }
        }

        val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss a", Locale.getDefault())
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        if (currentUserId == null) {
            Log.d("WaterReminderReceiver", "Пользователь не авторизован")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            val database = AppDatabase.getDatabase(context)
            val settings = database.notificationSettingsDao().getNotificationSettings(currentUserId).first()
            
            settings?.let {
                val currentTime = Calendar.getInstance()
                val currentHour = currentTime.get(Calendar.HOUR_OF_DAY)
                val currentMinute = currentTime.get(Calendar.MINUTE)

                // Проверяем, находимся ли мы в пределах времени уведомлений
                if (currentHour >= it.startHour && currentHour <= it.endHour) {
                    if (currentHour == it.startHour && currentMinute < it.startMinute) {
                        return@let
                    }
                    if (currentHour == it.endHour && currentMinute > it.endMinute) {
                        return@let
                    }

                    // Показываем уведомление
                    NotificationActivity.showStaticWaterNotification(context)

                    // Планируем следующее уведомление
                    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    val nextIntent = Intent(context, WaterReminderReceiver::class.java)
                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
                        currentUserId.hashCode(),
                        nextIntent,
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                    )

                    val nextTime = Calendar.getInstance().apply {
                        add(Calendar.MINUTE, it.intervalMinutes)
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            nextTime.timeInMillis,
                            pendingIntent
                        )
                    } else {
                        alarmManager.setExact(
                            AlarmManager.RTC_WAKEUP,
                            nextTime.timeInMillis,
                            pendingIntent
                        )
                    }

                    Log.d("WaterReminderReceiver", "Следующее уведомление запланировано на: ${sdf.format(nextTime.time)}")
                }
            } ?: run {
                Log.d("WaterReminderReceiver", "Настройки уведомлений не найдены для пользователя $currentUserId")
            }
        }
    }
}