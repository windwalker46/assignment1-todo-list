package com.example.assignment1.viewmodels

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.assignment1.api.TodoApiService
import com.example.assignment1.api.UserRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val apiService: TodoApiService,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Initial)
    val loginState: StateFlow<LoginState> = _loginState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                val response = apiService.loginUser(UserRequest(email, password))
                sharedPreferences.edit().apply {
                    putString("token", response.token)
                    putString("userId", response.id.toString())
                    apply()
                }
                _loginState.value = LoginState.Success(response.token, response.id.toString())
            } catch (e: Exception) {
                _loginState.value = LoginState.Error("Login failed: ${e.message}")
            }
        }
    }

    sealed class LoginState {
        object Initial : LoginState()
        object Loading : LoginState()
        data class Success(val token: String, val userId: String) : LoginState()
        data class Error(val message: String) : LoginState()
    }
}
