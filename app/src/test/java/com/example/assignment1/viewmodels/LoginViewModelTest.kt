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
class LoginViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: LoginViewModel
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
        viewModel = LoginViewModel(apiService, sharedPreferences)

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
    fun `login should succeed when credentials are valid`() = runTest {
        val email = "test@example.com"
        val password = "password123"
        val token = "validToken"
        val userId = "123"
        coEvery { apiService.loginUser(UserRequest(email, password)) } returns UserResponse(token, userId)

        viewModel.login(email, password)

        viewModel.loginState.test(timeout = 5.seconds) {
            assertEquals(LoginViewModel.LoginState.Initial, awaitItem())
            assertEquals(LoginViewModel.LoginState.Loading, awaitItem())
            val result = awaitItem()
            assertTrue(result is LoginViewModel.LoginState.Success)
            assertEquals(token, (result as LoginViewModel.LoginState.Success).token)
            assertEquals(userId, result.userId)
            cancelAndConsumeRemainingEvents()
        }

        coVerify { apiService.loginUser(UserRequest(email, password)) }
        verify {
            sharedPreferences.edit()
            editor.putString("token", token)
            editor.putString("userId", userId)
            editor.apply()
        }
    }

    @Test
    fun `login should fail when credentials are invalid`() = runTest {
        val email = "test@example.com"
        val password = "wrongpassword"
        coEvery { apiService.loginUser(UserRequest(email, password)) } throws Exception("Invalid credentials")

        viewModel.login(email, password)

        viewModel.loginState.test(timeout = 5.seconds) {
            assertEquals(LoginViewModel.LoginState.Initial, awaitItem())
            assertEquals(LoginViewModel.LoginState.Loading, awaitItem())
            val result = awaitItem()
            assertTrue(result is LoginViewModel.LoginState.Error)
            assertEquals("Login failed: Invalid credentials", (result as LoginViewModel.LoginState.Error).message)
            cancelAndConsumeRemainingEvents()
        }

        coVerify { apiService.loginUser(UserRequest(email, password)) }
        verify(exactly = 0) { sharedPreferences.edit() }
    }
}