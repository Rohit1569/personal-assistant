package com.example.myapplication.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.*

@Composable
fun AuthScreen(viewModel: AuthViewModel) {
    var name by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var cellPhone by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    
    var isLogin by remember { mutableStateOf(true) }
    var isForgotPasswordMode by remember { mutableStateOf(false) }
    
    val state by viewModel.authState.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(state) {
        if (state is AuthState.PasswordResetSuccess) {
            isLogin = true
            isForgotPasswordMode = false
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(DeepBlack), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(GlassWhite)
                .border(1.dp, NeonCyanEdge, RoundedCornerShape(24.dp))
                .padding(24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = when {
                    state is AuthState.OtpSent -> "Verify Identity"
                    state is AuthState.PasswordResetOtpSent -> "Update Security"
                    isForgotPasswordMode -> "Reset Access"
                    isLogin -> "Welcome Back"
                    else -> "Create Account"
                },
                color = SoftNeonWhite,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            if (state is AuthState.OtpSent || state is AuthState.PasswordResetOtpSent) {
                Text("Verification code sent to email.", color = SoftNeonWhite.copy(alpha = 0.7f), fontSize = 12.sp)
                Spacer(Modifier.height(16.dp))
                AuthTextField(otp, { if (it.length <= 6) otp = it }, "Enter OTP")
                
                if (state is AuthState.PasswordResetOtpSent) {
                    Spacer(Modifier.height(12.dp))
                    AuthTextField(password, { password = it }, "New Password", isPassword = true)
                }
            } else {
                if (!isLogin && !isForgotPasswordMode) {
                    AuthTextField(name, { name = it }, "Username")
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AuthTextField(firstName, { firstName = it }, "First Name", Modifier.weight(1f))
                        AuthTextField(lastName, { lastName = it }, "Last Name", Modifier.weight(1f))
                    }
                    AuthTextField(address, { address = it }, "Address")
                    AuthTextField(cellPhone, { cellPhone = it }, "Cell Phone")
                    AuthTextField(reason, { reason = it }, "Why this app?", isLongText = true)
                    Spacer(Modifier.height(8.dp))
                }

                AuthTextField(email, { email = it }, "Email Address")
                
                if (!isForgotPasswordMode) {
                    Spacer(Modifier.height(12.dp))
                    AuthTextField(password, { password = it }, "Password", isPassword = true)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (state is AuthState.Loading) {
                CircularProgressIndicator(color = ElectricCyan)
            } else {
                Button(
                    onClick = {
                        when {
                            state is AuthState.OtpSent -> viewModel.verifyOtp(otp)
                            state is AuthState.PasswordResetOtpSent -> viewModel.resetPassword(otp, password)
                            isForgotPasswordMode -> viewModel.forgotPassword(email)
                            isLogin -> viewModel.login(email, password)
                            else -> viewModel.signup(
                                name = name,
                                email = email, 
                                password = password, 
                                firstName = firstName,
                                lastName = lastName,
                                address = address,
                                cellPhone = cellPhone,
                                reason = reason
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DeepPurple),
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (isLogin) "LOGIN" else if (isForgotPasswordMode) "RESET" else "JOIN NEURAL CORE")
                }
            }

            // Forgot Password Option (only in login mode)
            if (isLogin && !isForgotPasswordMode && state !is AuthState.OtpSent) {
                TextButton(onClick = { isForgotPasswordMode = true }) {
                    Text("Forgot Password?", color = ElectricCyan, fontSize = 11.sp)
                }
            }

            // Switch between Login / Signup
            TextButton(onClick = { 
                if (isForgotPasswordMode) isForgotPasswordMode = false else isLogin = !isLogin 
            }) {
                Text(
                    text = if (isForgotPasswordMode) "Back to Login" else if (isLogin) "New user? Sign Up" else "Have account? Login",
                    color = ElectricCyan,
                    fontSize = 12.sp
                )
            }

            if (state is AuthState.Error) {
                Text((state as AuthState.Error).message, color = Color.Red, fontSize = 11.sp, modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}

@Composable
fun AuthTextField(
    value: String, 
    onValueChange: (String) -> Unit, 
    label: String, 
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    isLongText: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 12.sp) },
        visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        modifier = modifier.fillMaxWidth().then(if (isLongText) Modifier.height(100.dp) else Modifier),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White, unfocusedTextColor = Color.White,
            focusedBorderColor = ElectricCyan, unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
            focusedLabelColor = ElectricCyan, unfocusedLabelColor = Color.Gray
        ),
        shape = RoundedCornerShape(12.dp)
    )
}
