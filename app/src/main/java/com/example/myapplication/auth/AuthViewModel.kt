package com.example.myapplication.auth

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.api.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Authenticated(val token: String) : AuthState()
    data class Error(val message: String) : AuthState()
    data class OtpSent(val email: String) : AuthState()
    data class PasswordResetOtpSent(val email: String) : AuthState()
    object PasswordResetSuccess : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authApi: AuthApi,
    private val tokenManager: TokenManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(if (tokenManager.getToken() != null) AuthState.Authenticated(tokenManager.getToken()!!) else AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    @SuppressLint("HardwareIds")
    private val deviceId: String = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)

    private var currentEmail: String? = null

    fun signup(name: String, email: String, password: String, firstName: String, lastName: String, address: String, cellPhone: String, reason: String) {
        viewModelScope.launch {
            currentEmail = email
            _authState.value = AuthState.Loading
            try {
                val response = authApi.signup(SignupRequest(name, email, password, firstName, lastName, address, cellPhone, reason))
                if (response.isSuccessful) {
                    _authState.value = AuthState.OtpSent(email)
                } else {
                    val errorBody = response.errorBody()?.string()
                    _authState.value = AuthState.Error(parseErrorMessage(errorBody) ?: "Signup failed")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Network error")
            }
        }
    }

    fun verifyOtp(otp: String) {
        val email = currentEmail ?: return
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val response = authApi.verifyOtp(VerifyOtpRequest(email, otp, deviceId))
                if (response.isSuccessful) {
                    _authState.value = AuthState.Idle
                } else {
                    val errorBody = response.errorBody()?.string()
                    _authState.value = AuthState.Error(parseErrorMessage(errorBody) ?: "Invalid OTP")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Network error")
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val response = authApi.login(LoginRequest(email, password, deviceId))
                if (response.isSuccessful && response.body() != null) {
                    val token = response.body()!!.token
                    tokenManager.saveToken(token)
                    _authState.value = AuthState.Authenticated(token)
                } else {
                    val errorBody = response.errorBody()?.string()
                    _authState.value = AuthState.Error(parseErrorMessage(errorBody) ?: "Login failed")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Network error")
            }
        }
    }

    fun forgotPassword(email: String) {
        viewModelScope.launch {
            currentEmail = email
            _authState.value = AuthState.Loading
            try {
                val response = authApi.forgotPassword(ForgotPasswordRequest(email))
                if (response.isSuccessful) {
                    _authState.value = AuthState.PasswordResetOtpSent(email)
                } else {
                    val errorBody = response.errorBody()?.string()
                    _authState.value = AuthState.Error(parseErrorMessage(errorBody) ?: "Reset request failed")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Network error")
            }
        }
    }

    fun resetPassword(otp: String, newPassword: String) {
        val email = currentEmail ?: return
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val response = authApi.resetPassword(ResetPasswordRequest(email, otp, newPassword))
                if (response.isSuccessful) {
                    _authState.value = AuthState.PasswordResetSuccess
                } else {
                    val errorBody = response.errorBody()?.string()
                    _authState.value = AuthState.Error(parseErrorMessage(errorBody) ?: "Password reset failed")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Network error")
            }
        }
    }

    private fun parseErrorMessage(errorBody: String?): String? {
        return try {
            val json = JSONObject(errorBody ?: "")
            json.optString("message")
        } catch (e: Exception) {
            null
        }
    }

    fun logout() {
        tokenManager.clearToken()
        _authState.value = AuthState.Idle
    }
}
