package com.example.myapplication.ui

import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.api.IncrementRequest
import com.example.myapplication.api.UsageApi
import com.example.myapplication.communication.*
import com.example.myapplication.data.FinanceRepository
import com.example.myapplication.data.ProductivityRepository
import com.example.myapplication.data.SchedulingRepository
import com.example.myapplication.models.*
import com.example.myapplication.plugin.IntentResult
import com.example.myapplication.voice.VoiceIntentProcessor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class SchedulingViewModel @Inject constructor(
    private val repository: SchedulingRepository,
    private val financeRepository: FinanceRepository,
    private val productivityRepository: ProductivityRepository,
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

    private val _selectedLocale = MutableStateFlow(Locale.getDefault())
    val selectedLocale: StateFlow<Locale> = _selectedLocale.asStateFlow()

    private var userToken: String? = null
    private var userId: String? = null

    init {
        observeAppointments()
    }

    fun setLanguage(locale: Locale) {
        _selectedLocale.value = locale
        ttsManager.setLanguage(locale)
    }

    fun setToken(token: String) {
        userToken = "Bearer $token"
        try {
            val json = JSONObject(String(Base64.decode(token.split(".")[1], Base64.DEFAULT)))
            userId = json.getString("id")
        } catch (e: Exception) {
            userId = token.hashCode().toString()
        }
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
            locale = _selectedLocale.value,
            onState = { listening -> _isListening.value = listening },
            onResultFound = { command -> handleVoiceCommand(command) }
        )
    }

    fun handleVoiceCommand(text: String) {
        viewModelScope.launch {
            _isListening.value = false
            _uiState.value = _uiState.value.copy(lastVoiceCommandResult = "ANALYZING NEURAL INPUT...")
            val result = voiceProcessor.parse(text)
            
            when (result) {
                is IntentResult.AddTask -> {
                    val uid = userId ?: return@launch
                    productivityRepository.createTask(Task(userId = uid, title = result.title, description = result.description, priority = result.priority))
                    speakAndConfirm("TASK RECOGNIZED. ADDED ${result.title.uppercase()} TO YOUR PLAN.")
                }

                is IntentResult.AddNote -> {
                    val uid = userId ?: return@launch
                    productivityRepository.createNote(Note(userId = uid, title = result.title, content = result.content))
                    speakAndConfirm("NOTE CAPTURED. I HAVE SAVED THAT TO YOUR IDEATION ENGINE.")
                }

                is IntentResult.AddExpense -> {
                    val uid = userId ?: return@launch
                    financeRepository.addExpense(Expense(id = UUID.randomUUID().toString(), userId = uid, category = result.category, amount = result.amount, date = getIsoDate(), note = result.note))
                    speakAndConfirm("ACKNOWLEDGED. ADDED EXPENSE OF $${result.amount} FOR ${result.category.uppercase()}.")
                }

                is IntentResult.SendMessage -> {
                    val contact = contactHelper.findContact(result.recipient)
                    val target = if (result.app.toString().contains("MAIL")) contact?.email else contact?.phone
                    val finalTarget = target ?: result.recipient
                    speakAndConfirm("INITIALIZING ${result.app} PROTOCOL TO ${result.recipient.uppercase()}.")
                    communicationManager.sendMessage(result.app, finalTarget, result.message)
                }

                is IntentResult.Query -> {
                    val query = result.query
                    when {
                        query == "OPEN_FINANCE_OVERVIEW" -> speakAndConfirm("PREPARING FINANCIAL ANALYTICS.")
                        query.startsWith("OPEN_MAPS|") -> externalAppManager.launchAppWithSearch("MAPS", query.substringAfter("|"))
                        query.startsWith("OPEN_APP|YOUTUBE|") -> externalAppManager.launchAppWithSearch("YOUTUBE", query.substringAfter("YOUTUBE|"))
                        query.startsWith("OPEN_APP|AMAZON|") -> externalAppManager.launchAppWithSearch("AMAZON", query.substringAfter("AMAZON|"))
                        query.startsWith("OPEN_BROWSER|") -> externalAppManager.launchAppWithSearch("BROWSER", query.substringAfter("|"))
                    }
                }

                is IntentResult.BookCab -> {
                    speakAndConfirm("REQUESTING ${result.provider} TO ${result.destination.uppercase()}...")
                    cabBookingManager.bookCab(result.provider, result.destination)
                }

                is IntentResult.Call -> {
                    speakAndConfirm("INITIATING VOICE CHANNEL TO ${result.recipient.uppercase()}...")
                    communicationManager.initiateCall(result.recipient)
                }

                is IntentResult.CalendarInsert -> {
                    val formattedDate = SimpleDateFormat("MMMM dd 'at' HH:mm", Locale.getDefault()).format(Date(result.startTime))
                    speakAndConfirm("SCHEDULING ${result.title.uppercase()} ON $formattedDate.")
                    backgroundManager.insertCalendarEventBackground(result.title, result.startTime, result.durationMinutes, result.location)
                }

                is IntentResult.CalendarQuery -> {
                    speakAndConfirm("SCANNING YOUR CALENDAR...")
                    val eventsResult = backgroundManager.queryCalendarEvents(result.startTime, result.endTime)
                    eventsResult.onSuccess { events ->
                        if (events.isEmpty()) {
                            speakAndConfirm("I FOUND NO MEETINGS FOR THAT DATE.")
                        } else {
                            val eventTitles = events.joinToString(", ") { it.title }
                            speakAndConfirm("I FOUND ${events.size} MEETINGS: $eventTitles.")
                        }
                    }
                }

                // NEW: Handle Calendar Deletion
                is IntentResult.CalendarDelete -> {
                    speakAndConfirm("ATTEMPTING TO REMOVE '${result.title.uppercase()}' FROM CALENDAR...")
                    val deleteResult = backgroundManager.deleteCalendarEvent(result.title)
                    deleteResult.onSuccess { count ->
                        if (count > 0) speakAndConfirm("SUCCESS. I REMOVED $count MATCHING EVENTS.")
                        else speakAndConfirm("I COULD NOT FIND ANY MATCHING EVENTS TO DELETE.")
                    }
                }

                is IntentResult.Unrecognized -> {
                    speakAndConfirm("NEURAL INPUT UNRECOGNIZED: $text")
                }
                else -> { _uiState.value = _uiState.value.copy(lastVoiceCommandResult = null) }
            }
        }
    }

    private fun getIsoDate(): String {
        return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(Date())
    }

    private fun speakAndConfirm(text: String) {
        _uiState.value = _uiState.value.copy(lastVoiceCommandResult = text)
        ttsManager.speak(text)
    }
}
