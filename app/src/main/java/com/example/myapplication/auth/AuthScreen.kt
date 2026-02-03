package com.example.myapplication.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.*

@Composable
fun AuthScreen(viewModel: AuthViewModel) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var isLogin by remember { mutableStateOf(true) }
    var isForgotPasswordMode by remember { mutableStateOf(false) }
    
    val state by viewModel.authState.collectAsState()

    // Handle screen transitions based on state
    LaunchedEffect(state) {
        if (state is AuthState.Idle && !isLogin) {
            isLogin = true
            isForgotPasswordMode = false
        }
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
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = when {
                    state is AuthState.OtpSent -> "Verify Signup"
                    state is AuthState.PasswordResetOtpSent -> "Reset Password"
                    isForgotPasswordMode -> "Forgot Password"
                    isLogin -> "Welcome Back"
                    else -> "Join the Future"
                },
                color = SoftNeonWhite,
                fontSize = 24.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            if (state is AuthState.OtpSent || state is AuthState.PasswordResetOtpSent) {
                Text(
                    text = "Enter the 6-digit code sent to $email",
                    color = SoftNeonWhite.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                OutlinedTextField(
                    value = otp,
                    onValueChange = { if (it.length <= 6) otp = it },
                    label = { Text("OTP Code", color = ElectricCyan.copy(alpha = 0.5f)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = SoftNeonWhite, unfocusedTextColor = SoftNeonWhite,
                        focusedBorderColor = ElectricCyan, unfocusedBorderColor = GlassWhite
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                
                if (state is AuthState.PasswordResetOtpSent) {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("New Password", color = ElectricCyan.copy(alpha = 0.5f)) },
                        visualTransformation = PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = SoftNeonWhite, unfocusedTextColor = SoftNeonWhite,
                            focusedBorderColor = ElectricCyan, unfocusedBorderColor = GlassWhite
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                if (isForgotPasswordMode) {
                    Text(
                        text = "Enter your email to receive a reset code",
                        color = SoftNeonWhite.copy(alpha = 0.7f),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                if (!isLogin && !isForgotPasswordMode) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name", color = ElectricCyan.copy(alpha = 0.5f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = SoftNeonWhite, unfocusedTextColor = SoftNeonWhite,
                            focusedBorderColor = ElectricCyan, unfocusedBorderColor = GlassWhite
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email", color = ElectricCyan.copy(alpha = 0.5f)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = SoftNeonWhite, unfocusedTextColor = SoftNeonWhite,
                        focusedBorderColor = ElectricCyan, unfocusedBorderColor = GlassWhite
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (!isForgotPasswordMode) {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password", color = ElectricCyan.copy(alpha = 0.5f)) },
                        visualTransformation = PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = SoftNeonWhite, unfocusedTextColor = SoftNeonWhite,
                            focusedBorderColor = ElectricCyan, unfocusedBorderColor = GlassWhite
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
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
                            else -> viewModel.signUp(name, email, password)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DeepPurple),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text(
                        when {
                            state is AuthState.OtpSent -> "VERIFY SIGNUP"
                            state is AuthState.PasswordResetOtpSent -> "UPDATE PASSWORD"
                            isForgotPasswordMode -> "SEND RESET CODE"
                            isLogin -> "LOGIN"
                            else -> "SIGN UP"
                        }
                    )
                }
            }

            if (state !is AuthState.OtpSent && state !is AuthState.PasswordResetOtpSent) {
                if (isLogin && !isForgotPasswordMode) {
                    TextButton(onClick = { isForgotPasswordMode = true }) {
                        Text("Forgot Password?", color = ElectricCyan)
                    }
                }
                
                TextButton(onClick = { 
                    if (isForgotPasswordMode) {
                        isForgotPasswordMode = false
                    } else {
                        isLogin = !isLogin 
                    }
                }) {
                    Text(
                        text = when {
                            isForgotPasswordMode -> "Back to Login"
                            isLogin -> "Create an account"
                            else -> "Already have an account? Login"
                        },
                        color = ElectricCyan
                    )
                }
            } else {
                TextButton(onClick = { viewModel.logout() }) {
                    Text("Cancel", color = ElectricCyan)
                }
            }

            if (state is AuthState.Error) {
                Text((state as AuthState.Error).message, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
            }
            if (state is AuthState.PasswordResetSuccess) {
                Text("Password updated! Please login.", color = ElectricCyan, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}
