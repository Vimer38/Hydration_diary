package com.example.vkr_healthy_nutrition.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow // Используем Flow для консистентности с остальным репозиторием

@Dao
interface UserGoalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateGoal(goal: UserGoalEntity)
    @Query("SELECT * FROM user_goals WHERE userId = :userId LIMIT 1")
    fun getUserGoal(userId: String): Flow<UserGoalEntity?> // Возвращаем Flow<UserGoalEntity?>
}