package com.example.vkr_healthy_nutrition.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_goals")
data class UserGoalEntity(
    @PrimaryKey
    val userId: String, // Используем userId как первичный ключ
    val waterGoal: Int, // Цель по воде в мл
    val setTimestamp: Long = System.currentTimeMillis()
)