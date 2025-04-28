package com.example.vkr_healthy_nutrition.data.repository

import com.example.vkr_healthy_nutrition.data.network.ApiService
import com.example.vkr_healthy_nutrition.data.network.LoginRequest
import com.example.vkr_healthy_nutrition.data.network.RegisterRequest
import com.example.vkr_healthy_nutrition.data.network.RetrofitInstance
import retrofit2.Response

class AuthRepository {

    private val apiService: ApiService = RetrofitInstance.api

    suspend fun registerUser(registerRequest: RegisterRequest) = apiService.register(registerRequest)

    suspend fun loginUser(loginRequest: LoginRequest) = apiService.login(loginRequest)
} 