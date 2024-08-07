package com.example.assignment1.api

import com.squareup.moshi.Json

data class Todo(
    val id: String,
    val description: String,
    @Json(name = "completed") val completedRaw: Int
) {
    val completed: Boolean
        get() = completedRaw == 1
}

data class TodoRequest(
    val description: String,
    val completed: Int = 0
)

data class UserRequest(
    val email: String,
    val password: String,
    val name: String = ""
)

data class UserResponse(
    val token: String,
    val id: String
)