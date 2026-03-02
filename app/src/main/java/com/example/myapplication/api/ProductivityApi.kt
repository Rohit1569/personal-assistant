package com.example.myapplication.api

import com.example.myapplication.models.Note
import com.example.myapplication.models.Task
import retrofit2.Response
import retrofit2.http.*

interface ProductivityApi {
    // --- TASKS ---
    @POST("api/productivity/tasks")
    suspend fun createTask(@Body task: Task): Response<Task>

    @GET("api/productivity/tasks")
    suspend fun getTasks(): Response<List<Task>>

    @PUT("api/productivity/tasks/{id}")
    suspend fun updateTask(@Path("id") id: String, @Body task: Task): Response<Unit>

    @DELETE("api/productivity/tasks/{id}")
    suspend fun deleteTask(@Path("id") id: String): Response<Unit>

    // --- NOTES ---
    @POST("api/productivity/notes")
    suspend fun createNote(@Body note: Note): Response<Note>

    @GET("api/productivity/notes")
    suspend fun getNotes(): Response<List<Note>>

    @PUT("api/productivity/notes/{id}")
    suspend fun updateNote(@Path("id") id: String, @Body note: Note): Response<Unit>

    @DELETE("api/productivity/notes/{id}")
    suspend fun deleteNote(@Path("id") id: String): Response<Unit>
}
