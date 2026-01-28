package com.example.myapplication.data

import com.example.myapplication.models.Appointment
import com.example.myapplication.models.AppointmentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

interface SchedulingRepository {
    fun getAppointments(): Flow<List<Appointment>>
    suspend fun createAppointment(appointment: Appointment): Result<Appointment>
    suspend fun getAppointmentTypes(): Result<List<AppointmentType>>
}

class SchedulingRepositoryImpl : SchedulingRepository {
    
    // Cleaned up: Start with an empty list for a professional look
    private val _appointments = MutableStateFlow<List<Appointment>>(emptyList())

    override fun getAppointments(): Flow<List<Appointment>> = _appointments.asStateFlow()

    override suspend fun createAppointment(appointment: Appointment): Result<Appointment> {
        val currentList = _appointments.value.toMutableList()
        currentList.add(0, appointment)
        _appointments.value = currentList
        return Result.success(appointment)
    }

    override suspend fun getAppointmentTypes(): Result<List<AppointmentType>> {
        return Result.success(emptyList()) // Types will be dynamically handled by NLP
    }
}
