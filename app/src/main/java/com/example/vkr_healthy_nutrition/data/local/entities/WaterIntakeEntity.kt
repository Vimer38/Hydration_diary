package com.example.vkr_healthy_nutrition.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "water_intakes")
data class WaterIntakeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val amount: Int, // in milliliters
    val timestamp: Long = System.currentTimeMillis()

)

