package com.livingtrust.app.domain.usecase.ai

import com.livingtrust.app.domain.model.AiMessage
import com.livingtrust.app.domain.repository.AiRepository
import com.livingtrust.app.util.Resource
import javax.inject.Inject

class ChatWithAiUseCase @Inject constructor(
    private val aiRepository: AiRepository
) {
    suspend operator fun invoke(message: String): Resource<AiMessage> {
        if (message.isBlank()) return Resource.Error("Message cannot be empty")
        return aiRepository.chat(message.trim())
    }
}
