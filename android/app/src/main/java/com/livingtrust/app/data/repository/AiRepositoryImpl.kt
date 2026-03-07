package com.livingtrust.app.data.repository

import com.livingtrust.app.data.remote.api.AiApi
import com.livingtrust.app.data.remote.dto.AnalyzeRequest
import com.livingtrust.app.data.remote.dto.ChatRequest
import com.livingtrust.app.domain.model.AiAnalysis
import com.livingtrust.app.domain.model.AiMessage
import com.livingtrust.app.domain.repository.AiRepository
import com.livingtrust.app.util.Resource
import javax.inject.Inject

class AiRepositoryImpl @Inject constructor(
    private val aiApi: AiApi
) : AiRepository {

    override suspend fun chat(message: String): Resource<AiMessage> {
        return try {
            val response = aiApi.chat(ChatRequest(message))
            Resource.Success(AiMessage(content = response.response, isUser = false))
        } catch (e: Exception) {
            Resource.Error(e.message ?: "AI chat failed")
        }
    }

    override suspend fun analyzeDocument(documentText: String): Resource<AiAnalysis> {
        return try {
            val response = aiApi.analyze(AnalyzeRequest(documentText))
            Resource.Success(
                AiAnalysis(
                    score = response.score,
                    summary = response.summary,
                    recommendations = response.recommendations,
                    issues = response.issues.map {
                        AiAnalysis.Issue(
                            severity = it.severity,
                            text = it.text,
                            suggestion = it.suggestion
                        )
                    }
                )
            )
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Document analysis failed")
        }
    }
}
