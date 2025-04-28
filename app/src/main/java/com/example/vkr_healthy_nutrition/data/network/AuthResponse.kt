package com.example.vkr_healthy_nutrition.data.network

data class AuthResponse(
    val message: String,
    val user: UserResponse,
    val token: String
)

data class UserResponse(
    val id: Int,
    val username: String,
    val email: String
) 