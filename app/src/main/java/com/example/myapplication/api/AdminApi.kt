package com.example.myapplication.api

import retrofit2.Response
import retrofit2.http.*

data class UserAdminInfo(
    val id: String,
    val name: String,
    val email: String,
    val role: String,
    val is_active: Boolean,
    val created_at: String
)

data class UserFullDetails(
    val profile: UserAdminInfo,
    val fitness: Any?, // Can be more specific if needed
    val tasks: List<Any>,
    val notes: List<Any>,
    val expenses: List<Any>
)

data class AccessRequest(val is_active: Boolean)

interface AdminApi {
    @GET("api/admin/users")
    suspend fun getAllUsers(@Query("search") search: String? = null): Response<List<UserAdminInfo>>

    @GET("api/admin/users/{id}")
    suspend fun getUserDetails(@Path("id") id: String): Response<UserFullDetails>

    @PATCH("api/admin/users/{id}/access")
    suspend fun toggleAccess(@Path("id") id: String, @Body request: AccessRequest): Response<Unit>
}
