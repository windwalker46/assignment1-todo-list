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

class CreateAccountViewModel(
    private val apiService: TodoApiService,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {
    private val _createAccountState = MutableStateFlow<CreateAccountState>(CreateAccountState.Initial)
    val createAccountState: StateFlow<CreateAccountState> = _createAccountState

    fun createAccount(name: String, email: String, password: String) {
        viewModelScope.launch {
            _createAccountState.value = CreateAccountState.Loading
            try {
                val response = apiService.registerUser(UserRequest(email, password, name))
                sharedPreferences.edit().apply {
                    putString("token", response.token)
                    putString("userId", response.id.toString())
                    apply()
                }
                _createAccountState.value = CreateAccountState.Success(response.token, response.id.toString())
            } catch (e: Exception) {
                val errorMessage = "Account creation failed: ${e.message}"
                Log.e("CreateAccountViewModel", errorMessage, e)
                _createAccountState.value = CreateAccountState.Error(errorMessage)
            }
        }
    }

    sealed class CreateAccountState {
        object Initial : CreateAccountState()
        object Loading : CreateAccountState()
        data class Success(val token: String, val userId: String) : CreateAccountState()
        data class Error(val message: String) : CreateAccountState()
    }
}
