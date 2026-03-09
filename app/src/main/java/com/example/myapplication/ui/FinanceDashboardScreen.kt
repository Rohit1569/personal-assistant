package com.example.myapplication.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.example.myapplication.models.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*

// 2026 NEURAL PRO PALETTE
private val MatrixCyan = Color(0xFF00F2FF)
private val MatrixPurple = Color(0xFF7000FF)
private val MatrixMagenta = Color(0xFFFF00E5)
private val MatrixCoral = Color(0xFFFF4D4D)
private val DeepObsidian = Color(0xFF050505)
private val ChartColors = listOf(MatrixCyan, MatrixPurple, MatrixMagenta, MatrixCoral, Color(0xFFFDCB6E))

@Composable
fun FinanceDashboardScreen(
    viewModel: FinanceViewModel,
    onScanRequest: () -> Unit,
    onGalleryRequest: () -> Unit,
    onVoiceRequest: () -> Unit
) {
    val expenses by viewModel.expenses.collectAsState()
    val summaries by viewModel.summaries.collectAsState()
    val incomes by viewModel.incomes.collectAsState()
    val monthlyReport by viewModel.monthlyReport.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()

    var currentTab by remember { mutableIntStateOf(0) }
    var showScanOptions by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(false) }
    var entryTypeToAdd by remember { mutableStateOf<String?>(null) }
    var showMonthPicker by remember { mutableStateOf(false) }

    Scaffold(containerColor = Color.Transparent) { padding ->
        Box(modifier = Modifier.fillMaxSize().background(DeepObsidian)) {
            MeshGradientBackground()

            Column(modifier = Modifier.fillMaxSize().padding(top = padding.calculateTopPadding()).padding(horizontal = 24.dp)) {
                // Interactive Header
                HeaderSection(selectedMonth) { showMonthPicker = true }
                
                monthlyReport?.let { HeroGlassCard(it) }
                TabSection(currentTab) { currentTab = it }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Box(modifier = Modifier.weight(1f)) {
                    AnimatedContent(targetState = currentTab, label = "tabs") { target ->
                        when (target) {
                            0 -> InsightsContent(summaries)
                            1 -> HistoryContent(expenses)
                            2 -> IncomeContent(incomes)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(140.dp))
            }

            Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = padding.calculateBottomPadding() + 16.dp)) {
                ActionHub(
                    isExpanded = isExpanded,
                    onToggle = { isExpanded = !isExpanded },
                    onVoice = onVoiceRequest,
                    onScan = { showScanOptions = true },
                    onExpense = { entryTypeToAdd = "Expense"; isExpanded = false },
                    onIncome = { entryTypeToAdd = "Income"; isExpanded = false }
                )
            }
        }
    }

    if (showMonthPicker) {
        MonthPickerMinimal(
            currentMonth = selectedMonth,
            onMonthSelected = { viewModel.setSelectedMonth(it); showMonthPicker = false },
            onDismiss = { showMonthPicker = false }
        )
    }

    if (showScanOptions) {
        ScanSourceDialog(onDismiss = { showScanOptions = false }, onCamera = onScanRequest, onGallery = onGalleryRequest)
    }
    
    when (entryTypeToAdd) {
        "Expense" -> ManualEntryDialog("Log Expense", "Details", onDismiss = { entryTypeToAdd = null }) { n, c, v ->
            viewModel.addManualExpense(n, c, v.toDoubleOrNull() ?: 0.0); entryTypeToAdd = null
        }
        "Income" -> ManualEntryDialog("Log Income", "Source", onDismiss = { entryTypeToAdd = null }) { s, _, v ->
            viewModel.addIncome(s, v.toDoubleOrNull() ?: 0.0); entryTypeToAdd = null
        }
    }
}

