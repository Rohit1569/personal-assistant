package com.example.myapplication.ui

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.myapplication.ui.components.AiLivingCore
import com.example.myapplication.ui.theme.*
import kotlin.random.Random

@Composable
fun AiAssistantScreen(
    viewModel: SchedulingViewModel, 
    onVoiceRequest: () -> Unit,
    onLogoutRequest: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isListening by viewModel.isListening.collectAsState()
    val context = LocalContext.current

    val isProcessing = uiState.lastVoiceCommandResult == "ANALYZING NEURAL INPUT..." || 
                      uiState.lastVoiceCommandResult?.startsWith("INITIALIZING") == true

    val statusText = "AI READY"
    val statusColor = ElectricCyan

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
        } else {
            Toast.makeText(context, "Permissions required for AI Assistant.", Toast.LENGTH_LONG).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000814))
    ) {
        // Animated Cosmic Background
        CosmicBackground(isListening || isProcessing)

        // Logout Button - Top Right
        IconButton(
            onClick = onLogoutRequest,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 48.dp, end = 20.dp)
                .size(40.dp)
                .background(Color.White.copy(alpha = 0.05f), CircleShape)
                .border(0.5.dp, Color.White.copy(alpha = 0.1f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Rounded.Logout,
                contentDescription = "Logout",
                tint = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Status Badge
            Spacer(modifier = Modifier.height(56.dp))
            Surface(
                shape = RoundedCornerShape(50),
                color = Color.Black.copy(alpha = 0.3f),
                border = BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.4f)),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(statusColor, CircleShape)
                            .drawBehind {
                                drawCircle(statusColor.copy(alpha = 0.4f), radius = size.minDimension * 0.8f)
                            }
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = statusText,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                }
            }
            
            Text(
                text = if (isListening) "Listening for commands" else "Tap mic to start",
                color = ElectricCyan.copy(alpha = 0.6f),
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 12.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            // Central Animated Orb
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(320.dp)
            ) {
                AiLivingCore(
                    isListening = isListening,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Text(
                text = "How can I assist you today?",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Normal,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(top = 24.dp)
            )

            Spacer(modifier = Modifier.weight(1.2f))

            // Large Animated Mic Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 60.dp),
                contentAlignment = Alignment.Center
            ) {
                val infiniteTransition = rememberInfiniteTransition(label = "micGlow")
                val glowAlpha by infiniteTransition.animateFloat(
                    initialValue = 0.3f,
                    targetValue = 0.7f,
                    animationSpec = infiniteRepeatable(tween(1500), RepeatMode.Reverse), label = "alpha"
                )

                IconButton(
                    onClick = {
                        val missing = permissions.filter { ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED }
                        if (missing.isNotEmpty()) {
                            permissionLauncher.launch(missing.toTypedArray())
                        } else {
                            if (isListening) viewModel.handleVoiceCommand("")
                            else onVoiceRequest() 
                        }
                    },
                    modifier = Modifier
                        .size(92.dp)
                        .drawBehind {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(ElectricCyan.copy(alpha = glowAlpha), Color.Transparent)
                                ),
                                radius = size.minDimension * 0.8f
                            )
                        }
                        .border(2.5.dp, ElectricCyan, CircleShape)
                        .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Rounded.Stop else Icons.Rounded.Mic,
                        contentDescription = "Mic",
                        tint = ElectricCyan,
                        modifier = Modifier.size(42.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CosmicBackground(isActive: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "cosmic")
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(80000, easing = LinearEasing)), label = "rotation"
    )

    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(tween(5000), RepeatMode.Reverse), label = "pulse"
    )

    Canvas(modifier = Modifier.fillMaxSize().graphicsLayer(rotationZ = rotation)) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF003566).copy(alpha = 0.25f * pulse), Color.Transparent),
                center = Offset(size.width * 0.3f, size.height * 0.4f),
                radius = size.width * 0.9f
            ),
            radius = size.width * 0.9f,
            center = Offset(size.width * 0.3f, size.height * 0.4f)
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF1B4965).copy(alpha = 0.2f * pulse), Color.Transparent),
                center = Offset(size.width * 0.7f, size.height * 0.6f),
                radius = size.width * 0.7f
            ),
            radius = size.width * 0.7f,
            center = Offset(size.width * 0.7f, size.height * 0.6f)
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        repeat(40) {
            val startX = remember { Random.nextFloat() }
            val startY = remember { Random.nextFloat() }
            val sizeStar = remember { Random.nextFloat() * 1.5f + 0.5f }
            
            Box(
                modifier = Modifier
                    .offset(
                        x = (startX * 400).dp,
                        y = (startY * 800).dp
                    )
                    .size(sizeStar.dp)
                    .background(Color.White.copy(alpha = Random.nextFloat() * 0.4f + 0.1f), CircleShape)
            )
        }
    }
}
