package com.example.assignment1.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.assignment1.api.Todo
import com.example.assignment1.api.TodoApiService
import com.example.assignment1.api.TodoRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TodoListViewModel(private val apiService: TodoApiService) : ViewModel() {
    private val _todos = MutableStateFlow<List<Todo>>(emptyList())
    val todos: StateFlow<List<Todo>> = _todos

    private val _todoListState = MutableStateFlow<TodoListState>(TodoListState.Initial)
    val todoListState: StateFlow<TodoListState> = _todoListState

    fun fetchTodos(userId: String) {
        viewModelScope.launch {
            _todoListState.value = TodoListState.Loading
            try {
                Log.d("TodoListViewModel", "Fetching todos for user: $userId")
                val fetchedTodos = apiService.getTodos(userId)
                _todos.value = fetchedTodos
                _todoListState.value = TodoListState.Success
                Log.d("TodoListViewModel", "Successfully fetched ${fetchedTodos.size} todos")
            } catch (e: Exception) {
                Log.e("TodoListViewModel", "Error fetching todos", e)
                _todoListState.value = TodoListState.Error("Failed to fetch todos: ${e.localizedMessage}")
            }
        }
    }

    fun createTodo(userId: String, description: String) {
        viewModelScope.launch {
            try {
                Log.d("TodoListViewModel", "Creating todo for user: $userId with description: $description")
                val newTodo = apiService.createTodo(userId, TodoRequest(description))
                _todos.value = _todos.value + newTodo
                _todoListState.value = TodoListState.Success
                Log.d("TodoListViewModel", "Todo created successfully: ${newTodo.id}")
            } catch (e: Exception) {
                Log.e("TodoListViewModel", "Error creating todo", e)
                _todoListState.value = TodoListState.Error("Failed to create todo: ${e.localizedMessage}")
            }
        }
    }

    fun updateTodo(userId: String, todo: Todo) {
        viewModelScope.launch {
            try {
                val updatedTodo = apiService.updateTodo(userId, todo.id, TodoRequest(todo.description, if (todo.completed) 1 else 0))
                _todos.value = _todos.value.map { if (it.id == updatedTodo.id) updatedTodo else it }
                _todoListState.value = TodoListState.Success
            } catch (e: Exception) {
                Log.e("TodoListViewModel", "Error updating todo", e)
                _todoListState.value = TodoListState.Error("Failed to update todo: ${e.localizedMessage}")
            }
        }
    }

    sealed class TodoListState {
        object Initial : TodoListState()
        object Loading : TodoListState()
        object Success : TodoListState()
        data class Error(val message: String) : TodoListState()
    }
}