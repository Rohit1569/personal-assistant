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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.example.myapplication.ui.components.AiLivingCore
import com.example.myapplication.ui.theme.*
import java.util.Locale
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

    var showLanguageSelector by remember { mutableStateOf(false) }

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
        CosmicBackground(isListening || isProcessing)

        // Top Bar: Logout & Language Selector
        Row(
            modifier = Modifier.align(Alignment.TopEnd).padding(top = 48.dp, end = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = { showLanguageSelector = true },
                modifier = Modifier.size(40.dp).background(Color.White.copy(alpha = 0.05f), CircleShape).border(0.5.dp, Color.White.copy(alpha = 0.1f), CircleShape)
            ) {
                Icon(Icons.Rounded.Language, "Language", tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(20.dp))
            }
            IconButton(
                onClick = onLogoutRequest,
                modifier = Modifier.size(40.dp).background(Color.White.copy(alpha = 0.05f), CircleShape).border(0.5.dp, Color.White.copy(alpha = 0.1f), CircleShape)
            ) {
                Icon(Icons.Rounded.Logout, "Logout", tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(20.dp))
            }
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(110.dp))
            Text(
                text = if (isListening) "Listening for commands" else "Tap mic to start",
                color = ElectricCyan.copy(alpha = 0.6f),
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.weight(1f))

            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(320.dp)) {
                AiLivingCore(isListening = isListening, modifier = Modifier.fillMaxSize())
            }

            Text("How can I assist you today?", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Normal, letterSpacing = 0.5.sp, modifier = Modifier.padding(top = 24.dp))

            Spacer(modifier = Modifier.weight(1.2f))

            Box(modifier = Modifier.fillMaxWidth().padding(bottom = 60.dp), contentAlignment = Alignment.Center) {
                val infiniteTransition = rememberInfiniteTransition(label = "micGlow")
                val glowAlpha by infiniteTransition.animateFloat(initialValue = 0.3f, targetValue = 0.7f, animationSpec = infiniteRepeatable(tween(1500), RepeatMode.Reverse), label = "alpha")

                IconButton(
                    onClick = {
                        val missing = permissions.filter { ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED }
                        if (missing.isNotEmpty()) permissionLauncher.launch(missing.toTypedArray()) else {
                            if (isListening) viewModel.handleVoiceCommand("") else onVoiceRequest()
                        }
                    },
                    modifier = Modifier.size(92.dp).drawBehind { drawCircle(brush = Brush.radialGradient(colors = listOf(ElectricCyan.copy(alpha = glowAlpha), Color.Transparent)), radius = size.minDimension * 0.8f) }.border(2.5.dp, ElectricCyan, CircleShape).background(Color.Black.copy(alpha = 0.4f), CircleShape)
                ) {
                    Icon(imageVector = if (isListening) Icons.Rounded.Stop else Icons.Rounded.Mic, contentDescription = "Mic", tint = ElectricCyan, modifier = Modifier.size(42.dp))
                }
            }
        }
    }

    if (showLanguageSelector) {
        LanguageSelectionDialog(
            onDismiss = { showLanguageSelector = false },
            onSelect = { locale -> 
                viewModel.setLanguage(locale)
                showLanguageSelector = false
            }
        )
    }
}

@Composable
fun LanguageSelectionDialog(onDismiss: () -> Unit, onSelect: (Locale) -> Unit) {
    val languages = mapOf(
        "🇺🇸 English (US)" to Locale.US,
        "🇬🇧 English (UK)" to Locale.UK,
        "🇮🇳 English (India)" to Locale.forLanguageTag("en-IN"),
        "🇮🇳 हिन्दी (Hindi)" to Locale.forLanguageTag("hi-IN"),
        "🇪🇸 Español (Spanish)" to Locale.forLanguageTag("es-ES"),
        "🇮🇳 தமிழ் (Tamil)" to Locale.forLanguageTag("ta-IN"),
        "🇮🇳 తెలుగు (Telugu)" to Locale.forLanguageTag("te-IN"),
        "🇮🇳 ಕನ್ನಡ (Kannada)" to Locale.forLanguageTag("kn-IN"),
        "🇮🇳 മലയാളം (Malayalam)" to Locale.forLanguageTag("ml-IN"),
        "🇮🇳 বাংলা (Bengali)" to Locale.forLanguageTag("bn-IN"),
        "🇮🇳 मराठी (Marathi)" to Locale.forLanguageTag("mr-IN"),
        "🇮🇳 ગુજરાતી (Gujarati)" to Locale.forLanguageTag("gu-IN")
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(16.dp), color = Color(0xFF1A1A1A)) {
            LazyColumn(modifier = Modifier.padding(16.dp).heightIn(max = 450.dp)) {
                item {
                    Text("Select Language", color = ElectricCyan, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(8.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                }
                languages.forEach { (name, locale) ->
                    item {
                        TextButton(onClick = { onSelect(locale) }, modifier = Modifier.fillMaxWidth()) {
                            Text(name, color = Color.White, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CosmicBackground(isActive: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "cosmic")
    val rotation by infiniteTransition.animateFloat(initialValue = 0f, targetValue = 360f, animationSpec = infiniteRepeatable(tween(80000, easing = LinearEasing)), label = "rotation")
    val pulse by infiniteTransition.animateFloat(initialValue = 0.3f, targetValue = 0.7f, animationSpec = infiniteRepeatable(tween(5000), RepeatMode.Reverse), label = "pulse")

    Canvas(modifier = Modifier.fillMaxSize().graphicsLayer(rotationZ = rotation)) {
        drawCircle(brush = Brush.radialGradient(colors = listOf(Color(0xFF003566).copy(alpha = 0.25f * pulse), Color.Transparent), center = Offset(size.width * 0.3f, size.height * 0.4f), radius = size.width * 0.9f), radius = size.width * 0.9f, center = Offset(size.width * 0.3f, size.height * 0.4f))
        drawCircle(brush = Brush.radialGradient(colors = listOf(Color(0xFF1B4965).copy(alpha = 0.2f * pulse), Color.Transparent), center = Offset(size.width * 0.7f, size.height * 0.6f), radius = size.width * 0.7f), radius = size.width * 0.7f, center = Offset(size.width * 0.7f, size.height * 0.6f))
    }

    Box(modifier = Modifier.fillMaxSize()) {
        repeat(40) {
            val startX = remember { Random.nextFloat() }
            val startY = remember { Random.nextFloat() }
            val sizeStar = remember { Random.nextFloat() * 1.5f + 0.5f }
            Box(modifier = Modifier.offset(x = (startX * 400).dp, y = (startY * 800).dp).size(sizeStar.dp).background(Color.White.copy(alpha = Random.nextFloat() * 0.4f + 0.1f), CircleShape))
        }
    }
}
