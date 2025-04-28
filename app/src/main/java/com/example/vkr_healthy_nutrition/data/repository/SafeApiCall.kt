package com.example.vkr_healthy_nutrition.data.repository

import retrofit2.Response

abstract class SafeApiCall {
    protected suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): Result<T> {
        return try {
            val response = apiCall()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                // Попытка прочитать тело ошибки
                val errorBody = response.errorBody()?.string()
                val errorMsg = if (!errorBody.isNullOrBlank()) {
                    errorBody // Используем сообщение из тела ошибки, если оно есть
                } else {
                    response.message() ?: "Unknown API error" // Иначе используем стандартное сообщение или запасной вариант
                }
                Result.failure(Exception("API Error ${response.code()}: $errorMsg"))
            }
        } catch (e: Exception) {
            // Обработка сетевых ошибок или других исключений
            Result.failure(Exception("Network Error: ${e.message ?: "Unknown network error"}"))
        }
    }
} 