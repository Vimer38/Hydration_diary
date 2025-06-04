package com.example.vkr_healthy_nutrition.data.local.dao

import androidx.room.*
import com.example.vkr_healthy_nutrition.data.local.entities.NotificationSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationSettingsDao {
    @Query("SELECT * FROM notification_settings WHERE userId = :userId")
    fun getNotificationSettings(userId: String): Flow<NotificationSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveNotificationSettings(settings: NotificationSettingsEntity)

    @Query("UPDATE notification_settings SET isEnabled = :isEnabled WHERE userId = :userId")
    suspend fun updateNotificationStatus(userId: String, isEnabled: Boolean)

    @Query("DELETE FROM notification_settings WHERE userId = :userId")
    suspend fun deleteNotificationSettings(userId: String)
} 