package com.example.myapplication.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.api.AdminApi
import com.example.myapplication.api.UserAdminInfo
import com.example.myapplication.api.UserFullDetails
import com.example.myapplication.api.AccessRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val adminApi: AdminApi
) : ViewModel() {

    private val _users = MutableStateFlow<List<UserAdminInfo>>(emptyList())
    val users: StateFlow<List<UserAdminInfo>> = _users

    private val _selectedUserDetails = MutableStateFlow<UserFullDetails?>(null)
    val selectedUserDetails: StateFlow<UserFullDetails?> = _selectedUserDetails

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadUsers()
    }

    fun loadUsers(search: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = adminApi.getAllUsers(search)
                if (response.isSuccessful) {
                    _users.value = response.body() ?: emptyList()
                }
            } catch (e: Exception) { }
            _isLoading.value = false
        }
    }

    fun loadUserDetails(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = adminApi.getUserDetails(userId)
                if (response.isSuccessful) {
                    _selectedUserDetails.value = response.body()
                }
            } catch (e: Exception) { }
            _isLoading.value = false
        }
    }

    fun toggleUserAccess(userId: String, currentStatus: Boolean) {
        viewModelScope.launch {
            try {
                val response = adminApi.toggleAccess(userId, AccessRequest(!currentStatus))
                if (response.isSuccessful) {
                    loadUsers() // Refresh list
                    if (_selectedUserDetails.value?.profile?.id == userId) {
                        loadUserDetails(userId) // Refresh details
                    }
                }
            } catch (e: Exception) { }
        }
    }
    
    fun clearDetails() {
        _selectedUserDetails.value = null
    }
}
