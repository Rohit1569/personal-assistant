package com.example.myapplication.ui

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.models.*
import com.example.myapplication.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

private val ChartColors = listOf(
    ElectricCyan, DeepPurple, Color(0xFF00E676), Color(0xFFFFD600), 
    Color(0xFFFF1744), Color(0xFFD500F9), Color(0xFF3D5AFE), 
    Color(0xFFFB8C00), Color(0xFF00ACC1)
)

@Composable
fun FinanceDashboardScreen(
    viewModel: FinanceViewModel,
    onScanRequest: () -> Unit,
    onGalleryRequest: () -> Unit,
    onVoiceRequest: () -> Unit // NEW PARAMETER
) {
    val expenses by viewModel.expenses.collectAsState()
    val summaries by viewModel.summaries.collectAsState()
    val incomes by viewModel.incomes.collectAsState()
    val monthlyReport by viewModel.monthlyReport.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    
    var showEntryTypeMenu by remember { mutableStateOf(false) }
    var entryTypeToAdd by remember { mutableStateOf<String?>(null) }
    var currentTab by remember { mutableStateOf(0) }
    var showMonthPicker by remember { mutableStateOf(false) }
    var showScanOptions by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(DeepBlack)) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("NEURAL_FINANCE", color = ElectricCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                Surface(
                    onClick = { showMonthPicker = true },
                    color = Color.White.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, ElectricCyan.copy(alpha = 0.3f))
                ) {
                    Row(Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.CalendarMonth, null, tint = ElectricCyan, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(selectedMonth, color = SoftNeonWhite, fontSize = 12.sp)
                    }
                }
            }
            
            monthlyReport?.let { DashboardCard(it) }

            ScrollableTabRow(
                selectedTabIndex = currentTab,
                containerColor = Color.Transparent,
                contentColor = ElectricCyan,
                edgePadding = 0.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(Modifier.tabIndicatorOffset(tabPositions[currentTab]), color = ElectricCyan)
                },
                divider = {}
            ) {
                listOf("DASHBOARD", "HISTORY", "INCOME").forEachIndexed { index, title ->
                    Tab(selected = currentTab == index, onClick = { currentTab = index }) {
                        Text(title, modifier = Modifier.padding(12.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(modifier = Modifier.weight(1f)) {
                when (currentTab) {
                    0 -> DashboardTab(monthlyReport, summaries)
                    1 -> HistoryContent(expenses)
                    2 -> IncomeContent(incomes)
                }
            }
        }

        // FAB ENTRY HUB
        Column(modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 24.dp, end = 24.dp), horizontalAlignment = Alignment.End) {
            if (showEntryTypeMenu) {
                listOf("Income", "Expense").forEach { type ->
                    ExtendedFloatingActionButton(
                        onClick = { entryTypeToAdd = type; showEntryTypeMenu = false },
                        containerColor = DeepPurple, contentColor = Color.White, modifier = Modifier.padding(bottom = 8.dp),
                        icon = { Icon(Icons.Rounded.Add, null) }, text = { Text(type) }
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // VOICE TRIGGER BUTTON
                FloatingActionButton(onClick = onVoiceRequest, containerColor = ElectricCyan, contentColor = DeepBlack) {
                    Icon(Icons.Rounded.Mic, "Voice Add")
                }
                FloatingActionButton(onClick = { showScanOptions = true }, containerColor = DeepPurple, contentColor = Color.White) {
                    Icon(Icons.Rounded.CameraAlt, "Scan")
                }
                FloatingActionButton(onClick = { showEntryTypeMenu = !showEntryTypeMenu }, containerColor = ElectricCyan, contentColor = DeepBlack) {
                    Icon(if (showEntryTypeMenu) Icons.Rounded.Close else Icons.Rounded.Add, "Add")
                }
            }
        }

        // Dialogs
        if (showScanOptions) {
            AlertDialog(
                onDismissRequest = { showScanOptions = false },
                containerColor = DeepBlack,
                title = { Text("SCAN_RECEIPT", color = SoftNeonWhite, fontSize = 14.sp) },
                text = {
                    Column {
                        ListItem(headlineContent = { Text("Take Photo", color = SoftNeonWhite) },
                            leadingContent = { Icon(Icons.Rounded.PhotoCamera, null, tint = ElectricCyan) },
                            modifier = Modifier.clickable { onScanRequest(); showScanOptions = false },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent))
                        ListItem(headlineContent = { Text("Upload from Gallery", color = SoftNeonWhite) },
                            leadingContent = { Icon(Icons.Rounded.Collections, null, tint = ElectricCyan) },
                            modifier = Modifier.clickable { onGalleryRequest(); showScanOptions = false },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent))
                    }
                }, confirmButton = {}
            )
        }

        if (showMonthPicker) {
            MonthPickerMinimal(currentMonth = selectedMonth, onMonthSelected = { viewModel.setSelectedMonth(it); showMonthPicker = false }, onDismiss = { showMonthPicker = false })
        }

        when (entryTypeToAdd) {
            "Expense" -> ManualEntryDialog("Add Expense", "Note", onDismiss = { entryTypeToAdd = null }) { name, cat, valStr ->
                viewModel.addManualExpense(name, cat, valStr.toDoubleOrNull() ?: 0.0); entryTypeToAdd = null
            }
            "Income" -> ManualEntryDialog("Add Income", "Source", onDismiss = { entryTypeToAdd = null }) { source, _, valStr ->
                viewModel.addIncome(source, valStr.toDoubleOrNull() ?: 0.0); entryTypeToAdd = null
            }
        }
    }
}

@Composable
fun DashboardTab(report: MonthlyReport?, summaries: List<ExpenseSummary>) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(bottom = 100.dp)) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatBox("DAILY_AVG", "$${"%.0f".format(report?.dailyAverage ?: 0.0)}", Modifier.weight(1f))
                StatBox("TOP_CAT", report?.topCategory ?: "N/A", Modifier.weight(1f))
            }
        }

        if (summaries.isNotEmpty()) {
            item { InteractivePieChart(summaries) }
        }
        
        if (report?.warnings?.isNotEmpty() == true) {
            item {
                Surface(color = Color(0xFFFF5252).copy(alpha = 0.1f), shape = RoundedCornerShape(16.dp), border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFFFF5252).copy(alpha = 0.3f))) {
                    Column(Modifier.padding(16.dp)) {
                        Text("CRITICAL_ALERTS", color = Color(0xFFFF5252), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        report.warnings.forEach { Text("⚠️ $it", color = SoftNeonWhite, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp)) }
                    }
                }
            }
        }

        if (report?.insights?.isNotEmpty() == true) {
            item {
                Surface(color = DeepPurple.copy(alpha = 0.1f), shape = RoundedCornerShape(16.dp), border = BoxBorder) {
                    Column(Modifier.padding(16.dp)) {
                        Text("NEURAL_INSIGHTS", color = DeepPurple, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        report.insights.forEach { Text("• $it", color = SoftNeonWhite, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp)) }
                    }
                }
            }
        }

        if (report?.savingTips?.isNotEmpty() == true) {
            item {
                Surface(color = ElectricCyan.copy(alpha = 0.05f), shape = RoundedCornerShape(16.dp), border = BoxBorder) {
                    Column(Modifier.padding(16.dp)) {
                        Text("SMART_ADVICE", color = ElectricCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        report.savingTips.forEach { Text("⚡ $it", color = SoftNeonWhite, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp)) }
                    }
                }
            }
        }
        
        item { Text("SPENDING_BREAKDOWN", color = Color.Gray, fontSize = 10.sp, modifier = Modifier.padding(top = 8.dp)) }
        items(summaries) { CategoryItem(it.categoryName, it.totalAmount, it.percentage) }
    }
}

