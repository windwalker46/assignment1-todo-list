package com.example.assignment1.api

data class Todo(
    val id: String,
    val text: String,
    val completed: Boolean
)

data class TodoRequest(
    val text: String,
    val completed: Boolean
)

data class UserRequest(
    val email: String,
    val password: String
)

data class UserResponse(
    val token: String,
    val id: String
)