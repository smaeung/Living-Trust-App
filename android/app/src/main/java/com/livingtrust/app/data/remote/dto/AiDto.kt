package com.livingtrust.app.data.remote.dto

data class ChatRequest(
    val message: String,
    val context: String? = null
)

data class ChatResponse(
    val response: String,
    val sources: List<String>
)

data class AnalyzeRequest(
    val documentText: String
)

data class AnalyzeIssue(
    val severity: String,
    val text: String,
    val suggestion: String
)

data class AnalyzeResponse(
    val score: Int,
    val issues: List<AnalyzeIssue>,
    val recommendations: List<String>,
    val summary: String
)
