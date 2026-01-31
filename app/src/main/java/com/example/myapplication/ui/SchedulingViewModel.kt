package com.example.myapplication.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.communication.*
import com.example.myapplication.data.SchedulingRepository
import com.example.myapplication.models.Appointment
import com.example.myapplication.plugin.IntentResult
import com.example.myapplication.voice.VoiceIntentProcessor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class SchedulingViewModel @Inject constructor(
    private val repository: SchedulingRepository,
    private val voiceProcessor: VoiceIntentProcessor,
    private val communicationManager: CommunicationManager,
    private val backgroundManager: BackgroundManager,
    private val contactHelper: ContactHelper,
    private val ttsManager: TtsManager,
    private val cabBookingManager: CabBookingManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SchedulingUiState())
    val uiState: StateFlow<SchedulingUiState> = _uiState.asStateFlow()

    init {
        observeAppointments()
    }

    private fun observeAppointments() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.getAppointments().collect { list ->
                _uiState.value = _uiState.value.copy(appointments = list, isLoading = false)
            }
        }
    }

    fun handleVoiceCommand(text: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(lastVoiceCommandResult = "ANALYZING...")
            val result = voiceProcessor.parse(text)
            
            when (result) {
                is IntentResult.CalendarInsert -> {
                    speakAndConfirm("INITIALIZING CALENDAR PROTOCOL FOR ${result.title.uppercase()}")
                    backgroundManager.insertCalendarEventBackground(result.title, result.startTime, result.durationMinutes, result.location)
                        .onSuccess { 
                            _uiState.value = _uiState.value.copy(lastVoiceCommandResult = "PROTOCOL COMPLETE: EVENT MARKED.")
                            ttsManager.speak("CALENDAR SYNCHRONIZED.")
                        }
                }

                is IntentResult.CalendarQuery -> {
                    ttsManager.speak("SCANNING TEMPORAL DATA.")
                    backgroundManager.queryCalendarEvents(result.startTime, result.endTime).onSuccess { events ->
                        if (events.isEmpty()) {
                            speakAndConfirm("NO TEMPORAL DATA DETECTED.")
                        } else {
                            val eventTitles = events.joinToString(", ") { "${it.title} AT ${formatTime(it.startTime)}" }
                            speakAndConfirm("DATA RETRIEVED: $eventTitles.")
                        }
                    }
                }

                is IntentResult.CalendarDelete -> {
                    ttsManager.speak("INITIATING PURGE FOR ${result.title.uppercase()}.")
                    backgroundManager.deleteCalendarEvent(result.title).onSuccess { count ->
                        if (count > 0) speakAndConfirm("PURGE SUCCESSFUL. REMOVED $count EVENTS.")
                        else speakAndConfirm("PURGE FAILED. TARGET NOT FOUND.")
                    }
                }

                is IntentResult.CalendarRangeDelete -> {
                    ttsManager.speak("INITIATING GLOBAL PURGE FOR THE SPECIFIED RANGE.")
                    backgroundManager.deleteCalendarEventsInRange(result.startTime, result.endTime).onSuccess { count ->
                        if (count > 0) speakAndConfirm("GLOBAL PURGE COMPLETE. $count MEETINGS REMOVED.")
                        else speakAndConfirm("SPECIFIED RANGE IS ALREADY VOID.")
                    }
                }

                is IntentResult.SendMessage -> {
                    val contact = contactHelper.findContact(result.recipient)
                    val target = contact?.phone ?: contact?.email ?: result.recipient
                    val recipientName = contact?.name?.uppercase() ?: result.recipient.uppercase()
                    speakAndConfirm("SENDING ${result.app} MESSAGE TO $recipientName SAYING ${result.message.uppercase()}.")
                    communicationManager.sendMessage(result.app, target, result.message)
                }

                is IntentResult.LastMessageQuery -> {
                    val appName = if (result.app == com.example.myapplication.plugin.CommunicationApp.WHATSAPP) "WhatsApp" else "Gmail"
                    speakAndConfirm("RETRIEVING ENCRYPTED DATA FROM $appName.")
                    val lastMsg = NotificationService.getLastMessageFrom(result.contactName, appName)
                    if (lastMsg != null) {
                        ttsManager.speak("THE LAST MESSAGE FROM ${lastMsg.sender.uppercase()} WAS: ${lastMsg.text.uppercase()}")
                    } else {
                        ttsManager.speak("NO RECENT DATA DETECTED FROM ${result.contactName.uppercase()}.")
                    }
                }

                is IntentResult.Call -> {
                    val contact = contactHelper.findContact(result.recipient)
                    val target = contact?.phone ?: result.recipient
                    speakAndConfirm("INITIATING VOICE LINK TO ${contact?.name?.uppercase() ?: result.recipient.uppercase()}.")
                    backgroundManager.makeCallWithSim(target, result.simIndex)
                }

                is IntentResult.BookCab -> {
                    speakAndConfirm("INITIALIZING CAB PROTOCOL. LAUNCHING ${result.provider} TO ${result.destination.uppercase()}.")
                    cabBookingManager.bookCab(result.provider, result.destination)
                }

                is IntentResult.Unrecognized -> {
                    ttsManager.speak("NEURAL INPUT UNRECOGNIZED.")
                    _uiState.value = _uiState.value.copy(lastVoiceCommandResult = "IDLE")
                }
                else -> {
                    _uiState.value = _uiState.value.copy(lastVoiceCommandResult = "IDLE")
                }
            }
        }
    }

    private fun formatTime(millis: Long): String {
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(millis))
    }

    private fun speakAndConfirm(text: String) {
        _uiState.value = _uiState.value.copy(lastVoiceCommandResult = text)
        ttsManager.speak(text)
    }
}
