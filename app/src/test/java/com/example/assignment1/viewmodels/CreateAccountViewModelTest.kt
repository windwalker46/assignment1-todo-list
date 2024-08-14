package com.example.assignment1.viewmodels

import android.content.SharedPreferences
import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.example.assignment1.api.TodoApiService
import com.example.assignment1.api.UserRequest
import com.example.assignment1.api.UserResponse
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class CreateAccountViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: CreateAccountViewModel
    private lateinit var apiService: TodoApiService
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        apiService = mockk()
        sharedPreferences = mockk()
        editor = mockk()
        viewModel = CreateAccountViewModel(apiService, sharedPreferences)

        every { sharedPreferences.edit() } returns editor
        every { editor.putString(any(), any()) } returns editor
        every { editor.apply() } just Runs

        mockkStatic(Log::class)
        every { Log.e(any(), any(), any()) } returns 0
        every { Log.d(any(), any()) } returns 0
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `createAccount should succeed when user details are valid`() = runTest {
        val name = "John Doe"
        val email = "john@example.com"
        val password = "password123"
        val token = "validToken"
        val userId = "123"
        coEvery { apiService.registerUser(UserRequest(email, password, name)) } returns UserResponse(token, userId)

        viewModel.createAccount(name, email, password)

        viewModel.createAccountState.test(timeout = 5.seconds) {
            assertEquals(CreateAccountViewModel.CreateAccountState.Initial, awaitItem())
            assertEquals(CreateAccountViewModel.CreateAccountState.Loading, awaitItem())
            val result = awaitItem()
            assertTrue(result is CreateAccountViewModel.CreateAccountState.Success)
            assertEquals(token, (result as CreateAccountViewModel.CreateAccountState.Success).token)
            assertEquals(userId, result.userId)
            cancelAndConsumeRemainingEvents()
        }

        coVerify { apiService.registerUser(UserRequest(email, password, name)) }
        verify {
            sharedPreferences.edit()
            editor.putString("token", token)
            editor.putString("userId", userId)
            editor.apply()
        }
    }

    @Test
    fun `createAccount should fail when user details are invalid`() = runTest {
        val name = "John Doe"
        val email = "john@example.com"
        val password = "weak"
        coEvery { apiService.registerUser(UserRequest(email, password, name)) } throws Exception("Invalid user details")

        viewModel.createAccount(name, email, password)

        // Then
        viewModel.createAccountState.test(timeout = 5.seconds) {
            assertEquals(CreateAccountViewModel.CreateAccountState.Initial, awaitItem())
            assertEquals(CreateAccountViewModel.CreateAccountState.Loading, awaitItem())
            val result = awaitItem()
            assertTrue(result is CreateAccountViewModel.CreateAccountState.Error)
            assertEquals("Account creation failed: Invalid user details", (result as CreateAccountViewModel.CreateAccountState.Error).message)
            cancelAndConsumeRemainingEvents()
        }

        coVerify { apiService.registerUser(UserRequest(email, password, name)) }
        verify(exactly = 0) { sharedPreferences.edit() }
    }
}