package com.example.vkr_healthy_nutrition.data.network

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val context: Context) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        
        // Получаем токен из SharedPreferences
        val token = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
            .getString("jwt_token", null)
        
        // Если токен есть, добавляем его в заголовок
        return if (token != null) {
            val authenticatedRequest = request.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
            chain.proceed(authenticatedRequest)
        } else {
            chain.proceed(request)
        }
    }
} 