package com.livingtrust.app.domain.repository

import com.livingtrust.app.domain.model.AiAnalysis
import com.livingtrust.app.domain.model.AiMessage
import com.livingtrust.app.util.Resource

/**
 * Domain repository interface for AI-powered features.
 *
 * WHY an interface? (same principle as AuthRepository and TrustRepository)
 * - The domain layer defines WHAT the AI repository can do, not HOW it does it.
 *   AiRepositoryImpl (data layer) handles the actual HTTP calls to the backend.
 *
 * WHY does the AI stay on the backend instead of running on-device?
 * - OpenAI GPT-4 requires significant compute that mobile devices can't run locally.
 * - Running AI on the backend also keeps the API key secret (never embedded in the APK).
 * - The backend handles rate limiting, billing, and prompt engineering centrally.
 * - If a better model is released, we update the backend — not every installed app.
 *
 * WHY no Flow here (unlike TrustRepository)?
 * - AI chat responses are one-shot: send a message, get one response.
 *   There's no need to observe changing data — each call is independent.
 * - Streaming token-by-token responses (like ChatGPT typing effect) would use Flow,
 *   but that adds complexity not needed for v1.
 *
 * WHY two separate functions (chat vs analyzeDocument)?
 * - `chat`: conversational Q&A — open-ended questions about living trusts.
 * - `analyzeDocument`: structured analysis of a specific trust document text,
 *   returning a score, issues list, and recommendations.
 * These are different backend endpoints with different response formats.
 */
interface AiRepository {

    /**
     * Sends a user message to the AI backend and returns the AI's reply.
     *
     * @param message the user's question or statement
     * @return Resource.Success(AiMessage) on success, Resource.Error with message on failure
     */
    suspend fun chat(message: String): Resource<AiMessage>

    /**
     * Sends trust document text to the AI for analysis and returns a structured report.
     *
     * @param documentText the full text of the trust document to be reviewed
     * @return Resource.Success(AiAnalysis) containing score, issues, and recommendations
     */
    suspend fun analyzeDocument(documentText: String): Resource<AiAnalysis>
}
