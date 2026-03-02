package com.example.myapplication.models

import com.google.gson.annotations.SerializedName

data class FitnessProfile(
    val id: String? = null,
    @SerializedName("user_id") val userId: String? = null,
    @SerializedName("first_name") val firstName: String,
    @SerializedName("middle_initial") val middleInitial: String? = null,
    @SerializedName("last_name") val lastName: String,
    val gender: String,
    val age: Int,
    val height: Float,
    val weight: Float,
    @SerializedName("body_type") val bodyType: String,
    @SerializedName("water_intake") val waterIntake: Float,
    val lifestyle: String,
    @SerializedName("exercise_flag") val exerciseFlag: Boolean,
    @SerializedName("exercise_per_day") val exercisePerDay: Int = 0,
    @SerializedName("exercise_per_week") val exercisePerWeek: Int = 0,
    @SerializedName("exercise_per_month") val exercisePerMonth: Int = 0,
    @SerializedName("food_type") val foodType: String,
    @SerializedName("meals_per_day") val mealsPerDay: Int,
    @SerializedName("outside_food_frequency") val outsideFoodFrequency: String? = null
)
