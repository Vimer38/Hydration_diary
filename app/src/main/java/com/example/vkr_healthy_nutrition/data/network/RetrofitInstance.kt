package com.example.vkr_healthy_nutrition.data.network

import android.annotation.SuppressLint
import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.IllegalStateException

// Превращаем в класс и делаем ленивую инициализацию с проверкой
object RetrofitInstance {

    @Volatile
    private var INSTANCE: ApiService? = null
    private lateinit var appContext: Context

    // Метод для инициализации из Application класса
    fun initialize(context: Context) {
        // Проверяем, чтобы не инициализировать повторно
        if (!this::appContext.isInitialized) {
             appContext = context.applicationContext // Сохраняем ApplicationContext
        }
    }

    private fun getOkHttpClient(): OkHttpClient {
        // Проверка инициализации перед созданием клиента
        if (!this::appContext.isInitialized) {
            throw IllegalStateException("RetrofitInstance must be initialized by calling initialize(context) first.")
        }

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // Создаем AuthInterceptor, передавая ему context
        val authInterceptor = AuthInterceptor(appContext)

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor) // Добавляем наш AuthInterceptor
            .build()
    }

     // Ленивое создание ApiService с проверкой инициализации
    val api: ApiService
        @Synchronized // Потокобезопасность для синглтона
        get() {
            // Создаем экземпляр только если он null
            if (INSTANCE == null) {
                 // Проверка инициализации перед созданием Retrofit
                 if (!this::appContext.isInitialized) {
                    throw IllegalStateException("RetrofitInstance must be initialized by calling initialize(context) first.")
                 }
                INSTANCE = Retrofit.Builder()
                    .baseUrl(ApiService.BASE_URL)
                    .client(getOkHttpClient()) // Используем OkHttpClient с Interceptor'ами
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(ApiService::class.java)
            }
            // Возвращаем существующий или только что созданный экземпляр
            // !! используется, так как мы уверены, что INSTANCE не будет null после блока if
            return INSTANCE!!
        }
} 