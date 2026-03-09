package com.example.myapplication.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.models.Note
import com.example.myapplication.models.Task
import kotlin.random.Random

// 2026 COSMIC PRO PALETTE
private val MatrixCyan = Color(0xFF00F2FF)
private val MatrixMagenta = Color(0xFFFF00E5)
private val MatrixPurple = Color(0xFF7000FF)

@Composable
fun ProductivityScreen(viewModel: ProductivityViewModel) {
    val tasks by viewModel.tasks.collectAsState()
    val notes by viewModel.notes.collectAsState()
    var currentTab by remember { mutableIntStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF020205))) {
        // High-Fidelity Cosmic Background
        CosmicBackground()

        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
            // Futuristic Greeting Header
            HeaderSection("Good Evening 👋")

            Spacer(modifier = Modifier.height(32.dp))

            // Capsule Style Navigation
            CapsuleTabRow(currentTab) { currentTab = it }

            Spacer(modifier = Modifier.height(24.dp))

            // Main Productivity Interface
            Box(modifier = Modifier.weight(1f)) {
                AnimatedContent(
                    targetState = currentTab,
                    transitionSpec = { fadeIn(tween(500)) togetherWith fadeOut(tween(500)) },
                    label = "content"
                ) { targetTab ->
                    if (targetTab == 0) {
                        if (tasks.isEmpty()) {
                            AllCaughtUpCard(onAdd = { showAddDialog = true })
                        } else {
                            TaskList(tasks, onToggle = { viewModel.toggleTaskStatus(it) }, onDelete = { viewModel.deleteTask(it) })
                        }
                    } else {
                        NoteList(notes, onDelete = { viewModel.deleteNote(it) })
                    }
                }
            }
        }

        // Action FAB positioned at Bottom Right
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            ActionFab(onClick = { showAddDialog = true })
        }
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

@Composable
fun HeaderSection(greeting: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 48.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(greeting, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
            Text("Your Productivity Hub", color = MatrixCyan, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }

        Box(contentAlignment = Alignment.Center) {
            Box(Modifier.size(50.dp).blur(20.dp).background(MatrixCyan.copy(alpha = 0.2f), CircleShape))
            Icon(Icons.Rounded.Psychology, null, tint = MatrixCyan, modifier = Modifier.size(42.dp))
        }
    }
}

@Composable
fun CapsuleTabRow(selected: Int, onSelect: (Int) -> Unit) {
    Surface(
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, MatrixPurple.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth().height(56.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            listOf("TASKS", "NOTES").forEachIndexed { index, title ->
                val isSelected = selected == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { onSelect(index) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        title,
                        color = if (isSelected) MatrixCyan else Color.White.copy(alpha = 0.6f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                    if (isSelected) {
                        Box(Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp).width(40.dp).height(2.dp).background(MatrixCyan))
                    }
                }
            }
        }
    }
}

@Composable
fun AllCaughtUpCard(onAdd: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
        color = Color.White.copy(alpha = 0.03f),
        shape = RoundedCornerShape(32.dp),
        border = BorderStroke(1.dp, Brush.linearGradient(listOf(MatrixMagenta, MatrixPurple, MatrixCyan)))
    ) {
        Column(modifier = Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(140.dp)) {
                Box(Modifier.size(90.dp).blur(35.dp).background(MatrixCyan.copy(alpha = 0.3f), CircleShape))
                Icon(Icons.Rounded.TaskAlt, null, tint = MatrixCyan, modifier = Modifier.size(90.dp))
            }

            Text(
                text = buildAnnotatedString {
                    append("You're all ")
                    withStyle(style = SpanStyle(color = MatrixCyan)) { append("caught up") }
                    append(" 🎉")
                },
                color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(32.dp))

            Surface(
                onClick = onAdd,
                color = Color.Transparent,
                shape = RoundedCornerShape(50),
                border = BorderStroke(2.dp, MatrixMagenta)
            ) {
                Text(
                    "Create Task",
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 48.dp, vertical = 14.dp),
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
fun ActionFab(onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(1500), RepeatMode.Reverse), label = "pulse"
    )

    Box(contentAlignment = Alignment.Center) {
        // Breathing pulse glow
        Box(
            Modifier
                .size(72.dp * pulse)
                .background(MatrixCyan.copy(alpha = 0.12f), CircleShape)
        )

        FloatingActionButton(
            onClick = onClick,
            containerColor = MatrixCyan,
            contentColor = Color.Black,
            modifier = Modifier.size(64.dp),
            shape = CircleShape,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 0.dp)
        ) {
            Icon(Icons.Rounded.Add, "Add", modifier = Modifier.size(36.dp))
        }
    }
}

