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
    WHATSAPP, GMAIL, SLACK, SMS, HOTMAIL, AOL, YAHOO
}

sealed class IntentResult {
    data class Schedule(val appointment: Appointment) : IntentResult()
    data class CalendarInsert(
        val title: String,
        val startTime: Long,
        val durationMinutes: Int,
        val location: String? = null,
        val inviteeEmail: String? = null
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
    
    // FINANCE INTENTS
    data class AddExpense(val amount: Double, val category: String, val note: String) : IntentResult()
    data class AddIncome(val amount: Double, val source: String) : IntentResult()

    // PRODUCTIVITY INTENTS
    data class AddTask(val title: String, val description: String? = null, val priority: String = "Medium") : IntentResult()
    data class AddNote(val title: String, val content: String) : IntentResult()
}
