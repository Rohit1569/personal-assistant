package com.example.myapplication.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.FitnessRepository
import com.example.myapplication.models.FitnessProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FitnessViewModel @Inject constructor(
    private val repository: FitnessRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<FitnessUiState>(FitnessUiState.Loading)
    val uiState: StateFlow<FitnessUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = FitnessUiState.Loading
            repository.getProfile().collect { profile ->
                _uiState.value = if (profile != null) {
                    FitnessUiState.Success(profile)
                } else {
                    FitnessUiState.NotFound
                }
            }
        }
    }

    fun saveProfile(profile: FitnessProfile) {
        viewModelScope.launch {
            _uiState.value = FitnessUiState.Loading
            val success = repository.saveProfile(profile)
            if (success) {
                loadProfile()
            } else {
                // Handle error
            }
        }
    }

    fun deleteProfile() {
        viewModelScope.launch {
            _uiState.value = FitnessUiState.Loading
            val success = repository.deleteProfile()
            if (success) {
                _uiState.value = FitnessUiState.NotFound
            }
        }
    }
}

sealed class FitnessUiState {
    object Loading : FitnessUiState()
    data class Success(val profile: FitnessProfile) : FitnessUiState()
    object NotFound : FitnessUiState()
    data class Error(val message: String) : FitnessUiState()
}
