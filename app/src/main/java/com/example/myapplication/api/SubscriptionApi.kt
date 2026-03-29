package com.example.myapplication.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

data class SubscriptionResponse(
    val id: String,
    val user_id: String,
    val plan_type: String, // "free" or "paid"
    val start_date: String,
    val end_date: String,
    val status: String // "active" or "expired"
)

data class UpgradeRequest(
    val planDurationDays: Int
)

interface SubscriptionApi {
    @GET("api/subscriptions/status")
    suspend fun getStatus(): Response<SubscriptionResponse>

    @POST("api/subscriptions/upgrade")
    suspend fun upgrade(@Body request: UpgradeRequest): Response<Unit>
}
