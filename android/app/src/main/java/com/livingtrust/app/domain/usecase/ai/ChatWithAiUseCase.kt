package com.livingtrust.app.domain.usecase.ai

import com.livingtrust.app.domain.model.AiMessage
import com.livingtrust.app.domain.repository.AiRepository
import com.livingtrust.app.util.Resource
import javax.inject.Inject

/**
 * Use case that sends a user message to the AI and returns the response.
 *
 * WHY is input validation here even though AiViewModel also checks for blank messages?
 * - Defence in depth: the ViewModel's check prevents sending an API request for an
 *   obviously empty message (good for UX). The Use Case check is the authoritative
 *   business rule — it would catch blank messages even if the ViewModel somehow didn't.
 * - Use Cases are independently testable. If we test ChatWithAiUseCase in isolation
 *   (without the ViewModel), the blank check ensures correct behavior.
 *
 * WHY `message.trim()` before sending to the repository?
 * - Mobile keyboards often append trailing spaces. Trimming ensures the AI receives
 *   the actual question text, not "What is a trust   ".
 * - The trimmed message is also what gets sent to the backend — consistent with what
 *   the user sees displayed in the chat bubble.
 *
 * WHY suspend here?
 * - The AI call is a network request (async I/O). `suspend` ensures it runs on a
 *   coroutine and never blocks the main thread.
 */
class ChatWithAiUseCase @Inject constructor(
    private val aiRepository: AiRepository
) {
    /**
     * Validates the message and sends it to the AI backend.
     *
     * @param message the user's chat message
     * @return Resource.Success(AiMessage) with the AI's reply, or Resource.Error on failure
     */
    suspend operator fun invoke(message: String): Resource<AiMessage> {
        if (message.isBlank()) return Resource.Error("Message cannot be empty")
        return aiRepository.chat(message.trim())
    }
}
