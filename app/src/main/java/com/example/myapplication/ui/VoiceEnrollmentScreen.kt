package com.example.myapplication.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.auth.AuthViewModel
import com.example.myapplication.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun VoiceEnrollmentScreen(viewModel: AuthViewModel, onComplete: () -> Unit) {
    var recordingProgress by remember { mutableFloatStateOf(0f) }
    var samplesCount by remember { mutableIntStateOf(0) }
    var isRecording by remember { mutableStateOf(false) }
    
    val phrases = listOf(
        "Authorize my neural link.",
        "Kiwi, authenticate my voice.",
        "Secure my personal workspace."
    )

    Box(modifier = Modifier.fillMaxSize().background(DeepBlack), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "VOICE BIOMETRIC ENROLLMENT",
                color = ElectricCyan,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
            
            Spacer(Modifier.height(48.dp))

            // Pulse Animation Box
            Box(contentAlignment = Alignment.Center) {
                Surface(
                    modifier = Modifier.size(160.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.05f),
                    border = androidx.compose.foundation.BorderStroke(2.dp, if (isRecording) ElectricCyan else Color.Gray)
                ) {
                    Icon(
                        imageVector = if (isRecording) Icons.Rounded.GraphicEq else Icons.Rounded.Mic,
                        contentDescription = null,
                        tint = if (isRecording) ElectricCyan else Color.Gray,
                        modifier = Modifier.size(64.dp).padding(40.dp)
                    )
                }
            }

            Spacer(Modifier.height(48.dp))

            if (samplesCount < phrases.size) {
                Text(
                    "Please say the following phrase clearly:",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "\"${phrases[samplesCount]}\"",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            } else {
                Text(
                    "Analyzing Vocal Patterns...",
                    color = ElectricCyan,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(48.dp))

            LinearProgressIndicator(
                progress = { recordingProgress },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = ElectricCyan,
                trackColor = Color.White.copy(alpha = 0.1f)
            )

            Spacer(Modifier.height(64.dp))

            if (samplesCount < phrases.size) {
                Button(
                    onClick = {
                        isRecording = true
                        // Simulate recording and processing
                        // In a real implementation, this would capture PCM data and generate MFCC vectors
                    },
                    enabled = !isRecording,
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = DeepBlack),
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(if (isRecording) "RECORDING..." else "HOLD TO RECORD", fontWeight = FontWeight.Black)
                }
            }
        }
    }

    // Simulation of Voiceprint Generation
    LaunchedEffect(isRecording) {
        if (isRecording) {
            var p = 0f
            while (p < 1f) {
                delay(50)
                p += 0.02f
                recordingProgress = p
            }
            isRecording = false
            recordingProgress = 0f
            samplesCount++
            
            if (samplesCount >= phrases.size) {
                delay(1500)
                // Mock generating a unique signature string
                val mockSignature = "vprint_sha256_${System.currentTimeMillis()}"
                viewModel.saveVoicePrint(mockSignature)
                onComplete()
            }
        }
    }
}
