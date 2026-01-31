package com.example.myapplication.plugin

import com.example.myapplication.models.Appointment
import kotlinx.coroutines.flow.Flow

/**
 * Interface for the Scheduling Plugin.
 */
interface SchedulingPlugin {
    suspend fun bookAppointment(appointment: Appointment): Result<Boolean>
    suspend fun processVoiceCommand(text: String): IntentResult
    fun getAppointments(): Flow<List<Appointment>>
    suspend fun syncWithGoogleCalendar(): Result<Unit>
    suspend fun sendMessage(app: CommunicationApp, recipient: String, message: String): Result<Unit>
}

enum class CommunicationApp {
    WHATSAPP, GMAIL, SLACK, SMS
}

sealed class IntentResult {
    data class Schedule(val appointment: Appointment) : IntentResult()
    data class CalendarInsert(
        val title: String,
        val startTime: Long,
        val durationMinutes: Int,
        val location: String? = null
    ) : IntentResult()
    data class CalendarQuery(val startTime: Long, val endTime: Long) : IntentResult()
    data class CalendarDelete(val title: String) : IntentResult()
    data class CalendarRangeDelete(val startTime: Long, val endTime: Long) : IntentResult()
    data class SendMessage(
        val app: CommunicationApp,
        val recipient: String,
        val message: String
    ) : IntentResult()
    data class LastMessageQuery(val app: CommunicationApp, val contactName: String) : IntentResult()
    data class Call(val recipient: String, val simIndex: Int = 1) : IntentResult()
    data class BookCab(val provider: String, val destination: String) : IntentResult()
    data class Query(val query: String) : IntentResult()
    data class Unrecognized(val rawText: String) : IntentResult()
}
