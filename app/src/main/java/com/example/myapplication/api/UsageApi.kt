package com.example.myapplication.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

data class IncrementRequest(val feature: String)

data class UsageStatsResponse(
    val messages_sent_count: Int,
    val meetings_scheduled_count: Int,
    val emails_sent_count: Int,
    val cab_booking_count: Int,
    val other_feature_usage_count: Int
)

interface UsageApi {
    @GET("api/usage/me")
    suspend fun getStats(@Header("Authorization") token: String): Response<UsageStatsResponse>

    @POST("api/usage/increment")
    suspend fun incrementStat(
        @Header("Authorization") token: String,
        @Body request: IncrementRequest
    ): Response<UsageStatsResponse>
}
