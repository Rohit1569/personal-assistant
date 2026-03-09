package com.example.myapplication.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    var step by remember { mutableIntStateOf(0) }
    
    val featureList = listOf(
        "Voice to Text",
        "Email Creation/Automation",
        "WhatsApp Message Automation",
        "SMS Message Automation",
        "Integrated Gemini AI / Google AI",
        "Task Capture Automation",
        "Notes Automation",
        "Ideation Automation",
        "Finance Overview",
        "Health and Fitness Overview"
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
            transitionSpec = { fadeIn(tween(800)) togetherWith fadeOut(tween(800)) },
            label = "step"
        ) { currentStep ->
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                when (currentStep) {
                    0 -> WelcomeStep()
                    1 -> FeatureListStep(featureList)
                    2 -> ValuePropositionStep()
                }
            }
        }

        // Action Button
        Button(
            onClick = {
                if (step < 2) step++ else onComplete()
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
                if (step < 2) "CONTINUE" else "GET STARTED",
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun WelcomeStep() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center) {
            Box(Modifier.size(140.dp).blur(50.dp).background(MatrixCyan.copy(alpha = 0.2f), CircleShape))
            Icon(Icons.Rounded.Psychology, null, tint = MatrixCyan, modifier = Modifier.size(100.dp))
        }
        Spacer(Modifier.height(48.dp))
        Text(
            "NEURAL LINK INITIALIZED",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "Welcome to the next generation of personal productivity. Your AI-powered core is ready to assist.",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}

@Composable
fun FeatureListStep(features: List<String>) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "FEATURES & BENEFITS",
            color = MatrixCyan,
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 3.sp
        )
        Spacer(Modifier.height(32.dp))
        
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White.copy(alpha = 0.03f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
            modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)
        ) {
            LazyColumn(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(features) { feature ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.CheckCircle, null, tint = MatrixCyan, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(16.dp))
                        Text(feature, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
fun ValuePropositionStep() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center) {
            Box(Modifier.size(120.dp).blur(40.dp).background(Color(0xFFFFD54F).copy(alpha = 0.15f), CircleShape))
            Icon(Icons.Rounded.Timer, null, tint = Color(0xFFFFD54F), modifier = Modifier.size(80.dp))
        }
        Spacer(Modifier.height(40.dp))
        Text(
            "30% - 40% TIME SAVED",
            color = Color(0xFFFFD54F),
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        Text(
            "Analysis and research show this application can save you up to 40% of your daily time.",
            color = Color.White,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            lineHeight = 26.sp
        )
        Spacer(Modifier.height(24.dp))
        Text(
            "Enhance your life so you can spend more time with your Family, Friends and on your Passions.",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}

private val MatrixCyan = Color(0xFF00F2FF)
