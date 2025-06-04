package com.example.vkr_healthy_nutrition.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.vkr_healthy_nutrition.data.local.dao.NotificationSettingsDao
import com.example.vkr_healthy_nutrition.data.local.dao.ThemeSettingsDao
import com.example.vkr_healthy_nutrition.data.local.dao.UserDao
import com.example.vkr_healthy_nutrition.data.local.dao.UserGoalDao
import com.example.vkr_healthy_nutrition.data.local.dao.WaterIntakeDao
import com.example.vkr_healthy_nutrition.data.local.entities.NotificationSettingsEntity
import com.example.vkr_healthy_nutrition.data.local.entities.ThemeSettingsEntity
import com.example.vkr_healthy_nutrition.data.local.entities.UserEntity
import com.example.vkr_healthy_nutrition.data.local.entities.UserGoalEntity
import com.example.vkr_healthy_nutrition.data.local.entities.WaterIntakeEntity

@Database(entities = [
    WaterIntakeEntity::class,
    UserGoalEntity::class,
    ThemeSettingsEntity::class,
    NotificationSettingsEntity::class,
    UserEntity::class
], version = 5, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun waterIntakeDao(): WaterIntakeDao
    abstract fun userGoalDao(): UserGoalDao
    abstract fun themeSettingsDao(): ThemeSettingsDao
    abstract fun notificationSettingsDao(): NotificationSettingsDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "hydration_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
} 