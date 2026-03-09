package com.example.myapplication

import android.Manifest
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.text.TextUtils
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.*
import com.example.myapplication.auth.AuthScreen
import com.example.myapplication.auth.AuthState
import com.example.myapplication.auth.AuthViewModel
import com.example.myapplication.auth.TokenManager
import com.example.myapplication.communication.WakeWordService
import com.example.myapplication.communication.WhatsAppAutomationService
import com.example.myapplication.ui.*
import com.example.myapplication.ui.theme.*
import com.example.myapplication.voice.BillScanner
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import org.json.JSONObject
import android.util.Base64
import android.widget.Toast

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: SchedulingViewModel by viewModels()
    private val financeViewModel: FinanceViewModel by viewModels()
    private val productivityViewModel: ProductivityViewModel by viewModels()
    private val fitnessViewModel: FitnessViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()
    
    @Inject lateinit var tokenManager: TokenManager
    private var showAccessibilityPrompt by mutableStateOf(false)

    @Inject lateinit var billScanner: BillScanner
    private var tempImageUri: Uri? = null

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && tempImageUri != null) { processImage(tempImageUri!!) }
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) { processImage(uri) }
    }

    private fun processImage(uri: Uri) {
        lifecycleScope.launch {
            val text = billScanner.scanBill(this@MainActivity, uri)
            text?.let { financeViewModel.processExtractedText(it) }
        }
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.all { it }) {
            // WakeWordService disabled to prevent automatic activation
            // startWakeWordService() 
            checkAccessibilityStatus()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        checkAndRequestPermissions()

        setContent {
            MyApplicationTheme {
                val authState by authViewModel.authState.collectAsState()
                val navController = rememberNavController()
                var isSetupComplete by remember { mutableStateOf(tokenManager.isSetupComplete()) }

                LaunchedEffect(authState) {
                    if (authState is AuthState.Authenticated) {
                        val token = (authState as AuthState.Authenticated).token
                        viewModel.setToken(token)
                        try {
                            val json = JSONObject(String(Base64.decode(token.split(".")[1], Base64.DEFAULT)))
                            val userId = json.getString("id")
                            financeViewModel.initForUser(userId)
                            productivityViewModel.setUserId(userId)
                        } catch (e: Exception) { }
                    }
                }

                Surface(color = DeepBlack) {
                    if (!isSetupComplete) {
                        OnboardingScreen(onComplete = {
                            tokenManager.setSetupComplete()
                            isSetupComplete = true
                        })
                    } else if (authState is AuthState.Authenticated) {
                        Scaffold(
                            bottomBar = {
                                NavigationBar(containerColor = Color.Black, modifier = Modifier.border(0.5.dp, Color.White.copy(alpha = 0.2f))) {
                                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                                    val currentRoute = navBackStackEntry?.destination?.route
                                    
                                    val items = listOf(
                                        Triple("assistant", "BOT", Icons.Rounded.Assistant),
                                        Triple("finance", "MONEY", Icons.Rounded.AccountBalanceWallet),
                                        Triple("productivity", "PLAN", Icons.Rounded.TaskAlt),
                                        Triple("fitness", "BODY", Icons.Rounded.FitnessCenter)
                                    )
                                    
                                    items.forEach { (route, label, icon) ->
                                        NavigationBarItem(
                                            selected = currentRoute == route,
                                            onClick = { navController.navigate(route) },
                                            icon = { Icon(icon, label, modifier = Modifier.size(24.dp)) },
                                            label = { Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold) },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = ElectricCyan,
                                                indicatorColor = ElectricCyan.copy(alpha = 0.15f),
                                                unselectedIconColor = Color.LightGray,
                                                unselectedTextColor = Color.LightGray
                                            )
                                        )
                                    }
                                }
                            }
                        ) { innerPadding ->
                            NavHost(navController, startDestination = "assistant", modifier = Modifier.padding(innerPadding)) {
                                composable("assistant") { AiAssistantScreen(viewModel = viewModel, onVoiceRequest = { startNeuralMic() }, onLogoutRequest = { authViewModel.logout() }) }
                                composable("finance") { FinanceDashboardScreen(viewModel = financeViewModel, onScanRequest = { launchCamera() }, onGalleryRequest = { launchGallery() }, onVoiceRequest = { startNeuralMic() }) }
                                composable("productivity") { ProductivityScreen(viewModel = productivityViewModel) }
                                composable("fitness") { FitnessScreen(viewModel = fitnessViewModel) }
                            }
                        }

                        if (showAccessibilityPrompt) {
                            AccessibilityDialog(
                                onDismiss = { showAccessibilityPrompt = false },
                                onConfirm = {
                                    showAccessibilityPrompt = false
                                    startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                                }
                            )
                        }
                    } else { AuthScreen(viewModel = authViewModel) }
                }
            }
        }
    }

    private fun startNeuralMic() {
        // Foreground mic activation on button click
        viewModel.startNeuralListening()
    }

    @Composable
    fun AccessibilityDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Neural Automation Required", fontWeight = FontWeight.Black) },
            text = { Text("To send messages and automate tasks, Kiwi needs the Accessibility service enabled. Please find 'Kiwi Voice Automation' in the list.") },
            confirmButton = {
                Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = DeepBlack)) {
                    Text("OPEN SETTINGS")
                }
            },
            containerColor = Color(0xFF1A1A1A),
            titleContentColor = Color.White,
            textContentColor = Color.White.copy(alpha = 0.8f)
        )
    }

    private fun checkAccessibilityStatus() {
        if (!isAccessibilityServiceEnabled(this, WhatsAppAutomationService::class.java)) {
            showAccessibilityPrompt = true
        }
    }

    private fun isAccessibilityServiceEnabled(context: Context, service: Class<*>): Boolean {
        val expectedComponentName = ComponentName(context, service).flattenToString()
        val enabledServices = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: return false
        val colonSplitter = TextUtils.SimpleStringSplitter(':')
        colonSplitter.setString(enabledServices)
        while (colonSplitter.hasNext()) {
            val componentName = colonSplitter.next()
            if (componentName.equals(expectedComponentName, ignoreCase = true)) return true
        }
        return false
    }

    private fun launchCamera() {
        val file = File(cacheDir, "temp_bill.jpg")
        tempImageUri = FileProvider.getUriForFile(this, "com.example.myapplication.provider", file)
        cameraLauncher.launch(tempImageUri!!)
    }

    private fun launchGallery() {
        galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun startWakeWordService() {
        // WakeWordService is no longer started automatically to prevent unwanted background listening
        // val serviceIntent = Intent(this, WakeWordService::class.java)
        // if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) startForegroundService(serviceIntent) else startService(serviceIntent)
    }

    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf(Manifest.permission.WRITE_CALENDAR, Manifest.permission.READ_CALENDAR, Manifest.permission.READ_CONTACTS, Manifest.permission.RECORD_AUDIO, Manifest.permission.CALL_PHONE, Manifest.permission.SEND_SMS, Manifest.permission.CAMERA)
        val missing = permissions.filter { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }
        if (missing.isNotEmpty()) permissionLauncher.launch(missing.toTypedArray()) else {
            // WakeWordService disabled to prevent automatic activation
            // startWakeWordService() 
            checkAccessibilityStatus()
        }
    }
}
