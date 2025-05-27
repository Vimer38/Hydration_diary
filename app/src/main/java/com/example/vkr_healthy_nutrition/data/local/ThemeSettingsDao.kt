package com.example.vkr_healthy_nutrition.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ThemeSettingsDao {
    // Получить настройки темы для пользователя по Firebase UID
    @Query("SELECT * FROM theme_settings WHERE firebaseUid = :firebaseUid")
    fun getThemeSettings(firebaseUid: String): Flow<ThemeSettingsEntity?>

    // Сохранить или обновить настройки темы
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveThemeSettings(settings: ThemeSettingsEntity)

    // Удалить настройки темы для пользователя по Firebase UID
    @Query("DELETE FROM theme_settings WHERE firebaseUid = :firebaseUid")
    suspend fun deleteThemeSettings(firebaseUid: String)
}

