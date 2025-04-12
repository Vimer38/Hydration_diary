package com.example.vkr_healthy_nutrition.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "water_records")
data class WaterRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val amount: Int,
    val date: Date = Date()
)