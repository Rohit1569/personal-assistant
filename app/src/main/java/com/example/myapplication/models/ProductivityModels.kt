package com.example.myapplication.models

import com.google.gson.annotations.SerializedName
import java.util.UUID

data class Task(
    val id: String = UUID.randomUUID().toString(),
    @SerializedName("user_id") val userId: String,
    val title: String,
    val description: String? = null,
    val priority: String = "Medium", // Low, Medium, High
    val status: String = "Pending", // Pending, Completed
    @SerializedName("due_date") val dueDate: String? = null,
    @SerializedName("created_at") val createdAt: String? = null
)

data class Note(
    val id: String = UUID.randomUUID().toString(),
    @SerializedName("user_id") val userId: String,
    val title: String,
    val content: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null
)
