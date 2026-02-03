package com.example.myapplication.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.api.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object OtpSent : AuthState()
    object PasswordResetOtpSent : AuthState()
    object PasswordResetSuccess : AuthState()
    data class Authenticated(val token: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authApi: AuthApi,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    private var pendingEmail: String? = null

    init {
        checkPersistedToken()
    }

    private fun checkPersistedToken() {
        val token = tokenManager.getToken()
        if (token != null) {
            _authState.value = AuthState.Authenticated(token)
        }
    }

    fun signUp(name: String, email: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val response = authApi.signup(SignupRequest(name, email, pass))
                if (response.isSuccessful) {
                    pendingEmail = email
                    _authState.value = AuthState.OtpSent
                } else {
                    _authState.value = AuthState.Error(extractError(response.errorBody()?.string()))
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Network error")
            }
        }
    }

    fun forgotPassword(email: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val response = authApi.forgotPassword(ForgotPasswordRequest(email))
                if (response.isSuccessful) {
                    pendingEmail = email
                    _authState.value = AuthState.PasswordResetOtpSent
                } else {
                    _authState.value = AuthState.Error(extractError(response.errorBody()?.string()))
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Network error")
            }
        }
    }

    fun resetPassword(otp: String, newPass: String) {
        val email = pendingEmail ?: return
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val response = authApi.resetPassword(ResetPasswordRequest(email, otp, newPass))
                if (response.isSuccessful) {
                    _authState.value = AuthState.PasswordResetSuccess
                } else {
                    _authState.value = AuthState.Error(extractError(response.errorBody()?.string()))
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Network error")
            }
        }
    }

    fun verifyOtp(otp: String) {
        val email = pendingEmail ?: return
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val response = authApi.verifyOtp(VerifyOtpRequest(email, otp))
                if (response.isSuccessful) {
                    _authState.value = AuthState.Idle 
                } else {
                    _authState.value = AuthState.Error(extractError(response.errorBody()?.string()))
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Verification error")
            }
        }
    }

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val response = authApi.login(LoginRequest(email, pass))
                if (response.isSuccessful && response.body() != null) {
                    val token = response.body()!!.token
                    tokenManager.saveToken(token)
                    _authState.value = AuthState.Authenticated(token)
                } else {
                    _authState.value = AuthState.Error(extractError(response.errorBody()?.string()))
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Login connection error")
            }
        }
    }

    private fun extractError(errorBody: String?): String {
        return try {
            val json = JSONObject(errorBody)
            json.optString("message", json.optString("error", "Action Failed"))
        } catch (e: Exception) {
            "Unknown error"
        }
    }

    fun logout() {
        tokenManager.clearToken()
        _authState.value = AuthState.Idle
    }
}
