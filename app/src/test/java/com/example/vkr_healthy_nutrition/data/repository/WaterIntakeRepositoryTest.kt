package com.example.vkr_healthy_nutrition.data.repository

import com.example.vkr_healthy_nutrition.data.local.WaterIntakeDao
import com.example.vkr_healthy_nutrition.data.local.WaterIntakeEntity
import com.example.vkr_healthy_nutrition.data.local.UserGoalDao
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.OnFailureListener
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.mockito.Mockito.* // Используйте Mockito для мокирования
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlinx.coroutines.tasks.await // Import await for Tasks
import kotlinx.coroutines.runBlocking // Import runBlocking
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.doReturn

@RunWith(RobolectricTestRunner::class)
class WaterIntakeRepositoryTest {

    private lateinit var waterIntakeDao: WaterIntakeDao
    private lateinit var userGoalDao: UserGoalDao
    private lateinit var firestore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var repository: WaterIntakeRepository

    // Mock Firebase objects
    private lateinit var mockUsersCollection: CollectionReference
    private lateinit var mockUserDocument: DocumentReference
    private lateinit var mockWaterIntakesCollection: CollectionReference
    private lateinit var mockAddTask: Task<DocumentReference>
    private lateinit var mockFirebaseUser: FirebaseUser

    @Before
    fun setUp() {
        // Создаем мок-объекты для зависимостей репозитория
        waterIntakeDao = mock()
        userGoalDao = mock()
        firestore = mock()
        firebaseAuth = mock()

        // Создаем мок-объекты для Firebase
        mockUsersCollection = mock()
        mockUserDocument = mock()
        mockWaterIntakesCollection = mock()
        mockAddTask = mock()
        mockFirebaseUser = mock()

        // Настраиваем поведение моков Firebase (не suspend вызовы)
        whenever(firestore.collection("users")).thenReturn(mockUsersCollection)
        whenever(mockUsersCollection.document(any())).thenReturn(mockUserDocument)
        whenever(mockUserDocument.collection("waterIntakes")).thenReturn(mockWaterIntakesCollection)

        // Настраиваем базовые свойства Task в setUp (не suspend)
        // Используем doReturn для явного указания возвращаемых значений для свойств
        doReturn(true).whenever(mockAddTask).isComplete
        doReturn(true).whenever(mockAddTask).isSuccessful
        doReturn(mock<DocumentReference>()).whenever(mockAddTask).result

        // Настраиваем вызов add для возврата настроенного mockAddTask
        whenever(mockWaterIntakesCollection.add(any<WaterIntakeEntity>())).thenReturn(mockAddTask)

        // Мокируем текущего пользователя в FirebaseAuth
        whenever(firebaseAuth.currentUser).thenReturn(mockFirebaseUser)
        whenever(mockFirebaseUser.uid).thenReturn("testUserId")

        // Создаем экземпляр репозитория с мок-зависимостями
        repository = WaterIntakeRepository(
            waterIntakeDao = waterIntakeDao,
            userGoalDao = userGoalDao,
            firestore = firestore,
            firebaseAuth = firebaseAuth
        )
    }

    @Test
    fun insertWaterIntake_saves() = runTest {
        // Подготовка данных
        val waterIntake = WaterIntakeEntity(
            userId = "testUserId",
            amount = 300, // Другое значение, чтобы отличить от первого теста
            timestamp = System.currentTimeMillis()
        )

        // Выполнение тестируемого метода
        repository.insertWaterIntake(waterIntake)

        // Проверка: убеждаемся, что метод insertWaterIntake в WaterIntakeDao был вызван
        verify(waterIntakeDao).insertWaterIntake(waterIntake)

        // Здесь нет проверок Firestore
    }

} 