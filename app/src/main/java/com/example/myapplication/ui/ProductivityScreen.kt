package com.example.myapplication.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.models.Note
import com.example.myapplication.models.Task
import com.example.myapplication.ui.theme.*

@Composable
fun ProductivityScreen(viewModel: ProductivityViewModel) {
    val tasks by viewModel.tasks.collectAsState()
    val notes by viewModel.notes.collectAsState()
    var currentTab by remember { mutableStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(DeepBlack)) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
            Text(
                text = "PRODUCTIVITY_CORE",
                color = ElectricCyan,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(top = 40.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            TabRow(
                selectedTabIndex = currentTab,
                containerColor = Color.Transparent,
                contentColor = ElectricCyan,
                divider = {},
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[currentTab]),
                        color = ElectricCyan
                    )
                }
            ) {
                Tab(selected = currentTab == 0, onClick = { currentTab = 0 }) {
                    Text("TASKS", modifier = Modifier.padding(12.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Tab(selected = currentTab == 1, onClick = { currentTab = 1 }) {
                    Text("NOTES", modifier = Modifier.padding(12.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(modifier = Modifier.weight(1f)) {
                if (currentTab == 0) {
                    TaskList(tasks, onToggle = { viewModel.toggleTaskStatus(it) }, onDelete = { viewModel.deleteTask(it) })
                } else {
                    NoteList(notes, onDelete = { viewModel.deleteNote(it) })
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = ElectricCyan,
            contentColor = DeepBlack,
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp)
        ) {
            Icon(Icons.Rounded.Add, "Add")
        }

        if (showAddDialog) {
            if (currentTab == 0) {
                AddTaskDialog(onDismiss = { showAddDialog = false }) { title, desc, priority ->
                    viewModel.addTask(title, desc, priority, null)
                    showAddDialog = false
                }
            } else {
                AddNoteDialog(onDismiss = { showAddDialog = false }) { title, content ->
                    viewModel.addNote(title, content)
                    showAddDialog = false
                }
            }
        }
    }
}

@Composable
fun TaskList(tasks: List<Task>, onToggle: (Task) -> Unit, onDelete: (String) -> Unit) {
    if (tasks.isEmpty()) ProductivityEmptyState("No tasks found.")
    else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(tasks) { task ->
                Surface(
                    color = Color.White.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = task.status == "Completed",
                            onCheckedChange = { onToggle(task) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = ElectricCyan,
                                uncheckedColor = Color.Gray
                            )
                        )
                        Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                            Text(
                                text = task.title,
                                color = if (task.status == "Completed") Color.Gray else SoftNeonWhite,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                textDecoration = if (task.status == "Completed") TextDecoration.LineThrough else null
                            )
                            task.description?.let {
                                Text(it, color = Color.Gray, fontSize = 12.sp)
                            }
                        }
                        IconButton(onClick = { onDelete(task.id) }) {
                            Icon(Icons.Rounded.Delete, null, tint = Color(0xFFFF5252))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NoteList(notes: List<Note>, onDelete: (String) -> Unit) {
    if (notes.isEmpty()) ProductivityEmptyState("No notes found.")
    else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(notes) { note ->
                Surface(
                    color = Color.White.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f))
                ) {
                    Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(note.title, color = ElectricCyan, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            IconButton(onClick = { onDelete(note.id) }) {
                                Icon(Icons.Rounded.Delete, null, tint = Color(0xFFFF5252), modifier = Modifier.size(18.dp))
                            }
                        }
                        note.content?.let {
                            Text(it, color = SoftNeonWhite, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductivityEmptyState(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Icon(
                Icons.Rounded.TaskAlt, 
                null, 
                tint = ElectricCyan.copy(alpha = 0.4f), 
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(message, color = Color.Gray, fontSize = 14.sp, textAlign = TextAlign.Center)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(onDismiss: () -> Unit, onAdd: (String, String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("Medium") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("NEW_TASK", color = ElectricCyan, fontSize = 16.sp) },
        containerColor = DeepBlack,
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = title, 
                    onValueChange = { title = it }, 
                    label = { Text("Title") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, 
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = ElectricCyan,
                        unfocusedLabelColor = Color.Gray
                    )
                )
                OutlinedTextField(
                    value = desc, 
                    onValueChange = { desc = it }, 
                    label = { Text("Description") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, 
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = ElectricCyan,
                        unfocusedLabelColor = Color.Gray
                    )
                )
                Text("Priority", color = Color.Gray, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Low", "Medium", "High").forEach { p ->
                        FilterChip(
                            selected = priority == p,
                            onClick = { priority = p },
                            label = { Text(p) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ElectricCyan,
                                selectedLabelColor = DeepBlack,
                                labelColor = Color.Gray
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (title.isNotEmpty()) onAdd(title, desc, priority) }, 
                colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = DeepBlack)
            ) { Text("ADD") }
        },
        dismissButton = { 
            TextButton(onClick = onDismiss) { 
                Text("CANCEL", color = ElectricCyan) 
            } 
        }
    )
}

@Composable
fun AddNoteDialog(onDismiss: () -> Unit, onAdd: (String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("NEW_NOTE", color = ElectricCyan, fontSize = 16.sp) },
        containerColor = DeepBlack,
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = title, 
                    onValueChange = { title = it }, 
                    label = { Text("Title") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, 
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = ElectricCyan,
                        unfocusedLabelColor = Color.Gray
                    )
                )
                OutlinedTextField(
                    value = content, 
                    onValueChange = { content = it }, 
                    label = { Text("Content") }, 
                    modifier = Modifier.height(150.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, 
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = ElectricCyan,
                        unfocusedLabelColor = Color.Gray
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (title.isNotEmpty()) onAdd(title, content) }, 
                colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = DeepBlack)
            ) { Text("SAVE") }
        },
        dismissButton = { 
            TextButton(onClick = onDismiss) { 
                Text("CANCEL", color = ElectricCyan) 
            } 
        }
    )
}
