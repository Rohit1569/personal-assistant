package com.example.myapplication.ui

import androidx.compose.animation.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.api.UserAdminInfo
import com.example.myapplication.api.UserFullDetails
import com.example.myapplication.ui.theme.*

@Composable
fun AdminScreen(viewModel: AdminViewModel) {
    val users by viewModel.users.collectAsState()
    val selectedUser by viewModel.selectedUserDetails.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize().background(DeepBlack)) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Spacer(modifier = Modifier.height(40.dp))
            
            Text(
                "SYSTEM ADMINISTRATION",
                color = ElectricCyan,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedUser == null) {
                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { 
                        searchQuery = it
                        viewModel.loadUsers(it)
                    },
                    placeholder = { Text("Search users by name or email...", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Rounded.Search, null, tint = ElectricCyan) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (isLoading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = ElectricCyan)

                UserList(users, onUserClick = { viewModel.loadUserDetails(it.id) })
            } else {
                UserDetailsTabularView(
                    details = selectedUser!!,
                    onBack = { viewModel.clearDetails() },
                    onToggleAccess = { viewModel.toggleUserAccess(it.id, it.is_active) }
                )
            }
        }
    }
}

@Composable
fun UserList(users: List<UserAdminInfo>, onUserClick: (UserAdminInfo) -> Unit) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(users) { user ->
            Surface(
                onClick = { onUserClick(user) },
                shape = RoundedCornerShape(12.dp),
                color = Color.White.copy(alpha = 0.05f),
                border = BorderStroke(0.5.dp, if (user.is_active) ElectricCyan.copy(alpha = 0.3f) else Color.Red.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(10.dp).background(if (user.is_active) Color.Green else Color.Red, CircleShape))
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(user.name, color = Color.White, fontWeight = FontWeight.Bold)
                        Text(user.email, color = Color.Gray, fontSize = 12.sp)
                    }
                    Text(user.role.uppercase(), color = ElectricCyan, fontSize = 10.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
fun UserDetailsTabularView(details: UserFullDetails, onBack: () -> Unit, onToggleAccess: (UserAdminInfo) -> Unit) {
    val scrollState = rememberScrollState()
    Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, null, tint = ElectricCyan) }
            Text("USER DATABASE INSPECTOR", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
        }

        Spacer(Modifier.height(16.dp))

        // Access Control Card
        Surface(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            color = if (details.profile.is_active) Color.White.copy(alpha = 0.02f) else Color.Red.copy(alpha = 0.05f),
            border = BorderStroke(1.dp, if (details.profile.is_active) ElectricCyan.copy(alpha = 0.2f) else Color.Red.copy(alpha = 0.4f))
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(details.profile.name.uppercase(), color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                Text(details.profile.email, color = Color.Gray, fontSize = 13.sp)
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { onToggleAccess(details.profile) },
                    colors = ButtonDefaults.buttonColors(containerColor = if (details.profile.is_active) Color.Red else Color.Green),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(if (details.profile.is_active) "DISABLE USER ACCESS" else "RESTORE USER ACCESS", fontWeight = FontWeight.Bold)
                }
            }
        }

        // TABULAR DATA SECTIONS
        DataTable("FINANCIAL LOGS", listOf("CATEGORY", "AMOUNT", "NOTE"), details.expenses)
        DataTable("PRODUCTIVITY: TASKS", listOf("TITLE", "STATUS", "PRIORITY"), details.tasks)
        DataTable("KNOWLEDGE: NOTES", listOf("TITLE", "CONTENT"), details.notes)

        if (details.fitness != null) {
            DataTable("HEALTH BIOMETRICS", listOf("METRIC", "VALUE"), listOf(details.fitness))
        }

        Spacer(Modifier.height(120.dp))
    }
}

@Composable
fun DataTable(title: String, headers: List<String>, data: List<Any>) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Text(title, color = ElectricCyan, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
        Spacer(Modifier.height(8.dp))
        
        HorizontalDataTable(headers, data)
    }
}

@Composable
fun HorizontalDataTable(headers: List<String>, items: List<Any>) {
    val scrollState = rememberScrollState()
    
    Surface(
        modifier = Modifier.fillMaxWidth().horizontalScroll(scrollState),
        color = Color.White.copy(alpha = 0.02f),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column {
            // Header Row
            Row(modifier = Modifier.background(Color.White.copy(alpha = 0.05f)).padding(12.dp)) {
                for (header in headers) {
                    Text(
                        text = header,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        modifier = Modifier.width(120.dp)
                    )
                }
            }
            
            if (items.isEmpty()) {
                Text("No data entries found.", color = Color.DarkGray, fontSize = 12.sp, modifier = Modifier.padding(16.dp))
            }

            // Data Rows
            for (item in items) {
                val map = item as? Map<*, *>
                if (map != null) {
                    Row(modifier = Modifier.border(0.2.dp, Color.White.copy(alpha = 0.05f)).padding(12.dp)) {
                        for (header in headers) {
                            val value = when(header) {
                                "CATEGORY" -> map["category"]
                                "AMOUNT" -> "$${map["amount"]}"
                                "NOTE" -> map["note"]
                                "TITLE" -> map["title"]
                                "STATUS" -> map["status"]
                                "PRIORITY" -> map["priority"]
                                "CONTENT" -> map["content"]
                                "METRIC" -> "Full Profile"
                                "VALUE" -> "See details"
                                else -> "-"
                            }
                            Text(
                                text = value?.toString() ?: "-",
                                color = Color.White,
                                fontSize = 12.sp,
                                modifier = Modifier.width(120.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
