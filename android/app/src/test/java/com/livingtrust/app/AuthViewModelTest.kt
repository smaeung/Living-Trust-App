package com.livingtrust.app

import com.livingtrust.app.domain.model.User
import com.livingtrust.app.domain.usecase.auth.LoginUseCase
import com.livingtrust.app.domain.usecase.auth.RegisterUseCase
import com.livingtrust.app.presentation.auth.AuthViewModel
import com.livingtrust.app.util.Resource
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import app.cash.turbine.test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var loginUseCase: LoginUseCase
    private lateinit var registerUseCase: RegisterUseCase
    private lateinit var viewModel: AuthViewModel

    private val mockUser = User(id = "1", email = "test@example.com", name = "Test User")

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        loginUseCase = mockk()
        registerUseCase = mockk()
        viewModel = AuthViewModel(loginUseCase, registerUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is not loading and not logged in`() = runTest {
        viewModel.state.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertFalse(state.isLoggedIn)
            assertNull(state.error)
            assertNull(state.user)
        }
    }

    @Test
    fun `login success updates state with user and isLoggedIn true`() = runTest {
        coEvery { loginUseCase("test@example.com", "password123") } returns Resource.Success(mockUser)

        viewModel.state.test {
            awaitItem() // initial

            viewModel.login("test@example.com", "password123")
            advanceUntilIdle()

            // loading state
            val loadingState = awaitItem()
            assertTrue(loadingState.isLoading)

            // success state
            val successState = awaitItem()
            assertFalse(successState.isLoading)
            assertTrue(successState.isLoggedIn)
            assertEquals(mockUser, successState.user)
            assertNull(successState.error)
        }
    }

    @Test
    fun `login failure sets error message`() = runTest {
        coEvery { loginUseCase("bad@example.com", "wrong") } returns Resource.Error("Invalid credentials")

        viewModel.state.test {
            awaitItem() // initial

            viewModel.login("bad@example.com", "wrong")
            advanceUntilIdle()

            awaitItem() // loading

            val errorState = awaitItem()
            assertFalse(errorState.isLoading)
            assertFalse(errorState.isLoggedIn)
            assertEquals("Invalid credentials", errorState.error)
        }
    }

    @Test
    fun `register success updates state with user and isLoggedIn true`() = runTest {
        coEvery { registerUseCase("Test User", "test@example.com", "password123") } returns Resource.Success(mockUser)

        viewModel.state.test {
            awaitItem() // initial

            viewModel.register("Test User", "test@example.com", "password123")
            advanceUntilIdle()

            awaitItem() // loading

            val successState = awaitItem()
            assertTrue(successState.isLoggedIn)
            assertEquals(mockUser, successState.user)
        }
    }

    @Test
    fun `clearError sets error to null`() = runTest {
        coEvery { loginUseCase(any(), any()) } returns Resource.Error("Some error")

        viewModel.login("x@x.com", "pass")
        advanceUntilIdle()

        viewModel.clearError()

        val state = viewModel.state.value
        assertNull(state.error)
    }
}
