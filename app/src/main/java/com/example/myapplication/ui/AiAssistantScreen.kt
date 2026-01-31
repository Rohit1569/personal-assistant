package com.example.myapplication.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Mic
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
fun AiAssistantScreen(viewModel: SchedulingViewModel, onVoiceRequest: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

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
            onVoiceRequest()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack)
    ) {
        // Futuristic background elements
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF0A0A0F), DeepBlack)
                    )
                )
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(100.dp))

            // Minimal Robotic Core
            Box(contentAlignment = Alignment.Center) {
                // Outer ring glow
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .border(1.dp, ElectricCyan.copy(alpha = 0.1f), CircleShape)
                )
                AiLivingCore(
                    isListening = false,
                    modifier = Modifier.size(200.dp)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Status Text
            Text(
                text = "APPLE CORE ONLINE",
                color = SoftNeonWhite.copy(alpha = 0.8f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // AI Thought Stream (Floating Terminal Panel)
            AnimatedVisibility(
                visible = uiState.lastVoiceCommandResult != null,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut()
            ) {
                uiState.lastVoiceCommandResult?.let { result ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 32.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color.White.copy(alpha = 0.03f))
                            .border(0.5.dp, ElectricCyan.copy(alpha = 0.2f))
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "> $result",
                            color = ElectricCyan,
                            fontSize = 16.sp,
                            fontFamily = FuturisticFontFamily,
                            lineHeight = 22.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Task Matrix (Log of recent actions)
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        "NEURAL LOG",
                        color = ElectricCyan.copy(alpha = 0.4f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                items(uiState.appointments) { appointment ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.02f))
                            .border(0.5.dp, Color.White.copy(alpha = 0.05f))
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = appointment.title.uppercase(),
                                    color = SoftNeonWhite,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "ACTION_TYPE: EXECUTED",
                                    color = ElectricCyan.copy(alpha = 0.5f),
                                    fontSize = 10.sp,
                                    letterSpacing = 1.sp
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(ElectricCyan, CircleShape)
                            )
                        }
                    }
                }
            }

            // Robotic Activation Interface
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 80.dp),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = {
                        val missing = permissions.filter {
                            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
                        }
                        if (missing.isNotEmpty()) {
                            permissionLauncher.launch(missing.toTypedArray())
                        } else {
                            onVoiceRequest()
                        }
                    },
                    modifier = Modifier
                        .size(80.dp)
                        .border(1.dp, ElectricCyan.copy(alpha = 0.3f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Mic,
                        contentDescription = "Activate",
                        tint = ElectricCyan,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}
