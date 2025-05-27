package com.example.vkr_healthy_nutrition.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WaterIntakeDao {
    @Query("SELECT * FROM water_intakes WHERE userId = :userId ORDER BY timestamp DESC")
    fun getWaterIntakesForUser(userId: String): Flow<List<WaterIntakeEntity>>

    @Query("SELECT * FROM water_intakes WHERE userId = :userId AND timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    fun getWaterIntakesForUserInTimeRange(userId: String, startTime: Long, endTime: Long): Flow<List<WaterIntakeEntity>>

    @Query("SELECT SUM(amount) FROM water_intakes WHERE userId = :userId AND timestamp BETWEEN :startTime AND :endTime")
    suspend fun getTotalWaterIntakeForUserInTimeRange(userId: String, startTime: Long, endTime: Long): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWaterIntake(waterIntake: WaterIntakeEntity)

    @Update
    suspend fun updateWaterIntake(waterIntake: WaterIntakeEntity)

    @Delete
    suspend fun deleteWaterIntake(waterIntake: WaterIntakeEntity)

    @Query("DELETE FROM water_intakes WHERE userId = :userId")
    suspend fun deleteAllWaterIntakesForUser(userId: String)
} 