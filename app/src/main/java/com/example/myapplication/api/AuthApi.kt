package com.example.myapplication.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

data class SignupRequest(val name: String, val email: String, val password: String)
data class LoginRequest(val email: String, val password: String)
data class VerifyOtpRequest(val email: String, val otp: String)
data class ForgotPasswordRequest(val email: String)
data class ResetPasswordRequest(val email: String, val otp: String, val newPassword: String)
data class AuthResponse(val token: String, val user: UserInfo)
data class UserInfo(val id: String, val name: String, val email: String)

interface AuthApi {
    @POST("api/auth/signup")
    suspend fun signup(@Body request: SignupRequest): Response<Unit>

    @POST("api/auth/verify-otp")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): Response<Unit>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<Unit>

    @POST("api/auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<Unit>
}
