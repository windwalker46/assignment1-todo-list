package com.example.assignment1.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.assignment1.api.Todo
import com.example.assignment1.viewmodels.TodoListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoListScreen(
    viewModel: TodoListViewModel,
    userId: String
) {
    var showBottomSheet by remember { mutableStateOf(false) }
    var newTodoText by remember { mutableStateOf("") }

    val todos by viewModel.todos.collectAsState()
    val todoListState by viewModel.todoListState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchTodos(userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Todo List") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showBottomSheet = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Todo")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            items(todos) { todo ->
                TodoItem(
                    todo = todo,
                    onToggle = { viewModel.updateTodo(userId, todo.copy(completed = !todo.completed)) }
                )
            }
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = newTodoText,
                    onValueChange = { newTodoText = it },
                    label = { Text("New Todo") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { showBottomSheet = false }) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (newTodoText.isNotBlank()) {
                                viewModel.createTodo(userId, newTodoText)
                                newTodoText = ""
                                showBottomSheet = false
                            }
                        }
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }

    when (todoListState) {
        is TodoListViewModel.TodoListState.Error -> {
            AlertDialog(
                onDismissRequest = { },
                title = { Text("Error") },
                text = { Text((todoListState as TodoListViewModel.TodoListState.Error).message) },
                confirmButton = {
                    Button(onClick = { viewModel.fetchTodos(userId) }) {
                        Text("Retry")
                    }
                }
            )
        }
        else -> {}
    }
}

@Composable
fun TodoItem(todo: Todo, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = todo.completed,
            onCheckedChange = { onToggle() }
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(todo.text)
    }
}