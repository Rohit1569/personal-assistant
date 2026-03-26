package com.example.myapplication.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.api.SubscriptionApi
import com.example.myapplication.api.SubscriptionResponse
import com.example.myapplication.api.UpgradeRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SubscriptionState {
    object Idle : SubscriptionState()
    object Loading : SubscriptionState()
    data class Success(val subscription: SubscriptionResponse) : SubscriptionState()
    data class Expired(val subscription: SubscriptionResponse) : SubscriptionState()
    data class Error(val message: String) : SubscriptionState()
}

@HiltViewModel
class SubscriptionViewModel @Inject constructor(
    private val subscriptionApi: SubscriptionApi
) : ViewModel() {

    private val _subscriptionState = MutableStateFlow<SubscriptionState>(SubscriptionState.Idle)
    val subscriptionState: StateFlow<SubscriptionState> = _subscriptionState

    fun checkSubscriptionStatus() {
        viewModelScope.launch {
            _subscriptionState.value = SubscriptionState.Loading
            try {
                val response = subscriptionApi.getStatus()
                if (response.isSuccessful && response.body() != null) {
                    val sub = response.body()!!
                    if (sub.status == "expired") {
                        _subscriptionState.value = SubscriptionState.Expired(sub)
                    } else {
                        _subscriptionState.value = SubscriptionState.Success(sub)
                    }
                } else {
                    _subscriptionState.value = SubscriptionState.Error("Failed to fetch subscription status")
                }
            } catch (e: Exception) {
                _subscriptionState.value = SubscriptionState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun upgradePlan(days: Int) {
        viewModelScope.launch {
            _subscriptionState.value = SubscriptionState.Loading
            try {
                val response = subscriptionApi.upgrade(UpgradeRequest(days))
                if (response.isSuccessful) {
                    checkSubscriptionStatus()
                } else {
                    _subscriptionState.value = SubscriptionState.Error("Upgrade failed")
                }
            } catch (e: Exception) {
                _subscriptionState.value = SubscriptionState.Error(e.message ?: "Upgrade error")
            }
        }
    }
}
