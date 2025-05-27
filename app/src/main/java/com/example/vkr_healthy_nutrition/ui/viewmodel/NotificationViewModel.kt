package com.example.vkr_healthy_nutrition.ui.viewmodel

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vkr_healthy_nutrition.WaterReminderReceiver
import com.example.vkr_healthy_nutrition.data.local.NotificationSettingsEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale
import java.text.SimpleDateFormat
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import com.example.vkr_healthy_nutrition.data.repository.NotificationSettingsRepository
import com.example.vkr_healthy_nutrition.data.repository.UserRepository
import kotlinx.coroutines.flow.collectLatest

private const val TAG = "NotificationViewModel"

class NotificationViewModel(
    private val notificationSettingsRepository: NotificationSettingsRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    private val _notificationSettings = MutableStateFlow<NotificationSettingsEntity?>(null)
    val notificationSettings: StateFlow<NotificationSettingsEntity?> = _notificationSettings.asStateFlow()

    private val _firstAlarmScheduledTime = MutableSharedFlow<Long>()
    val firstAlarmScheduledTime: SharedFlow<Long> = _firstAlarmScheduledTime.asSharedFlow()

    init {
        viewModelScope.launch {
            val userId = userRepository.currentUser?.uid
            if (userId != null) {
                notificationSettingsRepository.getNotificationSettings(userId).collectLatest { settings ->
                    _notificationSettings.value = settings
                }
            } else {
                Log.d(TAG, "init: User not logged in, cannot load notification settings.")
            }
        }
    }

    fun saveNotificationSettings(
        notificationType: Int,
        intervalMinutes: Int,
        startHour: Int,
        startMinute: Int,
        endHour: Int,
        endMinute: Int
    ) {
        viewModelScope.launch {
            val userId = userRepository.currentUser?.uid
            if (userId != null) {
                notificationSettingsRepository.saveNotificationSettings(
                    notificationType = notificationType,
                    intervalMinutes = intervalMinutes,
                    startHour = startHour,
                    startMinute = startMinute,
                    endHour = endHour,
                    endMinute = endMinute,
                    userId = userId
                )
            } else {
                Log.d(TAG, "saveNotificationSettings: User not logged in, cannot save settings.")
            }
        }
    }

    fun updateNotificationStatus(isEnabled: Boolean) {
        viewModelScope.launch {
            val userId = userRepository.currentUser?.uid
            if (userId != null) {
                notificationSettingsRepository.updateNotificationStatus(userId, isEnabled)
            } else {
                Log.d(TAG, "updateNotificationStatus: User not logged in, cannot update status.")
            }
        }
    }

    fun scheduleNotifications(context: Context) {
        Log.d(TAG, "Attempting to schedule notifications.")
        val settings = _notificationSettings.value ?: return

        val userId = userRepository.currentUser?.uid
        if (userId == null) {
            Log.d(TAG, "scheduleNotifications: User not logged in, cannot schedule.")
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return
            }
        }

        val intent = Intent(context, WaterReminderReceiver::class.java)
        val requestCode = userId.hashCode()

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        alarmManager.cancel(pendingIntent)

        if (!settings.isEnabled) return

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, settings.startHour)
            set(Calendar.MINUTE, settings.startMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss a", Locale.getDefault())
        Log.d(TAG, "First alarm scheduled for: ${sdf.format(calendar.time)}")

        Log.d(TAG, "Setting exact alarm for: ${sdf.format(calendar.time)}")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }

        viewModelScope.launch {
            _firstAlarmScheduledTime.emit(calendar.timeInMillis)
        }
    }

    fun syncNotificationSettingsFromFirestore() {
        viewModelScope.launch {
            val userId = userRepository.currentUser?.uid
            if (userId != null) {
                notificationSettingsRepository.syncNotificationSettingsFromFirestore()
            }
        }
    }
} 