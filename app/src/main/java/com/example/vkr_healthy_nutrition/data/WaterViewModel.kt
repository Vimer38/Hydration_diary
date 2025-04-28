package com.example.vkr_healthy_nutrition.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
// Импортируем нужные DTO и классы
import com.example.vkr_healthy_nutrition.data.network.WaterGoalResponse
import com.example.vkr_healthy_nutrition.data.network.WaterRecordResponse
import com.example.vkr_healthy_nutrition.data.repository.WaterRepository
import kotlinx.coroutines.launch

// Обновляем состояние Success, чтобы включить цель
sealed class WaterUiState {
    object Loading : WaterUiState()
    data class Success(val total: Int, val records: List<WaterRecordResponse>, val goal: Int) : WaterUiState() // Добавлена цель
    data class Error(val message: String) : WaterUiState()
    object Idle : WaterUiState()
}

sealed class WaterPeriodState {
    object Loading : WaterPeriodState()
    data class Success(
        val records: List<WaterRecordResponse>,
        val goal: Int
    ) : WaterPeriodState()
    data class Error(val message: String) : WaterPeriodState()
    object Idle : WaterPeriodState()
}

class WaterViewModel(private val repository: WaterRepository) : ViewModel() {

    private val _todayWaterState = MutableLiveData<WaterUiState>(WaterUiState.Idle)
    val todayWaterState: LiveData<WaterUiState> = _todayWaterState

    private val _weekWaterState = MutableLiveData<WaterPeriodState>(WaterPeriodState.Idle)
    val weekWaterState: LiveData<WaterPeriodState> = _weekWaterState

    private val _monthWaterState = MutableLiveData<WaterPeriodState>(WaterPeriodState.Idle)
    val monthWaterState: LiveData<WaterPeriodState> = _monthWaterState

    private val _addWaterResult = MutableLiveData<Result<WaterRecordResponse>?>()
    val addWaterResult: LiveData<Result<WaterRecordResponse>?> = _addWaterResult

    // LiveData для результата обновления цели
    private val _updateGoalResult = MutableLiveData<Result<WaterGoalResponse>?>()
    val updateGoalResult: LiveData<Result<WaterGoalResponse>?> = _updateGoalResult

    // Загружаем все данные: сумму, записи и цель
    fun loadTodayWaterData() {
        if (_todayWaterState.value is WaterUiState.Loading) return

        _todayWaterState.value = WaterUiState.Loading
        viewModelScope.launch {
            try {
                val totalResult = repository.getTodayWaterTotal()
                val recordsResult = repository.getTodayWaterRecords()
                val goalResult = repository.getUserWaterGoal()

                totalResult.onSuccess { totalResponse ->
                    recordsResult.onSuccess { records ->
                        goalResult.onSuccess { goalResponse ->
                            _todayWaterState.postValue(WaterUiState.Success(
                                total = totalResponse.total,
                                records = records,
                                goal = goalResponse.waterGoal
                            ))
                        }
                    }
                }

                when {
                    totalResult.isFailure -> _todayWaterState.postValue(WaterUiState.Error(totalResult.exceptionOrNull()?.message ?: "Unknown error"))
                    recordsResult.isFailure -> _todayWaterState.postValue(WaterUiState.Error(recordsResult.exceptionOrNull()?.message ?: "Unknown error"))
                    goalResult.isFailure -> _todayWaterState.postValue(WaterUiState.Error(goalResult.exceptionOrNull()?.message ?: "Unknown error"))
                }
            } catch (e: Exception) {
                _todayWaterState.postValue(WaterUiState.Error("Unexpected error: ${e.message}"))
            }
        }
    }

    fun loadWeekWaterData() {
        if (_weekWaterState.value is WaterPeriodState.Loading) return

        _weekWaterState.value = WaterPeriodState.Loading
        viewModelScope.launch {
            try {
                val recordsResult = repository.getWeekWaterRecords()
                val goalResult = repository.getUserWaterGoal()

                recordsResult.onSuccess { records ->
                    goalResult.onSuccess { goalResponse ->
                        _weekWaterState.postValue(WaterPeriodState.Success(
                            records = records,
                            goal = goalResponse.waterGoal
                        ))
                    }
                }

                when {
                    recordsResult.isFailure -> _weekWaterState.postValue(WaterPeriodState.Error(recordsResult.exceptionOrNull()?.message ?: "Unknown error"))
                    goalResult.isFailure -> _weekWaterState.postValue(WaterPeriodState.Error(goalResult.exceptionOrNull()?.message ?: "Unknown error"))
                }
            } catch (e: Exception) {
                _weekWaterState.postValue(WaterPeriodState.Error("Unexpected error: ${e.message}"))
            }
        }
    }

    fun loadMonthWaterData() {
        if (_monthWaterState.value is WaterPeriodState.Loading) return

        _monthWaterState.value = WaterPeriodState.Loading
        viewModelScope.launch {
            try {
                val recordsResult = repository.getMonthWaterRecords()
                val goalResult = repository.getUserWaterGoal()

                recordsResult.onSuccess { records ->
                    goalResult.onSuccess { goalResponse ->
                        _monthWaterState.postValue(WaterPeriodState.Success(
                            records = records,
                            goal = goalResponse.waterGoal
                        ))
                    }
                }

                when {
                    recordsResult.isFailure -> _monthWaterState.postValue(WaterPeriodState.Error(recordsResult.exceptionOrNull()?.message ?: "Unknown error"))
                    goalResult.isFailure -> _monthWaterState.postValue(WaterPeriodState.Error(goalResult.exceptionOrNull()?.message ?: "Unknown error"))
                }
            } catch (e: Exception) {
                _monthWaterState.postValue(WaterPeriodState.Error("Unexpected error: ${e.message}"))
            }
        }
    }

    // Добавление воды (остается без изменений)
    fun addWater(amount: Int) {
        viewModelScope.launch {
            try {
                val result = repository.addWaterRecord(amount)
                _addWaterResult.postValue(result)
                loadTodayWaterData() // Refresh data after adding water
            } catch (e: Exception) {
                _addWaterResult.postValue(Result.failure(e))
            }
        }
    }

    // Новая функция для обновления цели по воде
    fun updateWaterGoal(newGoal: Int) {
        _updateGoalResult.value = null // Сбрасываем предыдущий результат
        // Можно добавить индикатор загрузки для обновления цели, если нужно
        viewModelScope.launch {
            val result = repository.updateUserWaterGoal(newGoal)
            _updateGoalResult.postValue(result)
            result.onSuccess {
                loadTodayWaterData()
            }
        }
    }
}