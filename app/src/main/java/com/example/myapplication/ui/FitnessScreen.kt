package com.example.myapplication.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.models.FitnessProfile

// 2026 NEURAL PALETTE
private val MatrixCyan = Color(0xFF00F2FF)
private val MatrixPurple = Color(0xFF7000FF)
private val MatrixBlue = Color(0xFF1976D2)
private val MatrixOrange = Color(0xFFFF7043)
private val DeepObsidian = Color(0xFF050505)

@Composable
fun FitnessScreen(viewModel: FitnessViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    var firstName by remember { mutableStateOf("") }
    var middleInitial by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Male") }
    var age by remember { mutableIntStateOf(25) }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var bodyType by remember { mutableStateOf("Average") }
    var waterIntake by remember { mutableStateOf("") }
    var lifestyle by remember { mutableStateOf("Moderate") }
    var mealsPerDay by remember { mutableIntStateOf(3) }

    // Sync UI with backend state
    LaunchedEffect(uiState) {
        if (uiState is FitnessUiState.Success) {
            val profile = (uiState as FitnessUiState.Success).profile
            firstName = profile.firstName
            middleInitial = profile.middleInitial ?: ""
            lastName = profile.lastName
            gender = profile.gender
            age = profile.age
            height = profile.height.toString()
            weight = profile.weight.toString()
            bodyType = profile.bodyType
            waterIntake = profile.waterIntake.toString()
            lifestyle = profile.lifestyle
            mealsPerDay = profile.mealsPerDay
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(DeepObsidian)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Main Header
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "NEURAL FITNESS PROFILE",
                    color = Color(0xFFFFD54F),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
                
                if (uiState is FitnessUiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MatrixCyan, strokeWidth = 2.dp)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Personal Details Section
            FitnessSectionLabel("PERSONAL DETAILS")

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                NeuralInput(firstName, { firstName = it }, "First Name", Modifier.weight(1f), MatrixCyan)
                NeuralInput(middleInitial, { if (it.length <= 1) middleInitial = it }, "M.I.", Modifier.width(75.dp), MatrixCyan)
            }
            NeuralInput(lastName, { lastName = it }, "Last Name", glowColor = MatrixCyan)

            Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                GenderSelector(gender, { gender = it }, Modifier.weight(1.5f))
                AgeStepper(age, { age = it }, "Age", Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Body Metrics Section
            FitnessSectionLabel("BODY METRICS")

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                NeuralInput(height, { height = it }, "Height (cm)", Modifier.weight(1f), MatrixPurple, keyboardType = KeyboardType.Number)
                NeuralInput(weight, { weight = it }, "Weight (kg)", Modifier.weight(1f), MatrixPurple, keyboardType = KeyboardType.Number)
            }

            BodyTypeSelector(bodyType, { bodyType = it })

            Spacer(modifier = Modifier.height(32.dp))

            // Lifestyle Data Section
            FitnessSectionLabel("LIFESTYLE DATA")

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                NeuralInput(waterIntake, { waterIntake = it }, "Water (L)", Modifier.weight(1f), MatrixBlue, keyboardType = KeyboardType.Decimal)
                AgeStepper(mealsPerDay, { mealsPerDay = it }, "Meals", Modifier.weight(1f))
            }

            LifestyleSelector(lifestyle, { lifestyle = it })

            Spacer(modifier = Modifier.height(40.dp))

            // Integrated Save Button
            Button(
                onClick = {
                    val profile = FitnessProfile(
                        firstName = firstName,
                        middleInitial = middleInitial.takeIf { it.isNotEmpty() },
                        lastName = lastName,
                        gender = gender,
                        age = age,
                        height = height.toFloatOrNull() ?: 0f,
                        weight = weight.toFloatOrNull() ?: 0f,
                        bodyType = bodyType,
                        waterIntake = waterIntake.toFloatOrNull() ?: 0f,
                        lifestyle = lifestyle,
                        exerciseFlag = true,
                        foodType = "Mixed",
                        mealsPerDay = mealsPerDay
                    )
                    viewModel.saveProfile(profile)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(RoundedCornerShape(16.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = MatrixCyan),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                Icon(Icons.Rounded.Save, null, tint = DeepObsidian, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text("SAVE PROFILE", color = DeepObsidian, fontWeight = FontWeight.Black, fontSize = 16.sp, letterSpacing = 1.sp)
            }

            Spacer(modifier = Modifier.height(160.dp))
        }

        // Optional Floating Sync Indicator if background sync is preferred
        if (uiState is FitnessUiState.Success) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
                    .size(48.dp)
                    .background(MatrixCyan.copy(alpha = 0.1f), CircleShape)
                    .border(1.dp, MatrixCyan.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.CloudDone, null, tint = MatrixCyan, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun FitnessSectionLabel(text: String) {
    Text(
        text = text,
        color = MatrixCyan.copy(alpha = 0.7f),
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.5.sp,
        modifier = Modifier.padding(vertical = 12.dp)
    )
}

@Composable
fun NeuralInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    glowColor: Color,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Box(
        modifier = modifier
            .padding(vertical = 6.dp)
            .height(60.dp)
            .drawBehind {
                val radius = 16.dp.toPx()
                drawRoundRect(
                    color = glowColor.copy(alpha = 0.15f),
                    size = size,
                    cornerRadius = CornerRadius(radius, radius),
                    style = Stroke(width = 4.dp.toPx())
                )
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        listOf(glowColor.copy(alpha = 0.6f), glowColor.copy(alpha = 0.2f))
                    ),
                    size = size,
                    cornerRadius = CornerRadius(radius, radius),
                    style = Stroke(width = 1.5.dp.toPx())
                )
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.03f),
                    size = size,
                    cornerRadius = CornerRadius(radius, radius)
                )
            }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        if (value.isEmpty()) Text(label, color = Color.Gray, fontSize = 15.sp)
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 16.sp),
            modifier = Modifier.fillMaxWidth(),
            cursorBrush = SolidColor(glowColor),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
        )
    }
}

