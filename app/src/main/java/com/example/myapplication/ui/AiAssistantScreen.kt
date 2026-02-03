package com.example.myapplication.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.myapplication.ui.components.AiLivingCore
import com.example.myapplication.ui.theme.*

@Composable
fun AiAssistantScreen(
    viewModel: SchedulingViewModel, 
    onVoiceRequest: () -> Unit,
    onLogoutRequest: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isListening by viewModel.isListening.collectAsState()
    val context = LocalContext.current

    // State Logic: Listening -> Processing -> Ready
    val isProcessing = uiState.lastVoiceCommandResult == "ANALYZING NEURAL INPUT..." || 
                      uiState.lastVoiceCommandResult?.startsWith("INITIALIZING") == true ||
                      uiState.lastVoiceCommandResult?.startsWith("SENDING") == true ||
                      uiState.lastVoiceCommandResult?.startsWith("SEARCHING") == true ||
                      uiState.lastVoiceCommandResult?.startsWith("INITIATING") == true

    val statusText = when {
        isListening -> "LISTENING..."
        isProcessing -> "PROCESSING..."
        else -> "READY"
    }

    val statusColor = when {
        isListening -> ElectricCyan
        isProcessing -> Color(0xFFFFD700) // Gold for processing
        else -> ElectricCyan.copy(alpha = 0.5f)
    }

    val permissions = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.READ_CALENDAR,
        Manifest.permission.WRITE_CALENDAR,
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.CALL_PHONE,
        Manifest.permission.SEND_SMS
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.values.all { it }) {
            onVoiceRequest() // FIXED: Ensure accessibility check happens
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack)
    ) {
        // Background Glow
        if (isListening || isProcessing) {
            val infiniteTransition = rememberInfiniteTransition(label = "bgGlow")
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.05f,
                targetValue = 0.15f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200),
                    repeatMode = RepeatMode.Reverse
                ), label = "alpha"
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.radialGradient(listOf(statusColor.copy(alpha = alpha), Color.Transparent)))
            )
        }

        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp)
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status Badge
            Surface(
                shape = RoundedCornerShape(50),
                color = statusColor.copy(alpha = 0.1f),
                border = BorderStroke(1.dp, statusColor.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(6.dp).background(statusColor, CircleShape))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = statusText,
                        color = statusColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                }
            }

            IconButton(
                onClick = onLogoutRequest,
                modifier = Modifier.size(40.dp).background(Color.White.copy(alpha = 0.05f), CircleShape)
            ) {
                Icon(Icons.Rounded.Logout, contentDescription = "Logout", tint = ElectricCyan, modifier = Modifier.size(18.dp))
            }
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(120.dp))

            // AI Living Core
            Box(contentAlignment = Alignment.Center) {
                AiLivingCore(
                    isListening = isListening,
                    modifier = Modifier.size(240.dp)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Neural Log / Result Panel
            AnimatedVisibility(
                visible = uiState.lastVoiceCommandResult != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.02f))
                        .border(0.5.dp, statusColor.copy(alpha = 0.3f))
                        .padding(20.dp)
                ) {
                    Text(
                        text = "> ${uiState.lastVoiceCommandResult}",
                        color = statusColor,
                        fontSize = 15.sp,
                        fontFamily = FuturisticFontFamily,
                        lineHeight = 22.sp
                    )
                }
            }

            // Neural Task Log
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text("ACTIVITY_LOG", color = Color.Gray, fontSize = 10.sp, letterSpacing = 2.sp)
                }
                items(uiState.appointments) { appointment ->
                    Box(
                        modifier = Modifier.fillMaxWidth().background(Color.White.copy(alpha = 0.01f)).padding(12.dp)
                    ) {
                        Text(appointment.title.uppercase(), color = SoftNeonWhite, fontSize = 12.sp)
                    }
                }
            }

            // Mic Activation
            Box(modifier = Modifier.fillMaxWidth().padding(bottom = 60.dp), contentAlignment = Alignment.Center) {
                IconButton(
                    onClick = {
                        val missing = permissions.filter { ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED }
                        if (missing.isNotEmpty()) permissionLauncher.launch(missing.toTypedArray())
                        else {
                            if (isListening) viewModel.handleVoiceCommand("") // Stop listening
                            else onVoiceRequest() // FIXED: Use the lambda from MainActivity to trigger accessibility check
                        }
                    },
                    modifier = Modifier
                        .size(80.dp)
                        .background(if (isListening) ElectricCyan.copy(alpha = 0.2f) else Color.Transparent, CircleShape)
                        .border(2.dp, ElectricCyan.copy(alpha = if (isListening) 1f else 0.3f), CircleShape)
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Rounded.Stop else Icons.Rounded.Mic,
                        contentDescription = "Mic",
                        tint = ElectricCyan,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}
