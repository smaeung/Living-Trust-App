package com.livingtrust.app

import com.livingtrust.app.domain.model.AiMessage
import com.livingtrust.app.domain.usecase.ai.ChatWithAiUseCase
import com.livingtrust.app.presentation.ai.AiViewModel
import com.livingtrust.app.util.Resource
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AiViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var chatWithAiUseCase: ChatWithAiUseCase
    private lateinit var viewModel: AiViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        chatWithAiUseCase = mockk()
        viewModel = AiViewModel(chatWithAiUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has welcome message`() {
        val messages = viewModel.state.value.messages
        assertEquals(1, messages.size)
        assertFalse(messages.first().isUser)
    }

    @Test
    fun `sendMessage adds user message and AI response`() = runTest {
        val aiResponse = AiMessage(content = "A Living Trust is a legal document...", isUser = false)
        coEvery { chatWithAiUseCase("What is a Living Trust?") } returns Resource.Success(aiResponse)

        viewModel.sendMessage("What is a Living Trust?")
        advanceUntilIdle()

        val messages = viewModel.state.value.messages
        assertEquals(3, messages.size) // welcome + user + ai
        assertTrue(messages[1].isUser)
        assertEquals("What is a Living Trust?", messages[1].content)
        assertFalse(messages[2].isUser)
        assertEquals(aiResponse.content, messages[2].content)
    }

    @Test
    fun `sendMessage with blank text does nothing`() = runTest {
        viewModel.sendMessage("   ")
        advanceUntilIdle()
        assertEquals(1, viewModel.state.value.messages.size) // only welcome message
    }

    @Test
    fun `sendMessage on error sets error state`() = runTest {
        coEvery { chatWithAiUseCase(any()) } returns Resource.Error("Network error")

        viewModel.sendMessage("Hello")
        advanceUntilIdle()

        assertEquals("Network error", viewModel.state.value.error)
        assertFalse(viewModel.state.value.isLoading)
    }
}
