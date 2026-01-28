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
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLogin by remember { mutableStateOf(true) }
    val state by viewModel.authState.collectAsState()

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
                text = if (isLogin) "Welcome Back" else "Join the Future",
                color = SoftNeonWhite,
                fontSize = 24.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email", color = ElectricCyan.copy(alpha = 0.5f)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = SoftNeonWhite,
                    unfocusedTextColor = SoftNeonWhite,
                    focusedBorderColor = ElectricCyan,
                    unfocusedBorderColor = GlassWhite
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password", color = ElectricCyan.copy(alpha = 0.5f)) },
                visualTransformation = PasswordVisualTransformation(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = SoftNeonWhite,
                    unfocusedTextColor = SoftNeonWhite,
                    focusedBorderColor = ElectricCyan,
                    unfocusedBorderColor = GlassWhite
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (state is AuthState.Loading) {
                CircularProgressIndicator(color = ElectricCyan)
            } else {
                Button(
                    onClick = {
                        if (isLogin) viewModel.login(email, password)
                        else viewModel.signUp(email, password)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DeepPurple),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text(if (isLogin) "LOGIN" else "SIGN UP")
                }
            }

            TextButton(onClick = { isLogin = !isLogin }) {
                Text(
                    text = if (isLogin) "Create an account" else "Already have an account? Login",
                    color = ElectricCyan
                )
            }

            if (state is AuthState.Error) {
                Text((state as AuthState.Error).message, color = Color.Red, fontSize = 12.sp)
            }
        }
    }
}
