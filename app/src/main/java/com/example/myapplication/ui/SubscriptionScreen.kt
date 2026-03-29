package com.example.myapplication.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.DeepBlack
import com.example.myapplication.ui.theme.ElectricCyan
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SubscriptionScreen(
    viewModel: SubscriptionViewModel,
    onLogout: () -> Unit
) {
    val state by viewModel.subscriptionState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.checkSubscriptionStatus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (val s = state) {
            is SubscriptionState.Loading -> {
                CircularProgressIndicator(color = ElectricCyan)
            }
            is SubscriptionState.Expired -> {
                SubscriptionStatusCard(
                    title = "Subscription Expired",
                    message = "Your trial or plan has ended. Upgrade now to keep using Kiwi AI.",
                    isExpired = true,
                    expiryDate = s.subscription.end_date,
                    onUpgrade = { viewModel.upgradePlan(30) }
                )
            }
            is SubscriptionState.Success -> {
                SubscriptionStatusCard(
                    title = "Active Plan",
                    message = "You have full access to all features.",
                    isExpired = false,
                    expiryDate = s.subscription.end_date,
                    onUpgrade = { viewModel.upgradePlan(30) }
                )
            }
            is SubscriptionState.Error -> {
                Text("Error: ${s.message}", color = Color.Red)
                Button(onClick = { viewModel.checkSubscriptionStatus() }) {
                    Text("Retry")
                }
            }
            else -> {}
        }

        Spacer(modifier = Modifier.height(32.dp))
        
        TextButton(onClick = onLogout) {
            Text("Logout", color = Color.Gray)
        }
    }
}

@Composable
fun SubscriptionStatusCard(
    title: String,
    message: String,
    isExpired: Boolean,
    expiryDate: String,
    onUpgrade: () -> Unit
) {
    val dateStr = try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        val date = inputFormat.parse(expiryDate)
        SimpleDateFormat("MMM dd, yyyy", Locale.US).format(date!!)
    } catch (e: Exception) {
        expiryDate
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, if (isExpired) Color.Red.copy(alpha = 0.5f) else ElectricCyan.copy(alpha = 0.5f), RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111111)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = if (isExpired) Icons.Rounded.Warning else Icons.Rounded.CheckCircle,
                contentDescription = null,
                tint = if (isExpired) Color.Red else ElectricCyan,
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = message,
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Valid until:", color = Color.Gray, fontSize = 12.sp)
                Text(dateStr, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onUpgrade,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (isExpired) "RENEW NOW" else "EXTEND PLAN", color = DeepBlack, fontWeight = FontWeight.Bold)
            }
        }
    }
}
