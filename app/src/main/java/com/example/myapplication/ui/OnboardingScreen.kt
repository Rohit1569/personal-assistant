package com.example.myapplication.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    var step by remember { mutableIntStateOf(0) }
    val steps = listOf(
        OnboardingStep(
            "Initializing Neural Core",
            "Kiwi is setting up your secure local environment. Please wait while we calibrate your assistant.",
            Icons.Rounded.Psychology,
            MatrixCyan
        ),
        OnboardingStep(
            "Voice Commands",
            "Simply click the microphone icon to command Kiwi. Try saying 'Set a reminder' or 'Add an expense'.",
            Icons.Rounded.Mic,
            MatrixCyan
        ),
        OnboardingStep(
            "Seamless Automation",
            "Kiwi can send messages and manage your schedule automatically. Grant permissions to enable the full neural link.",
            Icons.Rounded.AutoAwesome,
            MatrixCyan
        )
    )

    Box(modifier = Modifier.fillMaxSize().background(DeepBlack)) {
        // Background Glow
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(MatrixCyan.copy(alpha = 0.05f), Color.Transparent),
                        radius = 1000f
                    )
                )
        )

        AnimatedContent(
            targetState = step,
            transitionSpec = { fadeIn(tween(1000)) togetherWith fadeOut(tween(1000)) },
            label = "step"
        ) { currentStep ->
            val data = steps[currentStep]
            Column(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Central Glowing Icon
                Box(contentAlignment = Alignment.Center) {
                    Box(Modifier.size(120.dp).blur(40.dp).background(data.color.copy(alpha = 0.3f), CircleShape))
                    Icon(data.icon, null, tint = data.color, modifier = Modifier.size(80.dp))
                }

                Spacer(Modifier.height(48.dp))

                Text(
                    data.title.uppercase(),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    data.description,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                Spacer(Modifier.height(64.dp))

                // Progress Dots
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    steps.forEachIndexed { index, _ ->
                        Box(
                            Modifier
                                .size(if (index == currentStep) 24.dp else 8.dp, 8.dp)
                                .clip(CircleShape)
                                .background(if (index == currentStep) MatrixCyan else Color.White.copy(alpha = 0.2f))
                        )
                    }
                }
            }
        }

        // Action Button
        Button(
            onClick = {
                if (step < steps.size - 1) step++ else onComplete()
            },
            colors = ButtonDefaults.buttonColors(containerColor = MatrixCyan, contentColor = DeepBlack),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 64.dp)
                .fillMaxWidth(0.7f)
                .height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                if (step < steps.size - 1) "NEXT" else "GET STARTED",
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        }
    }
}

data class OnboardingStep(val title: String, val description: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val color: Color)

private val MatrixCyan = Color(0xFF00F2FF)
