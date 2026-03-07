package com.livingtrust.app.domain.model

/**
 * Domain model representing a single message in the AI chat conversation.
 *
 * WHY a single class for both user and AI messages?
 * - A chat UI renders a list of messages in order. Both user messages and AI responses
 *   are the same conceptually — they have content and a sender.
 * - Using one class with an `isUser` flag is simpler than having two separate classes
 *   (UserMessage and AiMessage) that would need to be merged into a common list.
 *
 * WHY `isUser: Boolean` instead of an enum (e.g., Sender.USER / Sender.AI)?
 * - For this app, there are only ever two senders: the user and the AI.
 *   A Boolean is the simplest possible representation for a two-state value.
 *   If we added a third participant (e.g., a human lawyer), we'd upgrade to an enum.
 *
 * WHY `timestamp: Long = System.currentTimeMillis()`?
 * - Timestamps let us sort messages chronologically and display relative times (e.g., "2m ago").
 * - System.currentTimeMillis() is Unix epoch time in milliseconds — universally understood.
 * - Defaulting to the current time means callers don't have to set it manually.
 */
data class AiMessage(
    val content: String,
    val isUser: Boolean,          // true = user typed this; false = AI responded with this
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Domain model representing an AI-generated analysis of a trust document.
 *
 * WHY is AiAnalysis separate from AiMessage?
 * - Analysis is structured data (score, issues list, recommendations).
 *   It's not a chat message — it's the result of a specific "analyze document" operation.
 * - Keeping them separate lets the AI assistant screen show chat freely, while the
 *   documents screen shows structured analysis reports.
 *
 * WHY is `score` an Int (not a Float or String)?
 * - An integer score (e.g., 0–100) is easy to display as a progress bar or badge.
 *   Floats add unnecessary precision for a subjective quality score.
 *
 * WHY is `Issue` a nested data class inside AiAnalysis?
 * - Issue is only meaningful in the context of AiAnalysis. Nesting it signals to
 *   other developers "this class is not used standalone".
 *   If Issue were needed elsewhere, we'd promote it to a top-level class.
 *
 * WHY does Issue have `severity: String` instead of an enum (LOW/MEDIUM/HIGH)?
 * - The severity comes from the AI/backend as a string. Using String avoids a
 *   mapping step. In a future version, this could be an enum with a safe fallback.
 */
data class AiAnalysis(
    val score: Int,                           // quality score, e.g., 0-100
    val summary: String,                      // plain-English overview of findings
    val recommendations: List<String>,        // actionable suggestions
    val issues: List<Issue>                   // specific problems found
) {
    /**
     * Represents a single issue found during document analysis.
     *
     * @param severity "low", "medium", or "high" — how critical the issue is
     * @param text     description of the issue found in the document
     * @param suggestion what the user should do to fix this issue
     */
    data class Issue(
        val severity: String,
        val text: String,
        val suggestion: String
    )
}