@Composable
fun InteractivePieChart(summaries: List<ExpenseSummary>) {
    var selectedIndex by remember { mutableStateOf(-1) }
    val totalAmount = summaries.sumOf { it.totalAmount }

    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(24.dp),
        border = BoxBorder
    ) {
        Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("SPENDING_DISTRIBUTION", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(24.dp))

            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(200.dp)) {
                Canvas(modifier = Modifier.fillMaxSize().pointerInput(summaries) {
                    detectTapGestures { offset ->
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val angle = Math.toDegrees(Math.atan2((offset.y - center.y).toDouble(), (offset.x - center.x).toDouble()))
                            .let { if (it < 0) it + 360 else it }.let { (it + 90) % 360 }
                        var currentAngle = 0.0
                        var found = -1
                        summaries.forEachIndexed { index, summary ->
                            val sweep = (summary.totalAmount / totalAmount) * 360f
                            if (angle >= currentAngle && angle <= currentAngle + sweep) found = index
                            currentAngle += sweep
                        }
                        selectedIndex = if (selectedIndex == found) -1 else found
                    }
                }) {
                    var startAngle = -90f
                    summaries.forEachIndexed { index, summary ->
                        val sweepAngle = (summary.totalAmount / totalAmount).toFloat() * 360f
                        val isSelected = selectedIndex == index
                        val stroke = if (isSelected) 35.dp.toPx() else 25.dp.toPx()
                        drawArc(
                            color = if (selectedIndex == -1 || isSelected) ChartColors[index % ChartColors.size] else ChartColors[index % ChartColors.size].copy(alpha = 0.2f),
                            startAngle = startAngle, sweepAngle = sweepAngle, useCenter = false,
                            style = Stroke(width = stroke, cap = androidx.compose.ui.graphics.StrokeCap.Round),
                            size = Size(size.width, size.height)
                        )
                        startAngle += sweepAngle
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (selectedIndex == -1) {
                        Text("TOTAL", color = Color.Gray, fontSize = 8.sp)
                        Text("$${"%.0f".format(totalAmount)}", color = SoftNeonWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    } else {
                        val sel = summaries[selectedIndex]
                        Text(sel.categoryName.uppercase(), color = ElectricCyan, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        Text("$${"%.0f".format(sel.totalAmount)}", color = SoftNeonWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("${(sel.percentage * 100).toInt()}%", color = Color.Gray, fontSize = 10.sp)
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
            LazyRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                items(summaries.size) { i ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp).clickable { selectedIndex = if(selectedIndex == i) -1 else i }) {
                        Box(Modifier.size(8.dp).clip(CircleShape).background(ChartColors[i % ChartColors.size]))
                        Spacer(Modifier.width(6.dp))
                        Text(summaries[i].categoryName, color = if (selectedIndex == i) SoftNeonWhite else Color.Gray, fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardCard(report: MonthlyReport) {
    Surface(modifier = Modifier.padding(vertical = 20.dp).fillMaxWidth(), color = GlassWhite, shape = RoundedCornerShape(24.dp), border = BoxBorder) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("REMAINING_SAVINGS", color = SoftNeonWhite.copy(alpha = 0.6f), fontSize = 10.sp)
                    Text("$${"%.2f".format(report.remaining)}", color = if (report.remaining >= 0) ElectricCyan else Color(0xFFFF5252), fontSize = 28.sp, fontWeight = FontWeight.Bold)
                }
                Surface(color = ElectricCyan.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                    Text("HEALTH: ${report.healthScore}%", color = ElectricCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
            }
            Spacer(Modifier.height(20.dp))
            val progColor = when { report.percentageUsed >= 0.9f -> Color(0xFFFF5252); report.percentageUsed >= 0.7f -> Color(0xFFFFA000); else -> ElectricCyan }
            LinearProgressIndicator(progress = { report.percentageUsed.coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape), color = progColor, trackColor = Color.White.copy(alpha = 0.1f))
            Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${(report.percentageUsed * 100).toInt()}% OF INCOME SPENT", color = Color.Gray, fontSize = 8.sp)
                if (report.remaining < 0) Text("OVERSPENT", color = Color.Red, fontSize = 8.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun StatBox(label: String, value: String, modifier: Modifier) {
    Surface(modifier = modifier, color = Color.White.copy(alpha = 0.03f), shape = RoundedCornerShape(16.dp), border = BoxBorder) {
        Column(Modifier.padding(16.dp)) {
            Text(label, color = Color.Gray, fontSize = 8.sp); Text(value, color = SoftNeonWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun HistoryContent(expenses: List<Expense>) {
    if (expenses.isEmpty()) EmptyState("No transactions yet.")
    else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 80.dp)) {
            item { Text("HISTORY", color = Color.Gray, fontSize = 10.sp); Spacer(Modifier.height(8.dp)) }
            items(expenses) { ExpenseRow(it) }
        }
    }
}

@Composable
fun IncomeContent(incomes: List<Income>) {
    if (incomes.isEmpty()) EmptyState("No income tracked.")
    else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(incomes) { income ->
                Row(Modifier.fillMaxWidth().background(Color.White.copy(alpha = 0.02f), RoundedCornerShape(8.dp)).padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(income.source, color = SoftNeonWhite, fontSize = 14.sp)
                        Text(formatIsoDate(income.date), color = Color.Gray, fontSize = 10.sp)
                    }
                    Text("+$${"%.2f".format(income.amount)}", color = Color(0xFF4CAF50), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun EmptyState(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Icon(Icons.Rounded.QueryStats, null, tint = ElectricCyan.copy(alpha = 0.2f), modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.height(16.dp)); Text(message, color = Color.Gray, fontSize = 14.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualEntryDialog(title: String, label: String, onDismiss: () -> Unit, onAdd: (String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }; var value by remember { mutableStateOf("") }; var category by remember { mutableStateOf("General") }
    val categories = listOf("Food", "Groceries", "Travel", "Bills", "Shopping", "Entertainment", "General")
    AlertDialog(onDismissRequest = onDismiss, title = { Text(title, color = SoftNeonWhite) }, containerColor = DeepBlack, text = {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(label) }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = SoftNeonWhite, unfocusedTextColor = SoftNeonWhite, focusedBorderColor = ElectricCyan, unfocusedBorderColor = Color.Gray))
            OutlinedTextField(value = value, onValueChange = { value = it }, label = { Text("Amount ($)") }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = SoftNeonWhite, unfocusedTextColor = SoftNeonWhite, focusedBorderColor = ElectricCyan, unfocusedBorderColor = Color.Gray))
            if (title.contains("Expense")) {
                Text("Select Category", color = Color.Gray, fontSize = 12.sp)
                LazyRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { cat -> FilterChip(selected = category == cat, onClick = { category = cat }, label = { Text(cat) }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = ElectricCyan, selectedLabelColor = DeepBlack, labelColor = Color.Gray)) }
                }
            }
        }
    }, confirmButton = { Button(onClick = { if (value.isNotEmpty()) onAdd(name, category, value) }, colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = DeepBlack)) { Text("ADD") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("CANCEL", color = Color.Gray) } })
}

@Composable
fun CategoryItem(name: String, amount: Double, percentage: Float) {
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(name.uppercase(), color = SoftNeonWhite, fontSize = 12.sp); Text("$${"%.2f".format(amount)}", color = SoftNeonWhite, fontSize = 12.sp)
        }
        LinearProgressIndicator(progress = { percentage }, modifier = Modifier.fillMaxWidth().height(4.dp).padding(top = 8.dp).clip(CircleShape), color = ElectricCyan)
    }
}

@Composable
fun ExpenseRow(expense: Expense) {
    Row(Modifier.fillMaxWidth().background(Color.White.copy(alpha = 0.02f), RoundedCornerShape(8.dp)).padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Column {
            Text(expense.note ?: "Expense", color = SoftNeonWhite, fontSize = 14.sp)
            Text(formatIsoDate(expense.date), color = Color.Gray, fontSize = 10.sp)
        }
        Text("-$${"%.2f".format(expense.amount)}", color = Color(0xFFFF5252), fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

fun formatIsoDate(isoString: String): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val date = sdf.parse(isoString); SimpleDateFormat("dd MMM", Locale.getDefault()).format(date!!)
    } catch (e: Exception) { isoString.take(10) }
}

@Composable
fun MonthPickerMinimal(currentMonth: String, onMonthSelected: (String) -> Unit, onDismiss: () -> Unit) {
    val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault()); val cal = Calendar.getInstance()
    val months = (0..5).map { val m = cal.clone() as Calendar; m.add(Calendar.MONTH, -it); sdf.format(m.time) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("SELECT_MONTH", color = SoftNeonWhite, fontSize = 14.sp) }, containerColor = DeepBlack, text = {
        Column { months.forEach { month -> Text(text = month, color = if (month == currentMonth) ElectricCyan else Color.Gray, modifier = Modifier.fillMaxWidth().clickable { onMonthSelected(month) }.padding(16.dp), fontWeight = if (month == currentMonth) FontWeight.Bold else FontWeight.Normal) } }
    }, confirmButton = {})
}

private val BoxBorder = androidx.compose.foundation.BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f))
