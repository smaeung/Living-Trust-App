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

data class AiState(
    val messages: List<AiMessage> = listOf(
        AiMessage(
            content = "Hello! I'm your AI Lawyer Assistant specializing in Living Trusts. How can I help you today?\n\nTry asking:\n• What is a Living Trust?\n• Revocable vs Irrevocable?\n• How much does it cost?",
            isUser = false
        )
    ),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AiViewModel @Inject constructor(
    private val chatWithAiUseCase: ChatWithAiUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AiState())
    val state: StateFlow<AiState> = _state.asStateFlow()

    fun sendMessage(message: String) {
        if (message.isBlank()) return

        val userMessage = AiMessage(content = message, isUser = true)
        _state.value = _state.value.copy(
            messages = _state.value.messages + userMessage,
            isLoading = true,
            error = null
        )

        viewModelScope.launch {
            when (val result = chatWithAiUseCase(message)) {
                is Resource.Success -> _state.value = _state.value.copy(
                    messages = _state.value.messages + result.data,
                    isLoading = false
                )
                is Resource.Error -> _state.value = _state.value.copy(
                    isLoading = false,
                    error = result.message
                )
                Resource.Loading -> Unit
            }
        }
    }
}
