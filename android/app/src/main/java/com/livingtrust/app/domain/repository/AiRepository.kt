package com.livingtrust.app.domain.repository

import com.livingtrust.app.domain.model.AiAnalysis
import com.livingtrust.app.domain.model.AiMessage
import com.livingtrust.app.util.Resource

interface AiRepository {
    suspend fun chat(message: String): Resource<AiMessage>
    suspend fun analyzeDocument(documentText: String): Resource<AiAnalysis>
}
