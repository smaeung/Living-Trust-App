package com.livingtrust.app.data.remote.dto

/**
 * AI DTOs — Data Transfer Objects for the AI Lawyer API endpoints.
 *
 * The backend forwards these requests to OpenAI's GPT-4 API and returns
 * the AI-generated response. There are two AI features:
 *
 * 1. Chat: free-form Q&A about Living Trusts
 *    POST /api/ai/chat  →  ChatRequest  →  ChatResponse
 *
 * 2. Document Analysis: paste trust text, get a scored review
 *    POST /api/ai/analyze  →  AnalyzeRequest  →  AnalyzeResponse
 */

/**
 * Request body for the AI chat endpoint.
 *
 * @param message The user's question, e.g. "What is a Living Trust?"
 * @param context Optional previous conversation context. Marked nullable (String?)
 *                with a default of null so callers can omit it. In Kotlin,
 *                `= null` means the parameter is optional when calling the function.
 */
data class ChatRequest(
    val message: String,
    val context: String? = null
)

/**
 * Response from the AI chat endpoint.
 *
 * @param response The AI-generated answer text (may include markdown formatting).
 * @param sources List of reference sources cited by the AI (often empty in mock mode).
 */
data class ChatResponse(
    val response: String,
    val sources: List<String>
)

/**
 * Request body for the document analysis endpoint.
 * The user pastes the full text content of their trust document here.
 *
 * @param documentText The raw text extracted from a PDF or typed document.
 */
data class AnalyzeRequest(
    val documentText: String
)

/**
 * A single problem found in the document during AI analysis.
 *
 * @param severity How serious the issue is: "high" | "medium" | "low"
 * @param text Description of the problem found.
 * @param suggestion What the user should do to fix it.
 */
data class AnalyzeIssue(
    val severity: String,
    val text: String,
    val suggestion: String
)

/**
 * Full analysis result from the AI document review endpoint.
 *
 * @param score Overall quality score 0-100. Higher is better.
 *              (e.g. 85 = well-structured with minor issues)
 * @param issues List of specific problems found, each with a severity and fix suggestion.
 * @param recommendations General improvements the user should consider.
 * @param summary A one-paragraph plain-English assessment of the document.
 */
data class AnalyzeResponse(
    val score: Int,
    val issues: List<AnalyzeIssue>,
    val recommendations: List<String>,
    val summary: String
)
