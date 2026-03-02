package com.example.myapplication.api

import com.example.myapplication.models.FitnessProfile
import retrofit2.Response
import retrofit2.http.*

interface FitnessApi {
    @POST("api/fitness/profile")
    suspend fun createOrUpdateProfile(@Body profile: FitnessProfile): Response<FitnessProfile>

    @GET("api/fitness/profile")
    suspend fun getProfile(): Response<FitnessProfile>

    @DELETE("api/fitness/profile")
    suspend fun deleteProfile(): Response<Unit>
}
