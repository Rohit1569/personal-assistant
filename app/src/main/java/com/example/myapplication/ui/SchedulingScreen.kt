package com.example.myapplication.ui

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.myapplication.models.Appointment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchedulingScreen(
    viewModel: SchedulingViewModel,
    onVoiceRequest: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("VOICE PLUGIN") }
            )
        },
        floatingActionButton = {
            // MOCK MODE: 
            // TAP = Real Voice Recognition
            // LONG PRESS = Simulated "Book a Haircut"
            FloatingActionButton(
                onClick = onVoiceRequest,
                modifier = Modifier.pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = { 
                            viewModel.handleVoiceCommand("book a haircut")
                        },
                        onTap = { onVoiceRequest() }
                    )
                }
            ) {
                Text("🎤")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Text(
                text = "Tip: Long-press 🎤 to mock 'Book Haircut'",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            if (uiState.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            uiState.lastVoiceCommandResult?.let { result ->
                Card(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Text(text = result, modifier = Modifier.padding(16.dp))
                }
            }

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(uiState.appointments) { appointment ->
                    AppointmentItem(appointment)
                }
            }
        }
    }
}

@Composable
fun AppointmentItem(appointment: Appointment) {
    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = appointment.title, style = MaterialTheme.typography.titleMedium)
            Text(text = "Time: ${appointment.startTime}", style = MaterialTheme.typography.bodySmall)
            Text(text = "Type: ${appointment.type.displayName}", style = MaterialTheme.typography.bodySmall)
        }
    }
}
