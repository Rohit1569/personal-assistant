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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.myapplication.models.Note
import com.example.myapplication.models.Task
import com.example.myapplication.ui.theme.*

@Composable
fun ProductivityScreen(viewModel: ProductivityViewModel) {
    val tasks by viewModel.tasks.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val ideas by viewModel.ideas.collectAsState() // Independent local state
    
    var activeTab by remember { mutableStateOf("TASKS") }
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = DeepBlack,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = ElectricCyan,
                contentColor = DeepBlack,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = when(activeTab) {
                        "TASKS" -> Icons.Rounded.AddTask
                        "NOTES" -> Icons.Rounded.NoteAdd
                        else -> Icons.Rounded.Lightbulb
                    },
                    contentDescription = "Add Item"
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Column(modifier = Modifier.fillMaxSize()) {
                Spacer(modifier = Modifier.height(20.dp))
                
                // Tab Selector
                Row(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                        .padding(4.dp)
                ) {
                    TabItem("TASKS", activeTab == "TASKS", Modifier.weight(1f)) { activeTab = "TASKS" }
                    TabItem("NOTES", activeTab == "NOTES", Modifier.weight(1f)) { activeTab = "NOTES" }
                    TabItem("IDEAS", activeTab == "IDEAS", Modifier.weight(1f)) { activeTab = "IDEAS" }
                }

                Spacer(modifier = Modifier.height(24.dp))

                when (activeTab) {
                    "TASKS" -> TaskList(
                        tasks, 
                        onDelete = { viewModel.deleteTask(it) },
                        onToggleStatus = { viewModel.toggleTaskStatus(it) }
                    )
                    "NOTES" -> NoteList(notes, onDelete = { viewModel.deleteNote(it) })
                    "IDEAS" -> IdeaList(ideas, onDelete = { viewModel.deleteIdea(it) })
                }
            }
        }
    }

    if (showAddDialog) {
        AddProductivityItemDialog(
            type = activeTab,
            onDismiss = { showAddDialog = false },
            onConfirm = { title, content ->
                when (activeTab) {
                    "TASKS" -> viewModel.addTask(title, content, "Medium", null)
                    "NOTES" -> viewModel.addNote(title, content)
                    "IDEAS" -> viewModel.addIdea(title, content) // Local only
                }
                showAddDialog = false
            }
        )
    }
}

@Composable
fun AddProductivityItemDialog(type: String, onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF1A1A1A),
            border = BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "ADD NEW ${type}",
                    color = ElectricCyan,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    letterSpacing = 2.sp
                )
                Spacer(Modifier.height(20.dp))
                
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text(if (type == "TASKS") "Description" else "Content") },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
                Spacer(Modifier.height(24.dp))
                
                Button(
                    onClick = { if (title.isNotBlank()) onConfirm(title, content) },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("CONFIRM", color = DeepBlack, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun TabItem(label: String, isSelected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) ElectricCyan else Color.Transparent,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                label,
                color = if (isSelected) DeepBlack else Color.Gray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun TaskList(tasks: List<Task>, onDelete: (String) -> Unit, onToggleStatus: (Task) -> Unit) {
    LazyColumn(
        contentPadding = PaddingValues(start = 24.dp, top = 8.dp, end = 24.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(tasks) { task ->
            val isCompleted = task.status == "Completed"
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.03f),
                border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Toggable Check Indicator
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(if (isCompleted) ElectricCyan else Color.Transparent)
                            .border(1.5.dp, ElectricCyan, CircleShape)
                            .clickable { onToggleStatus(task) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCompleted) {
                            Icon(Icons.Rounded.Check, null, tint = DeepBlack, modifier = Modifier.size(18.dp))
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = task.title ?: "Untitled Task", 
                            color = if (isCompleted) Color.Gray else Color.White, 
                            fontWeight = FontWeight.Bold, 
                            fontSize = 15.sp,
                            textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None
                        )
                        if (!task.description.isNullOrEmpty()) {
                            Text(
                                text = task.description ?: "", 
                                color = Color.Gray.copy(alpha = 0.7f), 
                                fontSize = 12.sp,
                                textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None
                            )
                        }
                    }
                    IconButton(onClick = { task.id?.let { onDelete(it) } }) {
                        Icon(Icons.Rounded.Delete, "Delete", tint = Color.Red.copy(alpha = 0.6f), modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun NoteList(notes: List<Note>, onDelete: (String) -> Unit) {
    LazyColumn(
        contentPadding = PaddingValues(start = 24.dp, top = 8.dp, end = 24.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(notes) { note ->
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.03f),
                border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(note.title ?: "Untitled", color = ElectricCyan, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.weight(1f))
                        IconButton(onClick = { note.id?.let { onDelete(it) } }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Rounded.Delete, "Delete", tint = Color.Red.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(note.content ?: "", color = Color.White, fontSize = 14.sp, lineHeight = 20.sp)
                }
            }
        }
    }
}

@Composable
fun IdeaList(ideas: List<Idea>, onDelete: (String) -> Unit) {
    LazyColumn(
        contentPadding = PaddingValues(start = 24.dp, top = 8.dp, end = 24.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(ideas) { idea ->
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF0A0A0A),
                border = BorderStroke(1.dp, Brush.linearGradient(listOf(ElectricCyan.copy(alpha = 0.5f), Color.Transparent)))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Lightbulb, null, tint = Color(0xFFFFD54F), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(idea.title, color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp, modifier = Modifier.weight(1f))
                        IconButton(onClick = { onDelete(idea.id) }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Rounded.Delete, "Delete", tint = Color.Red.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(idea.content, color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp, lineHeight = 22.sp)
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Button(
                        onClick = { /* AI Execution Trigger */ },
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan.copy(alpha = 0.1f)),
                        border = BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.AutoAwesome, null, tint = ElectricCyan, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("HELP ME EXECUTE THIS IDEA", color = ElectricCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
