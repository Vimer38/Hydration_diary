package com.example.vkr_healthy_nutrition.data.network

import android.content.Context
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

interface ApiService {

    companion object {
        // TODO: Replace with your actual local IP address
        const val BASE_URL = "http://192.168.0.14:3000/"

        fun create(context: Context): ApiService {
            val client = OkHttpClient.Builder()
                .addInterceptor(AuthInterceptor(context))
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
        }
    }

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/water")
    suspend fun addWaterRecord(
        @Body request: AddWaterRequest
    ): Response<WaterRecordResponse>

    @GET("api/water/today")
    suspend fun getTodayWaterRecords(): Response<List<WaterRecordResponse>>

    @GET("api/water/week")
    suspend fun getWeekWaterRecords(): Response<List<WaterRecordResponse>>

    @GET("api/water/month")
    suspend fun getMonthWaterRecords(): Response<List<WaterRecordResponse>>

    @GET("api/water/today/total")
    suspend fun getTodayWaterTotal(): Response<TodayWaterTotalResponse>

    @GET("api/profile/water-goal")
    suspend fun getUserWaterGoal(): Response<WaterGoalResponse>

    @PUT("api/profile/water-goal")
    suspend fun updateUserWaterGoal(
        @Body request: UpdateWaterGoalRequest
    ): Response<WaterGoalResponse>

    @GET("api/profile/color-scheme")
    suspend fun getUserColorScheme(): Response<ColorSchemeResponse>

    @PUT("api/profile/color-scheme")
    suspend fun updateUserColorScheme(
        @Body request: UpdateColorSchemeRequest
    ): Response<ColorSchemeResponse>
}

data class AddWaterRequest(
    val amount: Int
)

data class WaterRecordResponse(
    val id: Int,
    val user_id: Int? = null,
    val amount: Int,
    val record_time: String
)

data class TodayWaterTotalResponse(
    val total: Int
)

data class UpdateWaterGoalRequest(
    val waterGoal: Int
)

data class WaterGoalResponse(
    val waterGoal: Int,
    val message: String? = null
)

data class UpdateColorSchemeRequest(
    val colorScheme: Int
)

data class ColorSchemeResponse(
    val colorScheme: Int,
    val message: String? = null
) 