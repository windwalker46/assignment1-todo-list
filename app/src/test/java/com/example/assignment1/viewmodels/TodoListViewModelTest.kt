package com.example.assignment1.viewmodels

import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.example.assignment1.api.Todo
import com.example.assignment1.api.TodoApiService
import com.example.assignment1.api.TodoRequest
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
class TodoListViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: TodoListViewModel
    private lateinit var apiService: TodoApiService
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        apiService = mockk()
        viewModel = TodoListViewModel(apiService)

        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `fetchTodos should succeed when API call is successful`() = runTest {
        val userId = "123"
        val todoList = listOf(
            Todo("1", "Task 1", 0),
            Todo("2", "Task 2", 1)
        )
        coEvery { apiService.getTodos(userId) } returns todoList

        viewModel.fetchTodos(userId)

// When
        viewModel.todoListState.test(timeout = 5.seconds) {
            assertEquals(TodoListViewModel.TodoListState.Initial, awaitItem())
            assertEquals(TodoListViewModel.TodoListState.Loading, awaitItem())
            assertEquals(TodoListViewModel.TodoListState.Success, awaitItem())
            cancelAndConsumeRemainingEvents()
        }

        viewModel.todos.test {
            assertEquals(todoList, awaitItem())
            cancelAndConsumeRemainingEvents()
        }

        coVerify { apiService.getTodos(userId) }
    }

    @Test
    fun `fetchTodos should fail when API call throws an exception`() = runTest {
        val userId = "123"
        val errorMessage = "Network error"
        coEvery { apiService.getTodos(userId) } throws Exception(errorMessage)

        viewModel.fetchTodos(userId)


        viewModel.todoListState.test(timeout = 5.seconds) {
            assertEquals(TodoListViewModel.TodoListState.Initial, awaitItem())
            assertEquals(TodoListViewModel.TodoListState.Loading, awaitItem())
            val result = awaitItem()
            assertTrue(result is TodoListViewModel.TodoListState.Error)
            assertEquals("Failed to fetch todos: $errorMessage", (result as TodoListViewModel.TodoListState.Error).message)
            cancelAndConsumeRemainingEvents()
        }

        viewModel.todos.test {
            assertEquals(emptyList(), awaitItem())
            cancelAndConsumeRemainingEvents()
        }

        coVerify { apiService.getTodos(userId) }
    }

    @Test
    fun `createTodo should succeed when API call is successful`() = runTest {
        val userId = "123"
        val description = "New Task"
        val newTodo = Todo("3", description, 0)
        coEvery { apiService.createTodo(userId, TodoRequest(description)) } returns newTodo

        viewModel.createTodo(userId, description)


        viewModel.todoListState.test(timeout = 5.seconds) {
            assertEquals(TodoListViewModel.TodoListState.Initial, awaitItem())
            assertEquals(TodoListViewModel.TodoListState.Loading, awaitItem())
            assertEquals(TodoListViewModel.TodoListState.Success, awaitItem())
            cancelAndConsumeRemainingEvents()
        }

        viewModel.todos.test {
            val updatedList = awaitItem()
            assertTrue(updatedList.contains(newTodo))
            cancelAndConsumeRemainingEvents()
        }

        coVerify { apiService.createTodo(userId, TodoRequest(description)) }
    }

    @Test
    fun `createTodo should fail when API call throws an exception`() = runTest {
        val userId = "123"
        val description = "New Task"
        val errorMessage = "Network error"
        coEvery { apiService.createTodo(userId, TodoRequest(description)) } throws Exception(errorMessage)

        viewModel.createTodo(userId, description)


        viewModel.todoListState.test(timeout = 5.seconds) {
            assertEquals(TodoListViewModel.TodoListState.Initial, awaitItem())
            assertEquals(TodoListViewModel.TodoListState.Loading, awaitItem())
            val result = awaitItem()
            assertTrue(result is TodoListViewModel.TodoListState.Error)
            assertEquals("Failed to create todo: $errorMessage", (result as TodoListViewModel.TodoListState.Error).message)
            cancelAndConsumeRemainingEvents()
        }

        coVerify { apiService.createTodo(userId, TodoRequest(description)) }
    }

    @Test
    fun `updateTodo should succeed when API call is successful`() = runTest {
        val userId = "123"
        val todo = Todo("1", "Task 1", 0)
        val updatedTodo = todo.copy(completedRaw = 1)
        coEvery { apiService.updateTodo(userId, todo.id, TodoRequest(todo.description, 1)) } returns updatedTodo

        viewModel.updateTodo(userId, todo)


        viewModel.todoListState.test(timeout = 5.seconds) {
            assertEquals(TodoListViewModel.TodoListState.Initial, awaitItem())
            assertEquals(TodoListViewModel.TodoListState.Loading, awaitItem())
            assertEquals(TodoListViewModel.TodoListState.Success, awaitItem())
            cancelAndConsumeRemainingEvents()
        }

        viewModel.todos.test {
            val updatedList = awaitItem()
            assertTrue(updatedList.contains(updatedTodo))
            cancelAndConsumeRemainingEvents()
        }

        coVerify { apiService.updateTodo(userId, todo.id, TodoRequest(todo.description, 1)) }
    }

    @Test
    fun `updateTodo should fail when API call throws an exception`() = runTest {
        val userId = "123"
        val todo = Todo("1", "Task 1", 0)
        val errorMessage = "Network error"
        coEvery { apiService.updateTodo(userId, todo.id, TodoRequest(todo.description, 1)) } throws Exception(errorMessage)

        viewModel.updateTodo(userId, todo)


        viewModel.todoListState.test(timeout = 5.seconds) {
            assertEquals(TodoListViewModel.TodoListState.Initial, awaitItem())
            assertEquals(TodoListViewModel.TodoListState.Loading, awaitItem())
            val result = awaitItem()
            assertTrue(result is TodoListViewModel.TodoListState.Error)
            assertEquals("Failed to update todo: $errorMessage", (result as TodoListViewModel.TodoListState.Error).message)
            cancelAndConsumeRemainingEvents()
        }

        coVerify { apiService.updateTodo(userId, todo.id, TodoRequest(todo.description, 1)) }
    }
}