package com.example.vkr_healthy_nutrition

import android.app.Application
import com.example.vkr_healthy_nutrition.auth.FirebaseAuthManager
import com.example.vkr_healthy_nutrition.data.local.AppDatabase
import com.example.vkr_healthy_nutrition.data.repository.WaterIntakeRepository
import com.example.vkr_healthy_nutrition.data.repository.NotificationSettingsRepository
import com.example.vkr_healthy_nutrition.data.repository.UserRepository
import com.example.vkr_healthy_nutrition.ui.viewmodel.NotificationViewModelFactory
import com.example.vkr_healthy_nutrition.ui.viewmodel.WaterIntakeViewModelFactory
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth


class HealthyNutritionApp : Application() {
    // База данных
    val database by lazy { AppDatabase.getDatabase(this) }

    // Firebase Auth и Firestore
    private val firebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore by lazy { FirebaseFirestore.getInstance() }

    // Firebase Auth Manager
    private val firebaseAuthManager by lazy { FirebaseAuthManager() }

    val userRepository by lazy {
        UserRepository(
            firebaseAuthManager = firebaseAuthManager,
            userDao = database.userDao()
        )
    }

    val waterIntakeRepository by lazy {
        WaterIntakeRepository(
            waterIntakeDao = database.waterIntakeDao(),
            userGoalDao = database.userGoalDao(),
            firestore = firestore,
            firebaseAuth = firebaseAuth
        )
    }

    val notificationSettingsRepository by lazy {
        NotificationSettingsRepository(
            notificationSettingsDao = database.notificationSettingsDao(),
            firestore = firestore,
            firebaseAuth = firebaseAuth

        )
    }

    // Фабрика для WaterIntakeViewModel, теперь с userRepository
    val waterIntakeViewModelFactory: WaterIntakeViewModelFactory by lazy {
        WaterIntakeViewModelFactory(waterIntakeRepository, userRepository)
    }

    // Фабрика для NotificationViewModel с userRepository
    val notificationViewModelFactory: NotificationViewModelFactory by lazy {
        NotificationViewModelFactory(notificationSettingsRepository, userRepository)
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: HealthyNutritionApp
            private set
    }
}