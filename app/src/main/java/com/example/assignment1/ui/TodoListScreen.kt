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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.assignment1.R
import com.example.assignment1.api.Todo
import com.example.assignment1.viewmodels.TodoListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoListScreen(viewModel: TodoListViewModel, userId: String) {
    var showAddDialog by remember { mutableStateOf(false) }
    var newTodoText by remember { mutableStateOf("") }

    val todos by viewModel.todos.collectAsState()
    val todoListState by viewModel.todoListState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchTodos(userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_todo))
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

        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text(stringResource(R.string.add_todo)) },
                text = {
                    OutlinedTextField(
                        value = newTodoText,
                        onValueChange = { newTodoText = it },
                        label = { Text(stringResource(R.string.new_todo)) }
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newTodoText.isNotBlank()) {
                                viewModel.createTodo(userId, newTodoText)
                                newTodoText = ""
                                showAddDialog = false
                            }
                        }
                    ) {
                        Text(stringResource(R.string.save))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }

        when (todoListState) {
            is TodoListViewModel.TodoListState.Error -> {
                AlertDialog(
                    onDismissRequest = { },
                    title = { Text(stringResource(R.string.error)) },
                    text = { Text((todoListState as TodoListViewModel.TodoListState.Error).message) },
                    confirmButton = {
                        Button(onClick = { viewModel.fetchTodos(userId) }) {
                            Text(stringResource(R.string.retry))
                        }
                    }
                )
            }
            is TodoListViewModel.TodoListState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            else -> {}
        }
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