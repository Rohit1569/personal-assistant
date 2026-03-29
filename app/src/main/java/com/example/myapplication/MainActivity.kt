package com.example.myapplication

import android.Manifest
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
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

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: SchedulingViewModel by viewModels()
    private val financeViewModel: FinanceViewModel by viewModels()
    private val productivityViewModel: ProductivityViewModel by viewModels()
    private val fitnessViewModel: FitnessViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()
    private val subscriptionViewModel: SubscriptionViewModel by viewModels()
    
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
            checkAccessibilityStatus()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            MyApplicationTheme {
                val authState by authViewModel.authState.collectAsState()
                val subState by subscriptionViewModel.subscriptionState.collectAsState()
                val navController = rememberNavController()
                
                var isSetupComplete by remember { mutableStateOf(tokenManager.isSetupComplete()) }
                var needsVoiceEnrollment by remember { mutableStateOf(false) }

                LaunchedEffect(authState) {
                    if (authState is AuthState.Authenticated) {
                        val token = (authState as AuthState.Authenticated).token
                        viewModel.setToken(token)
                        subscriptionViewModel.checkSubscriptionStatus()
                        try {
                            val json = JSONObject(String(Base64.decode(token.split(".")[1], Base64.DEFAULT)))
                            val userId = json.getString("id")
                            financeViewModel.initForUser(userId)
                            productivityViewModel.setUserId(userId)
                        } catch (e: Exception) { }
                    }
                    
                    if (authState is AuthState.VoiceEnrollmentRequired) {
                        needsVoiceEnrollment = true
                    }
                }

                Surface(color = DeepBlack) {
                    when {
                        !isSetupComplete -> {
                            OnboardingScreen(onComplete = {
                                tokenManager.setSetupComplete()
                                isSetupComplete = true
                            })
                        }
                        
                        needsVoiceEnrollment -> {
                            VoiceEnrollmentScreen(viewModel = authViewModel, onComplete = {
                                needsVoiceEnrollment = false
                            })
                        }

                        authState !is AuthState.Authenticated -> {
                            AuthScreen(viewModel = authViewModel)
                        }

                        subState is SubscriptionState.Expired -> {
                            SubscriptionScreen(
                                viewModel = subscriptionViewModel,
                                onLogout = { authViewModel.logout() }
                            )
                        }
                        
                        else -> {
                            LaunchedEffect(Unit) {
                                checkAndRequestPermissions()
                            }

                            Scaffold(
                                bottomBar = {
                                    NavigationBar(containerColor = Color.Black, modifier = Modifier.border(0.5.dp, Color.White.copy(alpha = 0.2f))) {
                                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                                        val currentRoute = navBackStackEntry?.destination?.route
                                        val items = listOf(
                                            Triple("assistant", "BOT", Icons.Rounded.Assistant),
                                            Triple("finance", "MONEY", Icons.Rounded.AccountBalanceWallet),
                                            Triple("productivity", "PLAN", Icons.Rounded.TaskAlt),
                                            Triple("fitness", "BODY", Icons.Rounded.FitnessCenter),
                                            Triple("subscription", "PASS", Icons.Rounded.Star)
                                        )
                                        items.forEach { (route, label, icon) ->
                                            NavigationBarItem(
                                                selected = currentRoute == route,
                                                onClick = { navController.navigate(route) },
                                                icon = { Icon(icon, label, modifier = Modifier.size(24.dp)) },
                                                label = { Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold) },
                                                colors = NavigationBarItemDefaults.colors(selectedIconColor = ElectricCyan, indicatorColor = ElectricCyan.copy(alpha = 0.15f), unselectedIconColor = Color.LightGray, unselectedTextColor = Color.LightGray)
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
                                    composable("subscription") { SubscriptionScreen(viewModel = subscriptionViewModel, onLogout = { authViewModel.logout() }) }
                                }
                            }

                            if (showAccessibilityPrompt) {
                                AccessibilityDialog(
                                    onDismiss = { showAccessibilityPrompt = false }, 
                                    onConfirm = {
                                        showAccessibilityPrompt = false
                                        startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                                    },
                                    onTroubleshoot = {
                                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                        intent.data = Uri.fromParts("package", packageName, null)
                                        startActivity(intent)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun startNeuralMic() {
        viewModel.startNeuralListening()
    }

    @Composable
    fun AccessibilityDialog(onDismiss: () -> Unit, onConfirm: () -> Unit, onTroubleshoot: () -> Unit) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Enable Automation", fontWeight = FontWeight.Black) },
            text = { 
                Column {
                    Text("To send messages automatically, please find 'Kiwi Voice Automation' in the settings and turn it ON.")
                    Spacer(Modifier.height(12.dp))
                    Text("Note: If the setting is greyed out, use the 'FIX BLOCKED' button below first.", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                }
            },
            confirmButton = { 
                Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = DeepBlack)) { 
                    Text("OPEN SETTINGS") 
                } 
            },
            dismissButton = {
                TextButton(onClick = onTroubleshoot) {
                    Text("FIX BLOCKED SETTING", color = ElectricCyan)
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

    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf(Manifest.permission.WRITE_CALENDAR, Manifest.permission.READ_CALENDAR, Manifest.permission.READ_CONTACTS, Manifest.permission.RECORD_AUDIO, Manifest.permission.CALL_PHONE, Manifest.permission.SEND_SMS, Manifest.permission.CAMERA)
        val missing = permissions.filter { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }
        if (missing.isNotEmpty()) permissionLauncher.launch(missing.toTypedArray()) else {
            checkAccessibilityStatus()
        }
    }
}
