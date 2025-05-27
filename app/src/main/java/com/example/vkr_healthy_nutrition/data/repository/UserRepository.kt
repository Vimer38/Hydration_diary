package com.example.vkr_healthy_nutrition.data.repository

import com.example.vkr_healthy_nutrition.auth.FirebaseAuthManager
import com.example.vkr_healthy_nutrition.data.local.UserDao
import com.example.vkr_healthy_nutrition.data.local.UserEntity
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class UserRepository(
    private val firebaseAuthManager: FirebaseAuthManager,
    private val userDao: UserDao
) {
    val currentUser: FirebaseUser?
        get() = firebaseAuthManager.currentUser

    val isUserLoggedIn: Boolean
        get() = firebaseAuthManager.isUserLoggedIn

    suspend fun signIn(email: String, password: String): Result<FirebaseUser> {
        return firebaseAuthManager.signIn(email, password).also { result ->
            result.getOrNull()?.let { user ->
                syncUserToLocal(user)
            }
        }
    }

    suspend fun signUp(email: String, password: String): Result<FirebaseUser> {
        return firebaseAuthManager.signUp(email, password).also { result ->
            result.getOrNull()?.let { user ->
                syncUserToLocal(user)
            }
        }
    }

    suspend fun signOut() {
        currentUser?.uid?.let { uid ->
            userDao.deleteUser(uid)
        }
        firebaseAuthManager.signOut()
    }

    suspend fun resetPassword(email: String): Result<Unit> {
        return firebaseAuthManager.resetPassword(email)
    }

    suspend fun updateProfile(displayName: String): Result<Unit> {
        return try {
            val user = currentUser ?: return Result.failure(Exception("Пользователь не авторизован"))

            // Обновляем профиль в Firebase
            val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build()
            user.updateProfile(profileUpdates).await()

            // Обновляем данные в локальной базе
            userDao.getUserByIdSync(user.uid)?.let { userEntity ->
                userDao.updateUser(userEntity.copy(displayName = displayName))
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getUserFlow(): Flow<UserEntity?> {
        return currentUser?.uid?.let { uid ->
            userDao.getUserById(uid)
        } ?: kotlinx.coroutines.flow.flowOf(null)
    }

    private suspend fun syncUserToLocal(firebaseUser: FirebaseUser) {
        val userEntity = UserEntity(
            uid = firebaseUser.uid,
            email = firebaseUser.email ?: "",
            displayName = firebaseUser.displayName,
            photoUrl = firebaseUser.photoUrl?.toString(),
            lastLoginTimestamp = System.currentTimeMillis()
        )
        userDao.insertUser(userEntity)
    }
} 