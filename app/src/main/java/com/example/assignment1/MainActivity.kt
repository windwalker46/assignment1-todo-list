package com.example.assignment1
/* Ben Tran */
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.assignment1.api.TodoApiService
import com.example.assignment1.ui.CreateAccountScreen
import com.example.assignment1.ui.LoginScreen
import com.example.assignment1.ui.TodoListScreen
import com.example.assignment1.ui.theme.Assignment1Theme
import com.example.assignment1.viewmodels.CreateAccountViewModel
import com.example.assignment1.viewmodels.LoginViewModel
import com.example.assignment1.viewmodels.TodoListViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Assignment1Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val apiService = TodoApiService.create()
                    App(apiService)
                }
            }
        }
    }
}

@Composable
fun App(apiService: TodoApiService) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Login) }
    var userId by remember { mutableStateOf("") }
    var token by remember { mutableStateOf("") }

    when (val screen = currentScreen) {
        is Screen.Login -> {
            val viewModel: LoginViewModel = viewModel { LoginViewModel(apiService) }
            LoginScreen(
                viewModel = viewModel,
                onNavigateToCreateAccount = { currentScreen = Screen.CreateAccount },
                onNavigateToTodoList = { newToken, newUserId ->
                    token = newToken
                    userId = newUserId
                    currentScreen = Screen.TodoList
                }
            )
        }
        is Screen.CreateAccount -> {
            val viewModel: CreateAccountViewModel = viewModel { CreateAccountViewModel(apiService) }
            CreateAccountScreen(
                viewModel = viewModel,
                onNavigateToLogin = { currentScreen = Screen.Login },
                onNavigateToTodoList = { newToken, newUserId ->
                    token = newToken
                    userId = newUserId
                    currentScreen = Screen.TodoList
                }
            )
        }
        is Screen.TodoList -> {
            val viewModel: TodoListViewModel = viewModel { TodoListViewModel(apiService) }
            TodoListScreen(viewModel = viewModel, userId = userId)
        }
    }
}

sealed class Screen {
    object Login : Screen()
    object CreateAccount : Screen()
    object TodoList : Screen()
}