package com.example.assignment1.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.assignment1.viewmodels.CreateAccountViewModel

@Composable
fun CreateAccountScreen(
    viewModel: CreateAccountViewModel,
    onNavigateToLogin: () -> Unit,
    onNavigateToTodoList: (String, String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    val createAccountState by viewModel.createAccountState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    showError = true
                } else {
                    viewModel.createAccount(email, password)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Create Account")
        }
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = onNavigateToLogin) {
            Text("Log In")
        }

        if (showError) {
            Text("Please enter both email and password", color = MaterialTheme.colorScheme.error)
        }
    }

    LaunchedEffect(createAccountState) {
        when (createAccountState) {
            is CreateAccountViewModel.CreateAccountState.Success -> {
                onNavigateToTodoList(
                    (createAccountState as CreateAccountViewModel.CreateAccountState.Success).token,
                    (createAccountState as CreateAccountViewModel.CreateAccountState.Success).userId
                )
            }
            is CreateAccountViewModel.CreateAccountState.Error -> {
                showError = true
            }
            else -> {}
        }
    }
}