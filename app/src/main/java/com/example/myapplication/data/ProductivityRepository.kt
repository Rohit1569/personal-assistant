package com.example.myapplication.data

import android.util.Log
import com.example.myapplication.api.ProductivityApi
import com.example.myapplication.models.Note
import com.example.myapplication.models.Task
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductivityRepository @Inject constructor(
    private val api: ProductivityApi
) {
    private val _refreshSignal = MutableSharedFlow<Unit>(replay = 1)
    val refreshSignal = _refreshSignal.asSharedFlow()

    init { _refreshSignal.tryEmit(Unit) }

    fun triggerRefresh() { _refreshSignal.tryEmit(Unit) }

    // Removed redundant userId parameter - identifying via JWT Token instead
    fun getTasks(): Flow<List<Task>> = refreshSignal
        .onStart { emit(Unit) }
        .flatMapLatest {
            flow {
                try {
                    val response = api.getTasks()
                    if (response.isSuccessful) emit(response.body() ?: emptyList())
                } catch (e: Exception) { emit(emptyList()) }
            }
        }

    fun getNotes(): Flow<List<Note>> = refreshSignal
        .onStart { emit(Unit) }
        .flatMapLatest {
            flow {
                try {
                    val response = api.getNotes()
                    if (response.isSuccessful) emit(response.body() ?: emptyList())
                } catch (e: Exception) { emit(emptyList()) }
            }
        }

    suspend fun createTask(task: Task) {
        try { if (api.createTask(task).isSuccessful) triggerRefresh() } catch (e: Exception) { }
    }

    suspend fun updateTask(task: Task) {
        try { if (api.updateTask(task.id, task).isSuccessful) triggerRefresh() } catch (e: Exception) { }
    }

    suspend fun deleteTask(id: String) {
        try { if (api.deleteTask(id).isSuccessful) triggerRefresh() } catch (e: Exception) { }
    }

    suspend fun createNote(note: Note) {
        try { if (api.createNote(note).isSuccessful) triggerRefresh() } catch (e: Exception) { }
    }

    suspend fun deleteNote(id: String) {
        try { if (api.deleteNote(id).isSuccessful) triggerRefresh() } catch (e: Exception) { }
    }
}
