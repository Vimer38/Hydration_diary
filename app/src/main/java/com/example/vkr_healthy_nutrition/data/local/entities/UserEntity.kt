package com.example.vkr_healthy_nutrition.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val uid: String,
    val email: String,
    val displayName: String?,
    val photoUrl: String?,
    val lastLoginTimestamp: Long = System.currentTimeMillis()
) 