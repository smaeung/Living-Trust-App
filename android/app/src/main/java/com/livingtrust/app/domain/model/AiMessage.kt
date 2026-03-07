package com.livingtrust.app.domain.model

data class AiMessage(
    val content: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

data class AiAnalysis(
    val score: Int,
    val summary: String,
    val recommendations: List<String>,
    val issues: List<Issue>
) {
    data class Issue(
        val severity: String,
        val text: String,
        val suggestion: String
    )
}
