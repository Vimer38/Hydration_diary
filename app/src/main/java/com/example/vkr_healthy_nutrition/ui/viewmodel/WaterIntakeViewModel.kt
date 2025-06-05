package com.example.vkr_healthy_nutrition.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vkr_healthy_nutrition.data.local.entities.UserGoalEntity
import com.example.vkr_healthy_nutrition.data.local.entities.WaterIntakeEntity
import com.example.vkr_healthy_nutrition.data.repository.UserRepository
import com.example.vkr_healthy_nutrition.data.repository.WaterIntakeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch

private const val TAG = "WaterIntakeViewModel"

class WaterIntakeViewModel(
    private val repository: WaterIntakeRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    private val _state = MutableStateFlow<WaterIntakeState>(WaterIntakeState.Idle)
    val state: StateFlow<WaterIntakeState> = _state


    val userWaterGoal: Flow<UserGoalEntity?> = userRepository.currentUser?.uid?.let { userId ->
        repository.getUserWaterGoal(userId)
    } ?: emptyFlow()
    fun loadTodayWaterData() {
        viewModelScope.launch {
            val userId = userRepository.currentUser?.uid
            if (userId == null) {
                _state.value = WaterIntakeState.Error("Пользователь не авторизован")
                Log.d(TAG, "loadTodayWaterData: User not logged in.")
                return@launch
            }
            Log.d(TAG, "loadTodayWaterData: Loading data for user $userId")
            _state.value = WaterIntakeState.Loading
            try {
                val calendar = java.util.Calendar.getInstance()
                calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                calendar.set(java.util.Calendar.MINUTE, 0)
                calendar.set(java.util.Calendar.SECOND, 0)
                calendar.set(java.util.Calendar.MILLISECOND, 0)
                val startOfDay = calendar.timeInMillis
                calendar.set(java.util.Calendar.HOUR_OF_DAY, 23)
                calendar.set(java.util.Calendar.MINUTE, 59)
                calendar.set(java.util.Calendar.SECOND, 59)
                calendar.set(java.util.Calendar.MILLISECOND, 999)
                val endOfDay = calendar.timeInMillis
                repository.getWaterIntakesForUserInTimeRange(userId, startOfDay, endOfDay)
                    .collect { records ->
                        val total = records.sumOf { it.amount }
                        _state.value = WaterIntakeState.Success(total, records)
                    }
            } catch (e: Exception) {
                _state.value = WaterIntakeState.Error(e.message ?: "Ошибка")
                Log.e(TAG, "loadTodayWaterData: Error loading data", e)
            }
        }
    }

    fun addWater(amount: Int) {
        viewModelScope.launch {
            val userId = userRepository.currentUser?.uid
            if (userId == null) {
                _state.value = WaterIntakeState.Error("Пользователь не авторизован")
                Log.d(TAG, "addWater: User not logged in.")
                return@launch
            }
            Log.d(TAG, "addWater: Using userId $userId to save water intake amount $amount.")
            try {
                repository.insertWaterIntake(
                    WaterIntakeEntity(userId = userId, amount = amount)
                )
            } catch (e: Exception) {
                _state.value = WaterIntakeState.Error(e.message ?: "Ошибка добавления")
                Log.e(TAG, "addWater: Error adding water intake", e)
            }
        }
    }

    fun deleteWaterIntake(waterIntake: WaterIntakeEntity) {
        viewModelScope.launch {
            val userId = userRepository.currentUser?.uid
            if (userId == null) {
                _state.value = WaterIntakeState.Error("Пользователь не авторизован")
                Log.d(TAG, "deleteWaterIntake: User not logged in.")
                return@launch
            }
            Log.d(TAG, "deleteWaterIntake: Using userId $userId to delete water intake.")
            try {
                repository.deleteWaterIntake(waterIntake)
            } catch (e: Exception) {
                _state.value = WaterIntakeState.Error(e.message ?: "Ошибка удаления")
                Log.e(TAG, "deleteWaterIntake: Error deleting water intake", e)
            }
        }
    }

    fun loadStatsForPeriod(startTime: Long, endTime: Long) {
        viewModelScope.launch {
            val userId = userRepository.currentUser?.uid
            if (userId == null) {
                _state.value = WaterIntakeState.Error("Пользователь не авторизован")
                Log.d(TAG, "loadStatsForPeriod: User not logged in.")
                return@launch
            }
            Log.d(TAG, "loadStatsForPeriod: Using userId $userId to load stats.")
            _state.value = WaterIntakeState.Loading
            try {
                repository.getWaterIntakesForUserInTimeRange(userId, startTime, endTime)
                .collect { records ->
                    val total = records.sumOf { it.amount }
                    _state.value = WaterIntakeState.Success(total, records)
                }
            } catch (e: Exception) {
                _state.value = WaterIntakeState.Error(e.message ?: "Ошибка")
                Log.e(TAG, "loadStatsForPeriod: Error loading stats", e)
            }
        }
    }

    fun saveWaterGoal(goal: Int) {
        viewModelScope.launch {
            val userId = userRepository.currentUser?.uid
            if (userId == null) {
                Log.d(TAG, "saveWaterGoal: User not logged in.")
                return@launch
            }
            Log.d(TAG, "saveWaterGoal: Using userId $userId to save water goal: $goal.")
            try {
                // Сохраняем цель, используя userId
                repository.saveUserWaterGoal(userId, goal)
            } catch (e: Exception) {
                Log.e(TAG, "saveWaterGoal: Error saving water goal", e)
            }
        }
    }

    fun syncWaterIntakesFromFirestore() {
        viewModelScope.launch {
            repository.syncWaterIntakesFromFirestore()
        }
    }
}