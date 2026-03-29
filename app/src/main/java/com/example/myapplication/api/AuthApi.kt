package com.example.myapplication.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

data class LoginRequest(val email: String, val password: String, val deviceId: String)
data class VerifyOtpRequest(val email: String, val otp: String, val deviceId: String)
data class VoicePrintRequest(val voiceSignature: String)

data class SignupRequest(
    val name: String,
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    val address: String,
    val cellPhone: String,
    val reasonForChoice: String
)

data class ForgotPasswordRequest(val email: String)
data class ResetPasswordRequest(val email: String, val otp: String, val newPassword: String)

// UPDATED: Added needsVoiceEnrollment to response
data class AuthResponse(
    val token: String, 
    val user: UserInfo,
    val needsVoiceEnrollment: Boolean = false
)

data class UserInfo(val id: String, val name: String, val email: String)

interface AuthApi {
    @POST("api/auth/signup")
    suspend fun signup(@Body request: SignupRequest): Response<Unit>

    @POST("api/auth/verify-otp")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): Response<Unit>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/auth/save-voice-print")
    suspend fun saveVoicePrint(@Body request: VoicePrintRequest): Response<Unit>

    @POST("api/auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<Unit>

    @POST("api/auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<Unit>
}
