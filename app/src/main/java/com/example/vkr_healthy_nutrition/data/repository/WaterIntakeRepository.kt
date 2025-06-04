package com.example.vkr_healthy_nutrition.data.repository

import android.util.Log // Import Log
import com.example.vkr_healthy_nutrition.data.local.entities.WaterIntakeEntity
import com.example.vkr_healthy_nutrition.data.local.dao.UserGoalDao
import com.example.vkr_healthy_nutrition.data.local.entities.UserGoalEntity
import com.example.vkr_healthy_nutrition.data.local.dao.WaterIntakeDao
import kotlinx.coroutines.flow.Flow
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await // Import await for Tasks
import com.google.firebase.firestore.SetOptions // Import SetOptions

private const val TAG = "WaterIntakeRepository" // Define TAG for logging

class WaterIntakeRepository(
    private val waterIntakeDao: WaterIntakeDao,
    private val userGoalDao: UserGoalDao,
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) {
    // Локальные операции
    fun getWaterIntakesForUser(userId: String): Flow<List<WaterIntakeEntity>> {
        return waterIntakeDao.getWaterIntakesForUser(userId)
    }

    fun getWaterIntakesForUserInTimeRange(
        userId: String,
        startTime: Long,
        endTime: Long
    ): Flow<List<WaterIntakeEntity>> {
        return waterIntakeDao.getWaterIntakesForUserInTimeRange(userId, startTime, endTime)
    }

    suspend fun getTotalWaterIntakeForUserInTimeRange(
        userId: String,
        startTime: Long,
        endTime: Long
    ): Int? {
        return waterIntakeDao.getTotalWaterIntakeForUserInTimeRange(userId, startTime, endTime)
    }

    suspend fun insertWaterIntake(waterIntake: WaterIntakeEntity) {
        Log.d(TAG, "insertWaterIntake: Saving water intake locally and to Firestore.") // Use Log.d
        waterIntakeDao.insertWaterIntake(waterIntake)
        saveWaterIntakeToFirestore(waterIntake)
    }

    suspend fun updateWaterIntake(waterIntake: WaterIntakeEntity) {
        waterIntakeDao.updateWaterIntake(waterIntake)
    }

    suspend fun deleteWaterIntake(waterIntake: WaterIntakeEntity) {
        waterIntakeDao.deleteWaterIntake(waterIntake)
    }

    suspend fun deleteAllWaterIntakesForUser(userId: String) {
        waterIntakeDao.deleteAllWaterIntakesForUser(userId)
    }

    // Методы для работы с UserGoalEntity

    suspend fun saveUserWaterGoal(userId: String, goal: Int) {
        Log.d(TAG, "saveUserWaterGoal: Saving water goal locally and to Firestore for user $userId: $goal.") // Use Log.d
        val userGoal = UserGoalEntity(userId = userId, waterGoal = goal)
        userGoalDao.insertOrUpdateGoal(userGoal)
        saveUserGoalToFirestore(userGoal) // Исправлено с saveWaterIntakeToFirestore
    }

    fun getUserWaterGoal(userId: String): Flow<UserGoalEntity?> {
        return userGoalDao.getUserGoal(userId)
    }

    private suspend fun saveWaterIntakeToFirestore(waterIntake: WaterIntakeEntity) {
        val userId = firebaseAuth.currentUser?.uid ?: run {
            Log.d(TAG, "saveWaterIntakeToFirestore: User not logged in, cannot save to Firestore.")
            return
        }
        Log.d(TAG, "saveWaterIntakeToFirestore: Saving water intake for user $userId with amount ${waterIntake.amount}.")

        try {
            val documentReference = firestore.collection("users").document(userId)
                .collection("waterIntakes")
                .add(waterIntake)
                .await()

            Log.d(TAG, "saveWaterIntakeToFirestore: Water intake added to Firestore with ID: ${documentReference.id}")
        } catch (e: Exception) {
            Log.e(TAG, "saveWaterIntakeToFirestore: Error adding water intake to Firestore", e)
            throw e
        }
    }

    suspend fun syncWaterIntakesFromFirestore() {
        val userId = firebaseAuth.currentUser?.uid ?: run { // Use run for returning from lambda
            Log.d(TAG, "syncWaterIntakesFromFirestore: User not logged in, cannot sync from Firestore.") // Use Log.d
            return
        }
        Log.d(TAG, "syncWaterIntakesFromFirestore: Syncing water intakes for user $userId from Firestore.") // Use Log.d
        try {
            val snapshot = firestore.collection("users").document(userId)
                .collection("waterIntakes")
                .get()
                .await()

            val waterIntakes = snapshot.documents.mapNotNull { document ->
                // Map Firestore document to WaterIntakeEntity
                // You might need to adjust this based on your Firestore document structure
                // Assuming Firestore document fields match WaterIntakeEntity properties
                Log.d(TAG, "syncWaterIntakesFromFirestore: Mapping document ${document.id} to WaterIntakeEntity.") // Use Log.d
                document.toObject(WaterIntakeEntity::class.java)?.copy(id = 0) // Room generates ID
            }

            Log.d(TAG, "syncWaterIntakesFromFirestore: Found ${waterIntakes.size} water intakes in Firestore.") // Use Log.d

            // Clear existing local data for the user and insert fresh data from Firestore
            Log.d(TAG, "syncWaterIntakesFromFirestore: Deleting all local water intakes for user $userId.") // Use Log.d
            waterIntakeDao.deleteAllWaterIntakesForUser(userId)
            Log.d(TAG, "syncWaterIntakesFromFirestore: Inserting ${waterIntakes.size} water intakes into local DB.") // Use Log.d
            waterIntakes.forEach { waterIntakeDao.insertWaterIntake(it) }

            Log.d(TAG, "syncWaterIntakesFromFirestore: Synced ${waterIntakes.size} water intakes from Firestore.") // Use Log.d

        } catch (e: Exception) {
            Log.e(TAG, "syncWaterIntakesFromFirestore: Error syncing water intakes from Firestore", e) // Use Log.e
        }
    }

    // Добавляем функцию для сохранения цели пользователя в Firestore
    private suspend fun saveUserGoalToFirestore(userGoal: UserGoalEntity) {
        val userId = firebaseAuth.currentUser?.uid ?: run {
            Log.d(TAG, "saveUserGoalToFirestore: User not logged in, cannot save goal to Firestore.") // Use Log.d
            return
        }
        if (userGoal.userId != userId) {
            Log.d(TAG, "saveUserGoalToFirestore: User ID mismatch. Cannot save goal.")
            return
        }
        Log.d(TAG, "saveUserGoalToFirestore: Saving user goal for user $userId: ${userGoal.waterGoal}.") // Use Log.d
        firestore.collection("users").document(userId)
            .collection("goals") // Новая подколлекция для целей
            .document("waterGoal") // Используем фиксированный ID для документа цели по воде
            .set(userGoal, SetOptions.merge()) // Используем set с merge для частичного обновления
            .await()
        Log.d(TAG, "saveUserGoalToFirestore: User goal saved to Firestore for user $userId.") // Use Log.d
    }

    // Добавляем функцию для синхронизации цели пользователя из Firestore
    suspend fun syncUserGoalsFromFirestore() {
        val userId = firebaseAuth.currentUser?.uid ?: run { // Use run for returning from lambda
            Log.d(TAG, "syncUserGoalsFromFirestore: User not logged in, cannot sync goals from Firestore.") // Use Log.d
            return
        }
        Log.d(TAG, "syncUserGoalsFromFirestore: Syncing user goals for user $userId from Firestore.") // Use Log.d
        try {
            val document = firestore.collection("users").document(userId)
                .collection("goals")
                .document("waterGoal")
                .get()
                .await()

            val userGoal = document.toObject(UserGoalEntity::class.java)

            if (userGoal != null) {
                Log.d(TAG, "syncUserGoalsFromFirestore: Found user goal in Firestore for user $userId. Saving locally: ${userGoal.waterGoal}.") // Use Log.d
                userGoalDao.insertOrUpdateGoal(userGoal)
            } else {
                Log.d(TAG, "syncUserGoalsFromFirestore: No user goal found in Firestore for user $userId.") // Use Log.d
                // Опционально: создать дефолтную цель локально, если ее нет нигде
            }

            Log.d(TAG, "syncUserGoalsFromFirestore: Synced user goals from Firestore for user $userId.") // Use Log.d

        } catch (e: Exception) {
            Log.e(TAG, "syncUserGoalsFromFirestore: Error syncing user goals from Firestore", e) // Use Log.e
        }
    }
}