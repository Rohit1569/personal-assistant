package com.example.myapplication.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.api.IncrementRequest
import com.example.myapplication.api.UsageApi
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
    private val cabBookingManager: CabBookingManager,
    private val externalAppManager: ExternalAppManager,
    private val neuralMicManager: NeuralMicManager,
    private val usageApi: UsageApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(SchedulingUiState())
    val uiState: StateFlow<SchedulingUiState> = _uiState.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private var userToken: String? = null

    init {
        observeAppointments()
    }

    fun setToken(token: String) {
        userToken = "Bearer $token"
    }

    private fun observeAppointments() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.getAppointments().collect { list ->
                _uiState.value = _uiState.value.copy(appointments = list, isLoading = false)
            }
        }
    }

    fun startNeuralListening() {
        neuralMicManager.startListening(
            onState = { listening -> _isListening.value = listening },
            onResultFound = { command -> handleVoiceCommand(command) }
        )
    }

    private fun trackUsage(feature: String) {
        val token = userToken ?: return
        viewModelScope.launch {
            try {
                usageApi.incrementStat(token, IncrementRequest(feature))
            } catch (e: Exception) {
                // Silent fail for tracking
            }
        }
    }

    fun handleVoiceCommand(text: String) {
        viewModelScope.launch {
            _isListening.value = false
            _uiState.value = _uiState.value.copy(lastVoiceCommandResult = "ANALYZING NEURAL INPUT...")
            val result = voiceProcessor.parse(text)
            
            when (result) {
                is IntentResult.CalendarInsert -> {
                    val contact = result.inviteeEmail?.let { contactHelper.findContact(it) }
                    val targetEmail = contact?.email
                    
                    if (result.inviteeEmail != null && targetEmail == null) {
                        speakAndConfirm("PROTOCOL ABORTED. EMAIL FOR ${result.inviteeEmail.uppercase()} NOT FOUND IN CONTACTS.")
                    } else {
                        val speechText = if (targetEmail != null) {
                            "INITIALIZING CALENDAR PROTOCOL FOR ${result.title.uppercase()} WITH ${contact?.name?.uppercase()}. SENDING INVITE."
                        } else {
                            "INITIALIZING CALENDAR PROTOCOL FOR ${result.title.uppercase()}. MARKING YOUR CALENDAR."
                        }
                        
                        speakAndConfirm(speechText)
                        backgroundManager.insertCalendarEventBackground(result.title, result.startTime, result.durationMinutes, result.location)
                            .onSuccess { 
                                _uiState.value = _uiState.value.copy(lastVoiceCommandResult = "PROTOCOL COMPLETE.")
                                ttsManager.speak("CALENDAR SYNCHRONIZED.")
                                trackUsage("MEETING")
                                if (targetEmail != null) {
                                    backgroundManager.sendGmailIntent(targetEmail, "Meeting Invitation: ${result.title}", "Hi ${contact?.name}, I have scheduled a meeting: ${result.title} at ${formatTime(result.startTime)}.")
                                }
                            }
                    }
                }

                is IntentResult.CalendarQuery -> {
                    speakAndConfirm("SCANNING TEMPORAL DATA.")
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
                    speakAndConfirm("INITIATING PURGE FOR ${result.title.uppercase()}.")
                    backgroundManager.deleteCalendarEvent(result.title).onSuccess { count ->
                        if (count > 0) speakAndConfirm("PURGE SUCCESSFUL. REMOVED $count EVENTS.")
                        else speakAndConfirm("PURGE FAILED. TARGET NOT FOUND.")
                    }
                }

                is IntentResult.CalendarRangeDelete -> {
                    speakAndConfirm("INITIATING GLOBAL PURGE FOR THE SPECIFIED RANGE.")
                    backgroundManager.deleteCalendarEventsInRange(result.startTime, result.endTime).onSuccess { count ->
                        if (count > 0) speakAndConfirm("GLOBAL PURGE COMPLETE. $count MEETINGS REMOVED.")
                        else speakAndConfirm("SPECIFIED RANGE IS ALREADY VOID.")
                    }
                }

                is IntentResult.SendMessage -> {
                    val contact = contactHelper.findContact(result.recipient)
                    val target = if (result.app == com.example.myapplication.plugin.CommunicationApp.GMAIL) contact?.email else contact?.phone
                    
                    if (target == null && contact == null) {
                        speakAndConfirm("PROTOCOL FAILED. ${result.recipient.uppercase()} NOT FOUND IN CONTACTS.")
                    } else {
                        val finalTarget = target ?: result.recipient
                        val recipientName = contact?.name?.uppercase() ?: result.recipient.uppercase()
                        speakAndConfirm("SENDING ${result.app} MESSAGE TO $recipientName SAYING ${result.message.uppercase()}.")
                        communicationManager.sendMessage(result.app, finalTarget, result.message)
                        trackUsage(if (result.app == com.example.myapplication.plugin.CommunicationApp.GMAIL) "EMAIL" else "MESSAGE")
                    }
                }

                is IntentResult.Call -> {
                    val contact = contactHelper.findContact(result.recipient)
                    val target = contact?.phone ?: result.recipient
                    val recipientName = contact?.name?.uppercase() ?: result.recipient.uppercase()
                    speakAndConfirm("INITIATING VOICE LINK TO $recipientName.")
                    backgroundManager.makeCallWithSim(target, result.simIndex)
                    trackUsage("OTHER")
                }

                is IntentResult.BookCab -> {
                    speakAndConfirm("INITIALIZING CAB PROTOCOL. LAUNCHING ${result.provider} TO ${result.destination.uppercase()}.")
                    cabBookingManager.bookCab(result.provider, result.destination)
                    trackUsage("CAB")
                }

                is IntentResult.Query -> {
                    if (result.query.startsWith("OPEN_MAPS|")) {
                        val destination = result.query.substringAfter("|")
                        speakAndConfirm("SEARCHING MAPS FOR $destination.")
                        externalAppManager.launchAppWithSearch("MAPS", destination)
                        trackUsage("OTHER")
                    } else if (result.query.startsWith("OPEN_BROWSER|")) {
                        val searchQuery = result.query.substringAfter("|")
                        speakAndConfirm("SEARCHING BROWSER FOR $searchQuery.")
                        externalAppManager.launchAppWithSearch("BROWSER", searchQuery)
                        trackUsage("OTHER")
                    } else if (result.query.startsWith("OPEN_APP|")) {
                        val parts = result.query.split("|")
                        val app = parts[1]
                        val query = parts[2]
                        speakAndConfirm("SEARCHING $app FOR $query.")
                        externalAppManager.launchAppWithSearch(app, query)
                        trackUsage("OTHER")
                    }
                }

                is IntentResult.Unrecognized -> {
                    ttsManager.speak("NEURAL INPUT UNRECOGNIZED: $text")
                    _uiState.value = _uiState.value.copy(lastVoiceCommandResult = null)
                }
                else -> {
                    _uiState.value = _uiState.value.copy(lastVoiceCommandResult = null)
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
