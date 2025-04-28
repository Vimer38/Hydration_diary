package com.example.vkr_healthy_nutrition.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vkr_healthy_nutrition.data.network.AuthResponse
import com.example.vkr_healthy_nutrition.data.network.LoginRequest
import com.example.vkr_healthy_nutrition.data.network.RegisterRequest
import com.example.vkr_healthy_nutrition.data.repository.AuthRepository
import kotlinx.coroutines.launch
import retrofit2.Response

// Класс для представления состояния UI
sealed class AuthResult {
    data class Success(val data: AuthResponse) : AuthResult()
    data class Error(val message: String) : AuthResult()
    object Loading : AuthResult()
}

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _loginResult = MutableLiveData<AuthResult>()
    val loginResult: LiveData<AuthResult> = _loginResult

    private val _registerResult = MutableLiveData<AuthResult>()
    val registerResult: LiveData<AuthResult> = _registerResult

    fun loginUser(loginRequest: LoginRequest) {
        _loginResult.value = AuthResult.Loading
        viewModelScope.launch {
            try {
                val response = authRepository.loginUser(loginRequest)
                handleAuthResponse(response, _loginResult)
            } catch (e: Exception) {
                _loginResult.value = AuthResult.Error("Ошибка сети: ${e.message}")
            }
        }
    }

    fun registerUser(registerRequest: RegisterRequest) {
        _registerResult.value = AuthResult.Loading
        viewModelScope.launch {
            try {
                val response = authRepository.registerUser(registerRequest)
                handleAuthResponse(response, _registerResult)
            } catch (e: Exception) {
                _registerResult.value = AuthResult.Error("Ошибка сети: ${e.message}")
            }
        }
    }

    private fun handleAuthResponse(response: Response<AuthResponse>, liveData: MutableLiveData<AuthResult>) {
        if (response.isSuccessful && response.body() != null) {
            liveData.value = AuthResult.Success(response.body()!!)
        } else {
            // Попытка извлечь сообщение об ошибке из тела ответа
            val errorMsg = response.errorBody()?.string() ?: response.message()
            liveData.value = AuthResult.Error("Ошибка ${response.code()}: $errorMsg")
        }
    }
} 