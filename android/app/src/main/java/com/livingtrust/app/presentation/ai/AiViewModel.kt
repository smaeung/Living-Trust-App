package com.livingtrust.app.presentation.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.livingtrust.app.domain.model.AiMessage
import com.livingtrust.app.domain.usecase.ai.ChatWithAiUseCase
import com.livingtrust.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the AI Chat Assistant screen.
 *
 * WHY `messages` is initialized with a welcome message?
 * - UX best practice: a chat screen with an empty message list looks broken.
 *   The welcome message guides new users by giving example questions they can ask.
 * - The welcome message has `isUser = false` (it comes from the AI side).
 * - Initializing in the default value means the welcome message appears the instant
 *   the screen opens, with zero latency — no network call needed.
 *
 * WHY `List<AiMessage>` instead of separate lists for user and AI messages?
 * - Chat UIs render messages in chronological order. A single ordered list is the
 *   natural representation: message[0] is the oldest, message[n-1] is the newest.
 *   The `isUser` field on each message tells the UI which side to render it on.
 */
data class AiState(
    val messages: List<AiMessage> = listOf(
        // Pre-seeded welcome message shown before any user interaction
        AiMessage(
            content = "Hello! I'm your AI Lawyer Assistant specializing in Living Trusts. How can I help you today?\n\nTry asking:\n• What is a Living Trust?\n• Revocable vs Irrevocable?\n• How much does it cost?",
            isUser = false
        )
    ),
    val isLoading: Boolean = false,  // true while waiting for AI response
    val error: String? = null        // network or API error message
)

/**
 * ViewModel for the AI Chat Assistant screen.
 *
 * WHY only one dependency (ChatWithAiUseCase)?
 * - The AI screen has a single operation: send a message, get a response.
 *   No local database, no complex state machine.
 *   Simple screens should have simple ViewModels.
 */
@HiltViewModel
class AiViewModel @Inject constructor(
    private val chatWithAiUseCase: ChatWithAiUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AiState())
    val state: StateFlow<AiState> = _state.asStateFlow()

    /**
     * Sends the user's message and waits for the AI response.
     *
     * WHY add the user message to the list BEFORE launching the coroutine?
     * - This is the "optimistic UI" pattern: show the user's message immediately so
     *   the chat feels instant. The user doesn't have to wait for the network round trip
     *   before seeing their own message in the chat bubble.
     * - The AI response is added later when it arrives.
     *
     * WHY set isLoading = true at the same time as adding the user message?
     * - Both state changes happen atomically via one copy() call.
     *   This prevents a momentary state where the user message is shown but
     *   the loading indicator hasn't appeared yet (which would look like a glitch).
     *
     * WHY blank check here AND in ChatWithAiUseCase?
     * - The check here prevents creating an AiMessage or launching a coroutine for
     *   obviously empty input (fast client-side short-circuit).
     * - The check in ChatWithAiUseCase is the authoritative business rule.
     *   Defence in depth: neither layer relies solely on the other.
     *
     * WHY `_state.value.messages + result.data` (not a replace)?
     * - We append the AI response to the existing list (which already contains the
     *   user message). Replacing would lose the conversation history.
     * - The + operator creates a new immutable list — correct for StateFlow immutability.
     */
    fun sendMessage(message: String) {
        if (message.isBlank()) return

        // Add user's message to the chat and show loading indicator immediately
        val userMessage = AiMessage(content = message, isUser = true)
        _state.value = _state.value.copy(
            messages = _state.value.messages + userMessage,
            isLoading = true,
            error = null
        )

        // Launch network call to get AI response
        viewModelScope.launch {
            when (val result = chatWithAiUseCase(message)) {
                is Resource.Success -> _state.value = _state.value.copy(
                    messages = _state.value.messages + result.data,  // append AI reply
                    isLoading = false
                )
                is Resource.Error -> _state.value = _state.value.copy(
                    isLoading = false,
                    error = result.message  // show error banner; user can retry
                )
                Resource.Loading -> Unit  // use case never emits Loading; exhaustive when
            }
        }
    }
}
