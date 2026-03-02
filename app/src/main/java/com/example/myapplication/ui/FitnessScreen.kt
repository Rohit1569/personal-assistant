package com.example.myapplication.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.models.FitnessProfile
import com.example.myapplication.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FitnessScreen(viewModel: FitnessViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    var firstName by remember { mutableStateOf("") }
    var middleInitial by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Male") }
    var age by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var bodyType by remember { mutableStateOf("Normal Build") }
    var waterIntake by remember { mutableStateOf("") }
    var lifestyle by remember { mutableStateOf("Moderate") }
    var exerciseFlag by remember { mutableStateOf(false) }
    var exPerDay by remember { mutableStateOf("0") }
    var exPerWeek by remember { mutableStateOf("0") }
    var exPerMonth by remember { mutableStateOf("0") }
    var foodType by remember { mutableStateOf("Mixed") }
    var mealsPerDay by remember { mutableStateOf("3") }
    var outsideFoodFreq by remember { mutableStateOf("") }

    LaunchedEffect(uiState) {
        if (uiState is FitnessUiState.Success) {
            val p = (uiState as FitnessUiState.Success).profile
            firstName = p.firstName
            middleInitial = p.middleInitial ?: ""
            lastName = p.lastName
            gender = p.gender
            age = p.age.toString()
            height = p.height.toString()
            weight = p.weight.toString()
            bodyType = p.bodyType
            waterIntake = p.waterIntake.toString()
            lifestyle = p.lifestyle
            exerciseFlag = p.exerciseFlag
            exPerDay = p.exercisePerDay.toString()
            exPerWeek = p.exercisePerWeek.toString()
            exPerMonth = p.exercisePerMonth.toString()
            foodType = p.foodType
            mealsPerDay = p.mealsPerDay.toString()
            outsideFoodFreq = p.outsideFoodFrequency ?: ""
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(DeepBlack)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState)
        ) {
            Text(
                text = "NEURAL_FITNESS_PROFILE",
                color = ElectricCyan,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(top = 40.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (uiState is FitnessUiState.Loading) {
                CircularProgressIndicator(color = ElectricCyan, modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                // Section: Personal Details
                FitnessSectionTitle("PERSONAL_DETAILS")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FitnessTextField(firstName, { firstName = it }, "First Name", Modifier.weight(1f))
                    FitnessTextField(middleInitial, { if (it.length <= 1) middleInitial = it }, "M.I.", Modifier.width(60.dp))
                }
                FitnessTextField(lastName, { lastName = it }, "Last Name")
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FitnessDropdown(gender, listOf("Male", "Female", "Other"), { gender = it }, "Gender", Modifier.weight(1f))
                    FitnessTextField(age, { age = it }, "Age", Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section: Body Metrics
                FitnessSectionTitle("BODY_METRICS")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FitnessTextField(height, { height = it }, "Height (cm)", Modifier.weight(1f))
                    FitnessTextField(weight, { weight = it }, "Weight (kg)", Modifier.weight(1f))
                }
                FitnessDropdown(bodyType, listOf("Skinny Build", "Normal Build", "Over Normal Build", "Medium Build", "Over Medium Build", "Heavy Build", "Obese Build"), { bodyType = it }, "Body Type")

                Spacer(modifier = Modifier.height(16.dp))

                // Section: Lifestyle
                FitnessSectionTitle("LIFESTYLE_DATA")
                FitnessTextField(waterIntake, { waterIntake = it }, "Daily Water Intake (L)")
                FitnessDropdown(lifestyle, listOf("Active", "Moderate", "Sedentary"), { lifestyle = it }, "Lifestyle Type")

                Spacer(modifier = Modifier.height(16.dp))

                // Section: Exercise
                FitnessSectionTitle("EXERCISE_HABITS")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = exerciseFlag, onCheckedChange = { exerciseFlag = it }, colors = CheckboxDefaults.colors(checkedColor = ElectricCyan))
                    Text("Do you exercise?", color = SoftNeonWhite, fontSize = 14.sp)
                }
                if (exerciseFlag) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FitnessTextField(exPerDay, { exPerDay = it }, "Times/Day", Modifier.weight(1f))
                        FitnessTextField(exPerWeek, { exPerWeek = it }, "Times/Week", Modifier.weight(1f))
                        FitnessTextField(exPerMonth, { exPerMonth = it }, "Times/Month", Modifier.weight(1f))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section: Food
                FitnessSectionTitle("NUTRITION_LOGIC")
                FitnessDropdown(foodType, listOf("Veg", "Non-Veg", "Mixed"), { foodType = it }, "Food Preference")
                FitnessTextField(mealsPerDay, { mealsPerDay = it }, "Meals Per Day")
                FitnessTextField(outsideFoodFreq, { outsideFoodFreq = it }, "Outside Food Frequency (e.g. 2/week)")

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        val profile = FitnessProfile(
                            firstName = firstName,
                            middleInitial = if (middleInitial.isEmpty()) null else middleInitial,
                            lastName = lastName,
                            gender = gender,
                            age = age.toIntOrNull() ?: 0,
                            height = height.toFloatOrNull() ?: 0f,
                            weight = weight.toFloatOrNull() ?: 0f,
                            bodyType = bodyType,
                            waterIntake = waterIntake.toFloatOrNull() ?: 0f,
                            lifestyle = lifestyle,
                            exerciseFlag = exerciseFlag,
                            exercisePerDay = exPerDay.toIntOrNull() ?: 0,
                            exercisePerWeek = exPerWeek.toIntOrNull() ?: 0,
                            exercisePerMonth = exPerMonth.toIntOrNull() ?: 0,
                            foodType = foodType,
                            mealsPerDay = mealsPerDay.toIntOrNull() ?: 0,
                            outsideFoodFrequency = outsideFoodFreq
                        )
                        viewModel.saveProfile(profile)
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = DeepBlack),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("SYNC_NEURAL_PROFILE", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun FitnessSectionTitle(title: String) {
    Text(
        text = title,
        color = Color.Gray,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FitnessTextField(value: String, onValueChange: (String) -> Unit, label: String, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = SoftNeonWhite,
            unfocusedTextColor = SoftNeonWhite,
            focusedBorderColor = ElectricCyan,
            unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
            focusedLabelColor = ElectricCyan,
            unfocusedLabelColor = Color.Gray
        ),
        shape = RoundedCornerShape(12.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FitnessDropdown(selected: String, options: List<String>, onSelected: (String) -> Unit, label: String, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier.padding(vertical = 4.dp)) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            label = { Text(label) },
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(if (expanded) Icons.Rounded.ArrowDropUp else Icons.Rounded.ArrowDropDown, null, tint = ElectricCyan)
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = SoftNeonWhite,
                unfocusedTextColor = SoftNeonWhite,
                focusedBorderColor = ElectricCyan,
                unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                focusedLabelColor = ElectricCyan,
                unfocusedLabelColor = Color.Gray
            ),
            shape = RoundedCornerShape(12.dp)
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(DeepBlack).border(0.5.dp, Color.White.copy(alpha = 0.1f))
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, color = SoftNeonWhite) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
