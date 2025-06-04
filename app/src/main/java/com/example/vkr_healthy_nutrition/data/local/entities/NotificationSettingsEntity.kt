package com.example.vkr_healthy_nutrition.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notification_settings")
data class NotificationSettingsEntity(
    @PrimaryKey
    val userId: String,
    val notificationType: Int, // 0 - обычное, 1 - звуковое, 2 - вибрация, 3 - звуковое + вибрация
    val intervalMinutes: Int,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val isEnabled: Boolean = true
) 