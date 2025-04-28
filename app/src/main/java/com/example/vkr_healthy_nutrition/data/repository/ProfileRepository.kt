package com.example.vkr_healthy_nutrition.data.repository

import android.content.Context
import com.example.vkr_healthy_nutrition.data.network.ApiService
import com.example.vkr_healthy_nutrition.data.network.UpdateColorSchemeRequest

class ProfileRepository(private val context: Context) : SafeApiCall() {
    private val api = ApiService.create(context)

    suspend fun getUserColorScheme(): Result<Int> {
        return safeApiCall {
            api.getUserColorScheme()
        }.map { response ->
            response.colorScheme
        }
    }

    suspend fun updateUserColorScheme(colorScheme: Int): Result<Int> {
        return safeApiCall {
            api.updateUserColorScheme(UpdateColorSchemeRequest(colorScheme))
        }.map { response ->
            response.colorScheme
        }
    }
} 