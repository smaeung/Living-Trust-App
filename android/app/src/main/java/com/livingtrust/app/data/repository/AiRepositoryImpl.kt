package com.livingtrust.app.data.repository

import com.livingtrust.app.data.remote.api.AiApi
import com.livingtrust.app.data.remote.dto.AnalyzeRequest
import com.livingtrust.app.data.remote.dto.ChatRequest
import com.livingtrust.app.domain.model.AiAnalysis
import com.livingtrust.app.domain.model.AiMessage
import com.livingtrust.app.domain.repository.AiRepository
import com.livingtrust.app.util.Resource
import javax.inject.Inject

/**
 * AiRepositoryImpl — sends user messages to the AI Lawyer backend and returns responses.
 *
 * WHY no local database caching here (unlike TrustRepositoryImpl)?
 * AI chat messages are conversational and temporary. Caching them would:
 *   - Consume significant storage on the device
 *   - Require complex cache invalidation logic
 *   - Provide little offline value (AI responses are only useful in real-time)
 * So every chat call goes directly to the network. If the network is unavailable,
 * the user sees an error message.
 *
 * Responsibility of this class:
 *   1. Wrap the raw message in the appropriate DTO
 *   2. Call the network API
 *   3. Convert the raw DTO response to a clean domain model
 *   4. Catch all exceptions and return Resource.Error instead of crashing
 */
class AiRepositoryImpl @Inject constructor(
    private val aiApi: AiApi  // Retrofit interface for /api/ai/* endpoints
) : AiRepository {

    /**
     * Sends a chat message and returns the AI's response.
     *
     * The returned AiMessage has isUser=false because it came from the AI,
     * not the user. The ViewModel uses this flag to display the message on
     * the correct side of the chat bubble UI.
     *
     * @param message The user's question (already validated by ChatWithAiUseCase)
     * @return Resource.Success with the AI response, or Resource.Error if the
     *         network call failed (no internet, server down, etc.)
     */
    override suspend fun chat(message: String): Resource<AiMessage> {
        return try {
            val response = aiApi.chat(ChatRequest(message))
            // Convert ChatResponse DTO → AiMessage domain model
            // isUser = false marks this as an AI response (shown on left side in chat UI)
            Resource.Success(AiMessage(content = response.response, isUser = false))
        } catch (e: Exception) {
            Resource.Error(e.message ?: "AI chat failed")
        }
    }

    /**
     * Sends document text for AI analysis and returns a scored review.
     *
     * The nested map { } call converts each AnalyzeIssue DTO into an
     * AiAnalysis.Issue domain model. WHY nested data classes?
     * AiAnalysis.Issue is an inner data class because it conceptually belongs
     * to an analysis result and would not make sense on its own.
     *
     * @param documentText Raw text content of the trust document to analyze
     * @return Resource.Success with score (0-100), issues, recommendations, and summary
     */
    override suspend fun analyzeDocument(documentText: String): Resource<AiAnalysis> {
        return try {
            val response = aiApi.analyze(AnalyzeRequest(documentText))
            Resource.Success(
                AiAnalysis(
                    score = response.score,
                    summary = response.summary,
                    recommendations = response.recommendations,
                    // Convert each AnalyzeIssue DTO to the domain Issue model
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
