package com.example.myapplication.ui

import com.example.myapplication.models.Appointment

data class SchedulingUiState(
    val appointments: List<Appointment> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val lastVoiceCommandResult: String? = null
)
