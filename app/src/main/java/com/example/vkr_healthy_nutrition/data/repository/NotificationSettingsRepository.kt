package com.example.vkr_healthy_nutrition.data.repository

import android.util.Log // Import Log for logging
import com.example.vkr_healthy_nutrition.data.local.dao.NotificationSettingsDao
import com.example.vkr_healthy_nutrition.data.local.entities.NotificationSettingsEntity
import kotlinx.coroutines.flow.Flow
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await // Import await for Tasks
import com.google.firebase.firestore.SetOptions // Import SetOptions
import kotlinx.coroutines.flow.firstOrNull // Import firstOrNull

private const val TAG = "NotificationSettingsRepository"

class NotificationSettingsRepository(
    private val notificationSettingsDao: NotificationSettingsDao,
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) {
    fun getNotificationSettings(userId: String): Flow<NotificationSettingsEntity?> {
        return notificationSettingsDao.getNotificationSettings(userId)
    }

    suspend fun saveNotificationSettings(
        notificationType: Int,
        intervalMinutes: Int,
        startHour: Int,
        startMinute: Int,
        endHour: Int,
        endMinute: Int,
        userId: String
    ) {
        val settings = NotificationSettingsEntity(
            userId = userId,
            notificationType = notificationType,
            intervalMinutes = intervalMinutes,
            startHour = startHour,
            startMinute = startMinute,
            endHour = endHour,
            endMinute = endMinute,
            isEnabled = true
        )
        notificationSettingsDao.saveNotificationSettings(settings)
        saveNotificationSettingsToFirestore(settings)
    }

    suspend fun updateNotificationStatus(userId: String, isEnabled: Boolean) {
        notificationSettingsDao.updateNotificationStatus(userId, isEnabled)
        // Получаем текущие настройки, чтобы обновить их в Firestore
        val currentSettings = notificationSettingsDao.getNotificationSettings(userId).firstOrNull()
        currentSettings?.copy(isEnabled = isEnabled)?.let { saveNotificationSettingsToFirestore(it) }
    }

    suspend fun deleteNotificationSettings(userId: String) {
        notificationSettingsDao.deleteNotificationSettings(userId)
        deleteNotificationSettingsFromFirestore(userId)
    }

    private suspend fun saveNotificationSettingsToFirestore(settings: NotificationSettingsEntity) {
        val userId = firebaseAuth.currentUser?.uid ?: run {
            Log.d(TAG, "saveNotificationSettingsToFirestore: User not logged in, cannot save to Firestore.")
            return
        }
        Log.d(TAG, "saveNotificationSettingsToFirestore: Saving settings for user $userId.")
        firestore.collection("users").document(userId)
            .collection("notificationSettings")
            .document(userId) // Используем userId как ID документа, так как у пользователя только один набор настроек
            .set(settings, SetOptions.merge())
            .await()
        Log.d(TAG, "saveNotificationSettingsToFirestore: Settings saved to Firestore for user $userId.")
    }

     private suspend fun deleteNotificationSettingsFromFirestore(userId: String) {
         val currentUserId = firebaseAuth.currentUser?.uid ?: run {
             Log.d(TAG, "deleteNotificationSettingsFromFirestore: User not logged in, cannot delete from Firestore.")
             return
         }
         if (userId != currentUserId) {
              Log.d(TAG, "deleteNotificationSettingsFromFirestore: Cannot delete settings for another user.")
              return
         }
         Log.d(TAG, "deleteNotificationSettingsFromFirestore: Deleting settings for user $userId from Firestore.")
         firestore.collection("users").document(userId)
             .collection("notificationSettings")
             .document(userId)
             .delete()
             .await()
         Log.d(TAG, "deleteNotificationSettingsFromFirestore: Settings deleted from Firestore for user $userId.")
     }

    suspend fun syncNotificationSettingsFromFirestore() {
        val userId = firebaseAuth.currentUser?.uid ?: run {
            Log.d(TAG, "syncNotificationSettingsFromFirestore: User not logged in, cannot sync from Firestore.")
            return
        }
        Log.d(TAG, "syncNotificationSettingsFromFirestore: Syncing settings for user $userId from Firestore.")
        try {
            val document = firestore.collection("users").document(userId)
                .collection("notificationSettings")
                .document(userId)
                .get()
                .await()

            val settings = document.toObject(NotificationSettingsEntity::class.java)

            if (settings != null) {
                Log.d(TAG, "syncNotificationSettingsFromFirestore: Found settings in Firestore for user $userId. Saving locally.")
                notificationSettingsDao.saveNotificationSettings(settings)
            } else {
                Log.d(TAG, "syncNotificationSettingsFromFirestore: No settings found in Firestore for user $userId.")

            }

            Log.d(TAG, "syncNotificationSettingsFromFirestore: Synced settings from Firestore for user $userId.")

        } catch (e: Exception) {
            Log.e(TAG, "syncNotificationSettingsFromFirestore: Error syncing settings from Firestore", e)
        }
    }
} 