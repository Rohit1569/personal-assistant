package com.example.myapplication.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.ProductivityRepository
import com.example.myapplication.models.Note
import com.example.myapplication.models.Task
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

// Local UI model for Ideas (Not stored in DB)
data class Idea(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val content: String
)

@HiltViewModel
class ProductivityViewModel @Inject constructor(
    private val repository: ProductivityRepository
) : ViewModel() {

    private val _userId = MutableStateFlow<String?>(null)
    
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    // NEW: Independent local state for Ideas
    private val _ideas = MutableStateFlow<List<Idea>>(emptyList())
    val ideas: StateFlow<List<Idea>> = _ideas.asStateFlow()

    fun setUserId(id: String) {
        if (_userId.value == id) return
        Log.d("PROD_VIEWMODEL", ">>> IDENTITY LINKED: $id")
        _userId.value = id
        startSync()
    }

    private fun startSync() {
        viewModelScope.launch {
            repository.getTasks().collect { _tasks.value = it }
        }
        viewModelScope.launch {
            repository.getNotes().collect { _notes.value = it }
        }
    }

    fun addTask(title: String, desc: String?, priority: String, dueDate: String?) {
        val uid = _userId.value ?: return
        viewModelScope.launch {
            repository.createTask(Task(userId = uid, title = title, description = desc, priority = priority, dueDate = dueDate))
            repository.triggerRefresh()
        }
    }

    fun addNote(title: String, content: String?) {
        val uid = _userId.value ?: return
        viewModelScope.launch {
            repository.createNote(Note(userId = uid, title = title, content = content))
            repository.triggerRefresh()
        }
    }

    // NEW: Logic to add ideas only to the local list
    fun addIdea(title: String, content: String) {
        val newList = _ideas.value.toMutableList().apply {
            add(0, Idea(title = title, content = content))
        }
        _ideas.value = newList
    }

    fun deleteIdea(id: String) {
        _ideas.value = _ideas.value.filter { it.id != id }
    }

    fun toggleTaskStatus(task: Task) {
        viewModelScope.launch {
            val newStatus = if (task.status == "Pending") "Completed" else "Pending"
            repository.updateTask(task.copy(status = newStatus))
            repository.triggerRefresh()
        }
    }

    fun deleteTask(id: String) {
        viewModelScope.launch {
            repository.deleteTask(id)
            repository.triggerRefresh()
        }
    }

    fun deleteNote(id: String) {
        viewModelScope.launch {
            repository.deleteNote(id)
            repository.triggerRefresh()
        }
    }
}
