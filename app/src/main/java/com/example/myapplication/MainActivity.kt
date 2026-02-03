package com.example.myapplication

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.speech.RecognizerIntent
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import com.example.myapplication.auth.AuthScreen
import com.example.myapplication.auth.AuthState
import com.example.myapplication.auth.AuthViewModel
import com.example.myapplication.communication.WakeWordService
import com.example.myapplication.ui.AiAssistantScreen
import com.example.myapplication.ui.SchedulingViewModel
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.theme.DeepBlack
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: SchedulingViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()
    private var showAccessibilityDialog by mutableStateOf(false)

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.all { it }) {
            requestIgnoreBatteryOptimization()
            startWakeWordService()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        checkAndRequestPermissions()

        setContent {
            MyApplicationTheme {
                val authState by authViewModel.authState.collectAsState()

                // Pass token to SchedulingViewModel when authenticated
                LaunchedEffect(authState) {
                    if (authState is AuthState.Authenticated) {
                        viewModel.setToken((authState as AuthState.Authenticated).token)
                        
                        // Handle delayed voice command if existing
                        intent?.getStringExtra("VOICE_COMMAND")?.let { 
                            viewModel.handleVoiceCommand(it) 
                            intent.removeExtra("VOICE_COMMAND")
                        }
                    }
                }

                Surface(color = DeepBlack) {
                    if (authState is AuthState.Authenticated) {
                        AiAssistantScreen(
                            viewModel = viewModel,
                            onVoiceRequest = { 
                                if (!isAccessibilityServiceEnabled(this, com.example.myapplication.communication.WhatsAppAutomationService::class.java)) {
                                    showAccessibilityDialog = true
                                } else {
                                    viewModel.startNeuralListening()
                                }
                            },
                            onLogoutRequest = {
                                authViewModel.logout()
                            }
                        )
                    } else {
                        AuthScreen(viewModel = authViewModel)
                    }

                    if (showAccessibilityDialog) {
                        AccessibilityDisclosureDialog(
                            onDismiss = { showAccessibilityDialog = false },
                            onConfirm = {
                                showAccessibilityDialog = false
                                openAccessibilitySettings()
                            }
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun AccessibilityDisclosureDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Automation Permission") },
            text = { Text("APPLE AI needs the Accessibility Service to send messages automatically in the background. We do not collect or see your personal data.") },
            confirmButton = {
                TextButton(onClick = onConfirm) { Text("Enable") }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text("Not Now") }
            }
        )
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        val command = intent?.getStringExtra("VOICE_COMMAND")
        if (command != null) {
            viewModel.handleVoiceCommand(command)
        }
    }

    private fun startWakeWordService() {
        val serviceIntent = Intent(this, WakeWordService::class.java)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }

    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.WRITE_CALENDAR,
            Manifest.permission.READ_CALENDAR,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.SEND_SMS
        )
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        val missing = permissions.filter { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }
        if (missing.isNotEmpty()) permissionLauncher.launch(missing.toTypedArray())
        else {
            requestIgnoreBatteryOptimization()
            startWakeWordService()
        }
    }

    @SuppressLint("BatteryLife")
    private fun requestIgnoreBatteryOptimization() {
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
            try {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            } catch (e: Exception) {
                startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
            }
        }
    }

    private fun openAccessibilitySettings() {
        startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
    }

    private fun isAccessibilityServiceEnabled(context: Context, service: Class<*>): Boolean {
        val expected = android.content.ComponentName(context, service)
        val enabled = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: return false
        return enabled.contains(expected.flattenToString())
    }
}
