package com.livingtrust.app.data.remote.api

import com.livingtrust.app.data.remote.dto.*
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * AiApi — Retrofit interface for AI Lawyer endpoints.
 *
 * These endpoints proxy to OpenAI's GPT-4 API on the backend server.
 * The backend handles the OpenAI API key and system prompt, so the Android
 * app never directly contacts OpenAI — it only talks to our own server.
 *
 * Why keep AI on the backend?
 *   - The OpenAI API key stays secret (never embedded in the APK)
 *   - The system prompt ("you are a lawyer specializing in Living Trusts...")
 *     can be updated without releasing a new app version
 *   - Rate limiting and cost control happen centrally on the server
 *
 * Both endpoints are POST because they send data in the request body.
 * GET requests cannot have a body by HTTP convention.
 */
interface AiApi {

    /**
     * Sends a chat message to the AI Lawyer and returns a response.
     * Maps to: POST /api/ai/chat
     *
     * The backend wraps the message in a GPT-4 conversation with a legal
     * system prompt. If no OPENAI_API_KEY is set on the server, it returns
     * a hardcoded mock response instead.
     *
     * @param request Contains the user's question and optional conversation context.
     * @return ChatResponse with the AI's answer text.
     */
    @POST("api/ai/chat")
    suspend fun chat(@Body request: ChatRequest): ChatResponse

    /**
     * Submits a trust document's text for AI analysis and scoring.
     * Maps to: POST /api/ai/analyze
     *
     * The AI reads the document text, scores its quality (0-100), and returns
     * a list of issues and recommendations for improvement.
     *
     * @param request Contains the raw text content of the trust document.
     * @return AnalyzeResponse with score, issues list, recommendations, and summary.
     */
    @POST("api/ai/analyze")
    suspend fun analyze(@Body request: AnalyzeRequest): AnalyzeResponse
}
