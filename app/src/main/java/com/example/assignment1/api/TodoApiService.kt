package com.example.assignment1.api

import retrofit2.http.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import okhttp3.OkHttpClient

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

        fun create(): TodoApiService {
            val moshi = Moshi.Builder()
                .addLast(KotlinJsonAdapterFactory())
                .build()

            val client = OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val original = chain.request()
                    val url = original.url.newBuilder()
                        .addQueryParameter("apikey", API_KEY)
                        .build()
                    val request = original.newBuilder()
                        .url(url)
                        .build()
                    chain.proceed(request)
                }
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