@Composable
fun HeroGlassCard(report: MonthlyReport) {
    Box(
        modifier = Modifier.padding(vertical = 16.dp).fillMaxWidth().height(200.dp).clip(RoundedCornerShape(32.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(1.dp, Brush.linearGradient(listOf(MatrixCyan.copy(alpha = 0.4f), Color.Transparent)), RoundedCornerShape(32.dp))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("Net Capital Surplus", color = MatrixCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))
            Box(Modifier.width(100.dp).height(2.dp).background(MatrixCyan))
            Spacer(modifier = Modifier.height(12.dp))
            Text("$${"%,.2f".format(report.remaining)}", color = Color.White, fontSize = 44.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun InsightsContent(summaries: List<ExpenseSummary>) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        item {
            Text("Data Matrix Breakdown", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Black)
            Surface(
                modifier = Modifier.padding(top = 12.dp).fillMaxWidth().height(250.dp),
                color = Color.White.copy(alpha = 0.03f),
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))
            ) {
                Row(modifier = Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1.2f)) {
                        Text("Spending Habits", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            InteractiveDonutChart3D(summaries)
                        }
                    }
                    Column(Modifier.weight(0.8f), verticalArrangement = Arrangement.Center) {
                        Text("Top Categories", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(16.dp))
                        summaries.take(4).forEachIndexed { i, s ->
                            CategoryMiniBar3D(s.percentage, i)
                            Spacer(Modifier.height(12.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InteractiveDonutChart3D(summaries: List<ExpenseSummary>) {
    var selectedIndex by remember { mutableIntStateOf(-1) }
    val total = summaries.sumOf { it.totalAmount }.coerceAtLeast(1.0)
    val anim = remember { Animatable(0f) }
    
    LaunchedEffect(summaries) { 
        anim.snapTo(0f)
        anim.animateTo(1f, tween(1500, easing = FastOutSlowInEasing)) 
    }

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(160.dp)) {
        Canvas(
            modifier = Modifier.fillMaxSize().pointerInput(summaries) {
                detectTapGestures { offset ->
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val dx = offset.x - center.x
                    val dy = offset.y - center.y
                    val distance = sqrt((dx * dx + dy * dy).toDouble())
                    if (distance > 30.dp.toPx() && distance < 80.dp.toPx()) {
                        var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                        if (angle < 0) angle += 360f
                        angle = (angle + 90f) % 360f
                        var currentAngle = 0f
                        var found = -1
                        for (i in summaries.indices) {
                            val sweep = (summaries[i].totalAmount / total).toFloat() * 360f
                            if (angle >= currentAngle && angle <= currentAngle + sweep) { found = i; break }
                            currentAngle += sweep
                        }
                        selectedIndex = if (selectedIndex == found) -1 else found
                    } else { selectedIndex = -1 }
                }
            }
        ) {
            var startAngle = -90f
            summaries.forEachIndexed { i, s ->
                val sweep = (s.totalAmount / total).toFloat() * 360f * anim.value
                val isSelected = selectedIndex == i
                val offsetAngle = startAngle + (sweep / 2)
                val offsetX = if (isSelected) 8.dp.toPx() * cos(Math.toRadians(offsetAngle.toDouble())).toFloat() else 0f
                val offsetY = if (isSelected) 8.dp.toPx() * sin(Math.toRadians(offsetAngle.toDouble())).toFloat() else 0f
                
                // 3D layers
                drawArc(Color.Black.copy(alpha = 0.4f), startAngle, sweep, false, style = Stroke(24.dp.toPx()), topLeft = Offset(offsetX, offsetY + 6.dp.toPx()))
                drawArc(brush = Brush.verticalGradient(listOf(ChartColors[i % ChartColors.size], ChartColors[i % ChartColors.size].copy(alpha = 0.4f))), startAngle, sweep, false, style = Stroke(20.dp.toPx(), cap = StrokeCap.Round), topLeft = Offset(offsetX, offsetY))
                
                startAngle += (s.totalAmount / total).toFloat() * 360f
            }
        }
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (selectedIndex != -1 && selectedIndex < summaries.size) {
                val s = summaries[selectedIndex]
                Text(s.categoryName.uppercase(), color = MatrixCyan, fontSize = 10.sp, fontWeight = FontWeight.Black)
                Text("$${"%.0f".format(s.totalAmount)}", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("${(s.percentage * 100).toInt()}%", color = Color.Gray, fontSize = 10.sp)
            } else {
                Text("TOTAL", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text("$${"%.0f".format(total)}", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
fun ActionHub(isExpanded: Boolean, onToggle: () -> Unit, onVoice: () -> Unit, onScan: () -> Unit, onExpense: () -> Unit, onIncome: () -> Unit) {
    val rotation by animateFloatAsState(if (isExpanded) 45f else 0f, label = "rot")
    val pulse by rememberInfiniteTransition().animateFloat(1f, 1.2f, infiniteRepeatable(tween(1500), RepeatMode.Reverse), label = "p")

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (isExpanded) {
            Row(modifier = Modifier.padding(bottom = 16.dp), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                FloatingActionButton(onClick = onExpense, containerColor = MatrixCoral, shape = CircleShape) { Icon(Icons.Rounded.Remove, null, tint = Color.White) }
                FloatingActionButton(onClick = onIncome, containerColor = MatrixCyan, shape = CircleShape) { Icon(Icons.Rounded.Add, null, tint = Color.Black) }
            }
        }
        Surface(color = Color(0xFF111111).copy(alpha = 0.95f), shape = RoundedCornerShape(50), modifier = Modifier.height(75.dp).padding(horizontal = 16.dp), border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 12.dp)) {
                IconButton(onClick = onVoice, modifier = Modifier.size(50.dp).border(1.dp, MatrixCyan, CircleShape)) { Icon(Icons.Rounded.Mic, null, tint = MatrixCyan) }
                Spacer(Modifier.width(16.dp))
                Box(contentAlignment = Alignment.Center) {
                    Box(Modifier.size(60.dp * pulse).background(MatrixCyan.copy(alpha = 0.1f), CircleShape))
                    FloatingActionButton(onClick = onToggle, containerColor = Color.White, shape = CircleShape, modifier = Modifier.size(56.dp)) { 
                        Icon(imageVector = Icons.Rounded.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(28.dp).graphicsLayer(rotationZ = rotation)) 
                    }
                }
                Spacer(Modifier.width(16.dp))
                IconButton(onClick = onScan, modifier = Modifier.size(50.dp).border(1.dp, MatrixPurple, CircleShape)) { Icon(Icons.Rounded.QrCodeScanner, null, tint = MatrixPurple) }
            }
        }
    }
}

@Composable fun CategoryMiniBar3D(p: Float, i: Int) {
    val width = remember { Animatable(0f) }
    LaunchedEffect(p) { delay(i * 100L); width.animateTo(p, tween(800)) }
    Box(Modifier.fillMaxWidth().height(10.dp).clip(CircleShape).background(Color.White.copy(0.05f))) {
        Box(Modifier.fillMaxWidth(width.value.coerceIn(0.01f, 1f)).fillMaxHeight().clip(CircleShape).background(Brush.horizontalGradient(listOf(MatrixCyan, MatrixPurple))))
    }
}

@Composable fun HeaderSection(m: String, onCalendarClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column {
            Text("Track Your Finance", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
            Text("Cloud Persistence: Active", color = Color.Gray, fontSize = 12.sp)
        }
        Surface(onClick = onCalendarClick, color = Color.White.copy(0.05f), shape = RoundedCornerShape(50), border = BorderStroke(1.dp, MatrixPurple.copy(0.5f))) {
            Text(m, color = Color.White, modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable fun TabSection(s: Int, onS: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(top = 24.dp)) {
        listOf("Insights", "History", "Revenue").forEachIndexed { i, t ->
            val active = s == i
            Column(Modifier.weight(1f).clickable { onS(i) }, horizontalAlignment = Alignment.CenterHorizontally) {
                Text(t, color = if (active) MatrixCyan else Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                if (active) Box(Modifier.padding(top = 6.dp).width(40.dp).height(2.5.dp).background(MatrixCyan))
            }
        }
    }
}

@Composable fun MeshGradientBackground() { Canvas(Modifier.fillMaxSize().blur(90.dp)) { drawCircle(MatrixCyan, radius = size.width * 0.8f, center = Offset(0f, 0f), alpha = 0.15f) ; drawCircle(MatrixMagenta, radius = size.width * 0.8f, center = Offset(size.width, size.height * 0.5f), alpha = 0.12f) } }

@Composable fun MonthPickerMinimal(currentMonth: String, onMonthSelected: (String) -> Unit, onDismiss: () -> Unit) {
    val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
    val cal = Calendar.getInstance()
    val months = (0..5).map { val m = cal.clone() as Calendar ; m.add(Calendar.MONTH, -it) ; sdf.format(m.time) }
    AlertDialog(onDismissRequest = onDismiss, containerColor = Color(0xFF1A1A1A), title = { Text("Select Month", color = Color.White) }, text = { Column { months.forEach { month -> Text(text = month, color = if (month == currentMonth) MatrixCyan else Color.Gray, modifier = Modifier.fillMaxWidth().clickable { onMonthSelected(month) }.padding(16.dp), fontWeight = if (month == currentMonth) FontWeight.Bold else FontWeight.Normal) } } }, confirmButton = {} )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun ManualEntryDialog(title: String, label: String, onDismiss: () -> Unit, onAdd: (String, String, String) -> Unit) {
    var n by remember { mutableStateOf("") }; var v by remember { mutableStateOf("") }; var c by remember { mutableStateOf("General") }
    val cats = listOf("Food", "Groceries", "Travel", "Bills", "Entertainment", "Shopping", "General")
    AlertDialog(onDismissRequest = onDismiss, containerColor = Color(0xFF1A1A1A), title = { Text(title, color = Color.White) }, text = { Column(verticalArrangement = Arrangement.spacedBy(12.dp)) { OutlinedTextField(value = n, onValueChange = { n = it }, label = { Text(label) }, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MatrixCyan, focusedTextColor = Color.White, unfocusedTextColor = Color.White)); OutlinedTextField(value = v, onValueChange = { v = it }, label = { Text("Amount ($)") }, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MatrixCyan, focusedTextColor = Color.White, unfocusedTextColor = Color.White)); if (title.contains("Expense")) { Text("Select Category", color = Color.Gray, fontSize = 12.sp); LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { items(cats) { cat -> FilterChip(selected = c == cat, onClick = { c = cat }, label = { Text(cat) }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MatrixCyan, selectedLabelColor = Color.Black, labelColor = Color.Gray)) } } } } }, confirmButton = { Button(onClick = { if(v.isNotEmpty()) onAdd(n, c, v) }, colors = ButtonDefaults.buttonColors(containerColor = MatrixCyan, contentColor = Color.Black)) { Text("INITIALIZE") } })
}

fun formatIsoDate(iso: String): String { return try { val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply { timeZone = TimeZone.getTimeZone("UTC") } ; val d = sdf.parse(iso) ; SimpleDateFormat("dd MMM", Locale.getDefault()).format(d!!) } catch (e: Exception) { iso.take(10) } }
@Composable fun HistoryContent(ex: List<Expense>) { LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 20.dp)) { items(ex) { expense -> Row(modifier = Modifier.fillMaxWidth().background(Color.White.copy(0.05f), RoundedCornerShape(16.dp)).padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) { Column { Text(expense.note ?: "Expense", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold) ; Text(formatIsoDate(expense.date), color = Color.Gray, fontSize = 10.sp) } ; Text("-$${"%.2f".format(expense.amount)}", color = MatrixCoral, fontWeight = FontWeight.Black) } } } }
@Composable fun IncomeContent(inc: List<Income>) { LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 20.dp)) { items(inc) { income -> Row(modifier = Modifier.fillMaxWidth().background(MatrixCyan.copy(0.05f), RoundedCornerShape(16.dp)).padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) { Column { Text(income.source, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold) ; Text(formatIsoDate(income.date), color = Color.Gray, fontSize = 10.sp) } ; Text("+$${"%.2f".format(income.amount)}", color = Color(0xFF4CAF50), fontWeight = FontWeight.Black) } } } }
@Composable fun ScanSourceDialog(onDismiss: () -> Unit, onCamera: () -> Unit, onGallery: () -> Unit) { AlertDialog(onDismissRequest = onDismiss, containerColor = Color(0xFF1A1A1A), title = { Text("Scan Receipt", color = Color.White) }, text = { Column { ListItem(headlineContent = { Text("Camera", color = Color.White) }, leadingContent = { Icon(Icons.Rounded.Camera, null, tint = MatrixCyan) }, modifier = Modifier.clickable(onClick = onCamera), colors = ListItemDefaults.colors(containerColor = Color.Transparent)) ; ListItem(headlineContent = { Text("Gallery", color = Color.White) }, leadingContent = { Icon(Icons.Rounded.PhotoLibrary, null, tint = MatrixCyan) }, modifier = Modifier.clickable(onClick = onGallery), colors = ListItemDefaults.colors(containerColor = Color.Transparent)) } }, confirmButton = {} ) }
