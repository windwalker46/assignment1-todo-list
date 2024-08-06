package com.example.assignment1.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.assignment1.api.TodoApiService
import com.example.assignment1.api.Todo
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
                _todos.value = apiService.getTodos(userId)
                _todoListState.value = TodoListState.Success
            } catch (e: Exception) {
                _todoListState.value = TodoListState.Error("Failed to fetch todos. Please try again.")
            }
        }
    }

    fun createTodo(userId: String, text: String) {
        viewModelScope.launch {
            try {
                val newTodo = apiService.createTodo(userId, TodoRequest(text, false))
                _todos.value = _todos.value + newTodo
            } catch (e: Exception) {
                _todoListState.value = TodoListState.Error("Failed to create todo. Please try again.")
            }
        }
    }

    fun updateTodo(userId: String, todo: Todo) {
        viewModelScope.launch {
            try {
                val updatedTodo = apiService.updateTodo(userId, todo.id, TodoRequest(todo.text, todo.completed))
                _todos.value = _todos.value.map { if (it.id == updatedTodo.id) updatedTodo else it }
            } catch (e: Exception) {
                _todoListState.value = TodoListState.Error("Failed to update todo. Please try again.")
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