@Composable
fun CosmicBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "stars")
    val stars = remember { List(60) { Offset(Random.nextFloat(), Random.nextFloat()) } }
    val animOffset by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(25000, easing = LinearEasing), RepeatMode.Reverse), label = "nebula"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            brush = Brush.radialGradient(listOf(MatrixCyan.copy(alpha = 0.12f), Color.Transparent)),
            radius = size.width,
            center = Offset(size.width * animOffset, size.height * (1 - animOffset))
        )
        stars.forEach { star ->
            drawCircle(Color.White.copy(alpha = 0.3f), radius = 1.dp.toPx(), center = Offset(star.x * size.width, star.y * size.height))
        }
    }
}

@Composable
fun TaskList(tasks: List<Task>, onToggle: (Task) -> Unit, onDelete: (String) -> Unit) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(bottom = 120.dp)) {
        items(tasks) { task ->
            Surface(
                color = Color.White.copy(alpha = 0.05f),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f))
            ) {
                Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = task.status == "Completed", onCheckedChange = { onToggle(task) }, colors = CheckboxDefaults.colors(checkedColor = MatrixCyan))
                    Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                        Text(task.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        task.description?.let { Text(it, color = Color.Gray, fontSize = 12.sp) }
                    }
                    IconButton(onClick = { onDelete(task.id) }) { Icon(Icons.Rounded.Delete, null, tint = Color(0xFFFF5252).copy(alpha = 0.7f)) }
                }
            }
        }
    }
}

@Composable
fun NoteList(notes: List<Note>, onDelete: (String) -> Unit) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(bottom = 120.dp)) {
        items(notes) { note ->
            Surface(
                color = Color.White.copy(alpha = 0.05f),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(note.title, color = MatrixCyan, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        IconButton(onClick = { onDelete(note.id) }) { Icon(Icons.Rounded.Delete, null, tint = Color(0xFFFF5252).copy(alpha = 0.7f), modifier = Modifier.size(18.dp)) }
                    }
                    note.content?.let { Text(it, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp)) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun AddTaskDialog(onDismiss: () -> Unit, onAdd: (String, String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = onDismiss, containerColor = Color(0xFF0A0A15),
        title = { Text("New Task", color = MatrixCyan, fontWeight = FontWeight.Black) },
        text = { Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MatrixCyan, focusedTextColor = Color.White, unfocusedTextColor = Color.White))
            OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Details") }, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MatrixCyan, focusedTextColor = Color.White, unfocusedTextColor = Color.White))
        }},
        confirmButton = { Button(onClick = { if (title.isNotEmpty()) onAdd(title, desc, "Medium") }, colors = ButtonDefaults.buttonColors(containerColor = MatrixCyan, contentColor = Color.Black)) { Text("Add") } }
    )
}

@Composable fun AddNoteDialog(onDismiss: () -> Unit, onAdd: (String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = onDismiss, containerColor = Color(0xFF0A0A15),
        title = { Text("New Note", color = MatrixCyan, fontWeight = FontWeight.Black) },
        text = { Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MatrixCyan, focusedTextColor = Color.White, unfocusedTextColor = Color.White))
            OutlinedTextField(value = content, onValueChange = { content = it }, label = { Text("Content") }, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MatrixCyan, focusedTextColor = Color.White, unfocusedTextColor = Color.White))
        }},
        confirmButton = { Button(onClick = { if (title.isNotEmpty()) onAdd(title, content) }, colors = ButtonDefaults.buttonColors(containerColor = MatrixCyan, contentColor = Color.Black)) { Text("Sync") } }
    )
}