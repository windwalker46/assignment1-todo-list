package com.example.assignment1.api

import retrofit2.http.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import android.util.Log
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson

interface TodoApiService {
    @GET("/api/users/{userId}/todos")
    suspend fun getTodos(@Path("userId") userId: String): List<Todo>

    @POST("/api/users/{userId}/todos")
    suspend fun createTodo(@Path("userId") userId: String, @Body todo: TodoRequest): Todo

    @PUT("/api/users/{userId}/todos/{id}")
    suspend fun updateTodo(@Path("userId") userId: String, @Path("id") id: String, @Body todo: TodoRequest): Todo

    @POST("/api/users/register")
    suspend fun registerUser(@Body user: UserRequest): UserResponse

    @POST("/api/users/login")
    suspend fun loginUser(@Body user: UserRequest): UserResponse

    companion object {
        private const val BASE_URL = "https://todos.simpleapi.dev/"
        private const val API_KEY = "01f294a9-c404-46ed-8842-ec3f9ab5dba1"

        fun create(token: String? = null): TodoApiService {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .addInterceptor { chain ->
                    val original = chain.request()
                    val url = original.url.newBuilder()
                        .addQueryParameter("apikey", API_KEY)
                        .build()
                    val requestBuilder = original.newBuilder()
                        .url(url)

                    if (!token.isNullOrEmpty()) {
                        requestBuilder.addHeader("Authorization", "Bearer $token")
                    }

                    val request = requestBuilder.build()
                    Log.d("API", "Request URL: ${request.url}")
                    Log.d("API", "Request Headers: ${request.headers}")
                    chain.proceed(request)
                }
                .build()

            val moshi = Moshi.Builder()
                .add(TodoAdapter)
                .addLast(KotlinJsonAdapterFactory())
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(TodoApiService::class.java)
        }
    }
}

object TodoAdapter {
    @FromJson
    fun fromJson(json: Map<String, Any?>): Todo {
        return Todo(
            id = json["id"].toString(),
            description = json["description"] as String,
            completedRaw = when (val completed = json["completed"]) {
                is Boolean -> if (completed) 1 else 0
                is Number -> completed.toInt()
                else -> 0 // Default to not completed if unexpected type
            }
        )
    }

    @ToJson
    fun toJson(todo: Todo): Map<String, Any> {
        return mapOf(
            "id" to todo.id,
            "description" to todo.description,
            "completed" to todo.completedRaw
        )
    }
}