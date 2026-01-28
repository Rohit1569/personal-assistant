package com.example.myapplication.auth

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Authenticated : AuthState()
    data class Error(val message: String) : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor() : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    fun signUp(emailInput: String, pass: String) {
        _authState.value = AuthState.Authenticated
    }

    fun login(emailInput: String, pass: String) {
        _authState.value = AuthState.Authenticated
    }
}
