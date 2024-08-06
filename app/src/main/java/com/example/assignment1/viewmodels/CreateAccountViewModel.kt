package com.example.assignment1.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.assignment1.api.TodoApiService
import com.example.assignment1.api.UserRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CreateAccountViewModel(private val apiService: TodoApiService) : ViewModel() {
    private val _createAccountState = MutableStateFlow<CreateAccountState>(CreateAccountState.Initial)
    val createAccountState: StateFlow<CreateAccountState> = _createAccountState

    fun createAccount(email: String, password: String) {
        viewModelScope.launch {
            _createAccountState.value = CreateAccountState.Loading
            try {
                val response = apiService.registerUser(UserRequest(email, password))
                // save token user ID
                _createAccountState.value = CreateAccountState.Success(response.token, response.id)
            } catch (e: Exception) {
                _createAccountState.value = CreateAccountState.Error("Account creation failed. Please try again.")
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