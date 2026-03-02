package com.example.myapplication.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey val id: String,
    @SerializedName("user_id") val userId: String,
    val name: String,
    @SerializedName("parent_id") val parentId: String? = null
)

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey val id: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("category") val category: String,
    val amount: Double,
    val currency: String = "USD",
    val date: String,
    val note: String? = null
)

@Entity(tableName = "incomes")
data class Income(
    @PrimaryKey val id: String,
    @SerializedName("user_id") val userId: String,
    val source: String,
    val amount: Double,
    val currency: String = "USD",
    val date: String, 
    val note: String? = null
)

@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey val id: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("category") val category: String,
    @SerializedName("monthly_limit") val monthlyLimit: Double,
    val currency: String = "USD",
    val month: String
)

@Entity(tableName = "savings_goals")
data class SavingsGoal(
    @PrimaryKey val id: String,
    @SerializedName("user_id") val userId: String,
    val title: String,
    @SerializedName("target_amount") val targetAmount: Double,
    @SerializedName("current_amount") val currentAmount: Double = 0.0,
    val currency: String = "USD",
    val deadline: String? = null
)

data class ExpenseSummary(
    val categoryName: String,
    val totalAmount: Double,
    val currency: String,
    val percentage: Float
)