@Composable
fun GenderSelector(selected: String, onSelect: (String) -> Unit, modifier: Modifier) {
    Surface(
        modifier = modifier.height(85.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(8.dp)) {
            Text("Gender", color = Color.Gray, fontSize = 10.sp)
            Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.CenterVertically) {
                GenderItem(Icons.Rounded.Male, "Male", selected == "Male") { onSelect("Male") }
                GenderItem(Icons.Rounded.Female, "Female", selected == "Female") { onSelect("Female") }
                GenderItem(Icons.Rounded.MoreHoriz, "NB", selected == "NB") { onSelect("NB") }
            }
        }
    }
}

@Composable
fun GenderItem(icon: ImageVector, label: String, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) MatrixCyan.copy(alpha = 0.2f) else Color.Transparent)
            .clickable { onClick() }
            .padding(6.dp)
    ) {
        Icon(icon, null, tint = if (isSelected) MatrixCyan else Color.White, modifier = Modifier.size(22.dp))
        Text(label, color = if (isSelected) MatrixCyan else Color.White, fontSize = 10.sp)
    }
}

@Composable
fun AgeStepper(value: Int, onValueChange: (Int) -> Unit, label: String, modifier: Modifier) {
    Surface(
        modifier = modifier.height(85.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(label, color = Color.Gray, fontSize = 10.sp)
                Text(value.toString(), color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
            Column {
                Icon(Icons.Rounded.ArrowDropUp, null, tint = MatrixCyan, modifier = Modifier.clickable { onValueChange(value + 1) })
                Icon(Icons.Rounded.ArrowDropDown, null, tint = MatrixCyan, modifier = Modifier.clickable { if (value > 0) onValueChange(value - 1) })
            }
        }
    }
}

@Composable
fun BodyTypeSelector(selected: String, onSelect: (String) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text("Body Type", color = Color.Gray, fontSize = 10.sp)
            Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceAround) {
                BodyItem("Slender", selected == "Slender") { onSelect("Slender") }
                BodyItem("Athletic", selected == "Athletic") { onSelect("Athletic") }
                BodyItem("Average", selected == "Average") { onSelect("Average") }
                BodyItem("Stocky", selected == "Stocky") { onSelect("Stocky") }
            }
        }
    }
}

@Composable
fun BodyItem(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) MatrixPurple.copy(alpha = 0.2f) else Color.Transparent)
            .border(if (isSelected) 1.dp else 0.dp, MatrixPurple, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 12.dp)
    ) {
        Text(label, color = if (isSelected) MatrixCyan else Color.White, fontSize = 11.sp)
    }
}

@Composable
fun LifestyleSelector(selected: String, onSelect: (String) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text("Lifestyle Type", color = Color.Gray, fontSize = 10.sp)
            Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                listOf("Sedentary", "Light", "Moderate", "Active", "Intense").forEach { item ->
                    val active = selected == item
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (active) MatrixOrange.copy(alpha = 0.3f) else Color.Transparent)
                            .clickable { onSelect(item) }
                            .padding(6.dp)
                    ) {
                        Text(item, color = Color.White, fontSize = 9.sp)
                    }
                }
            }
        }
    }
}
