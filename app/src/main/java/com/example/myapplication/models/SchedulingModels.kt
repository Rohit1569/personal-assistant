package com.example.myapplication.models

import java.util.Date
import java.util.UUID

data class AppointmentType(
    val id: String = UUID.randomUUID().toString(),
    val slug: String,
    val displayName: String,
    val isFreeTier: Boolean
)

data class ContactDetails(
    val name: String,
    val phone: String? = null,
    val email: String? = null
)

data class Appointment(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val type: AppointmentType,
    val startTime: Long, // Epoch timestamp
    val durationMinutes: Int,
    val location: String? = null,
    val notes: String? = null,
    val contact: ContactDetails? = null,
    val syncStatus: SyncStatus = SyncStatus.PENDING
)

enum class SyncStatus {
    PENDING, SYNCED, ERROR
}
